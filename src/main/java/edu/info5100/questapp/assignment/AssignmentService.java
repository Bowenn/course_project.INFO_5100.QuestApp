package edu.info5100.questapp.assignment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.info5100.questapp.assignment.dto.AssignmentResponse;
import edu.info5100.questapp.assignment.dto.UpdateAssignmentRequest;
import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.task.TaskRepository;
import edu.info5100.questapp.task.TaskStatus;
import edu.info5100.questapp.user.User;
import edu.info5100.questapp.user.UserRepository;

/**
 * Service for assignment lifecycle. Taker updates status: ASSIGNED → IN_PROGRESS → COMPLETED,
 * or DECLINED to reject.
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get assignment by ID. Access: giver, taker, or admin.
     */
    public AssignmentResponse getById(Long assignmentId, User currentUser) {
        var assignment = getAssignmentOrThrow(assignmentId);
        validateAssignmentAccess(assignment, currentUser);
        return AssignmentResponse.from(assignment);
    }

    /**
     * List assignments for current user:
     * - GIVER: assignments for my tasks
     * - TAKER: assignments where I am the taker
     * - ADMIN: all assignments
     */
    public List<AssignmentResponse> list(User currentUser) {
        return switch (currentUser.getRole()) {
            case GIVER -> assignmentRepository.findAll().stream()
                .filter(a -> a.getTask().getGiver().getId().equals(currentUser.getId()))
                .sorted((a, b) -> b.getAssignedAt().compareTo(a.getAssignedAt()))
                .map(AssignmentResponse::from).toList();
            case TAKER -> assignmentRepository.findByTakerOrderByAssignedAtDesc(currentUser).stream()
                .map(AssignmentResponse::from).toList();
            case ADMIN -> assignmentRepository.findAll().stream()
                .sorted((a, b) -> b.getAssignedAt().compareTo(a.getAssignedAt()))
                .map(AssignmentResponse::from).toList();
        };
    }

    /**
     * Taker updates assignment status: start work, complete, or decline.
     */
    @Transactional
    public AssignmentResponse update(Long assignmentId, UpdateAssignmentRequest request, User currentUser) {
        var assignment = getAssignmentOrThrow(assignmentId);
        if (!assignment.getTaker().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Only the assigned taker can update this assignment");
        }

        var task = assignment.getTask();
        var status = request.status();

        switch (status) {
            case IN_PROGRESS -> {
                if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
                    throw new BadRequestException("Only ASSIGNED can transition to IN_PROGRESS");
                }
                assignment.setStatus(AssignmentStatus.IN_PROGRESS);
                task.setStatus(TaskStatus.IN_PROGRESS);
            }
            case COMPLETED -> {
                if (assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
                    throw new BadRequestException("Only IN_PROGRESS can transition to COMPLETED");
                }
                assignment.setStatus(AssignmentStatus.COMPLETED);
                assignment.setCompletedAt(java.time.Instant.now());
                assignment.setNote(request.note());
                task.setStatus(TaskStatus.COMPLETED);
            }
            case DECLINED -> {
                if (assignment.getStatus() != AssignmentStatus.ASSIGNED) {
                    throw new BadRequestException("Only ASSIGNED can be DECLINED");
                }
                assignment.setStatus(AssignmentStatus.DECLINED);
                assignment.setNote(request.note());
                task.setStatus(TaskStatus.PUBLISHED); // Back to available
            }
            default -> throw new BadRequestException("Invalid status: " + status);
        }

        taskRepository.save(task);
        assignment = assignmentRepository.save(assignment);
        return AssignmentResponse.from(assignment);
    }

    /**
     * Giver confirms task completion and transfers bounty to taker.
     */
    @Transactional
    public AssignmentResponse confirm(Long assignmentId, User currentUser) {
        var assignment = getAssignmentOrThrow(assignmentId);
        var task = assignment.getTask();

        // Only giver or admin can confirm
        if (!task.getGiver().getId().equals(currentUser.getId()) && currentUser.getRole() != edu.info5100.questapp.user.Role.ADMIN) {
            throw new BadRequestException("Only the task giver or admin can confirm completion");
        }

        // Only completed assignments can be confirmed
        if (assignment.getStatus() != AssignmentStatus.COMPLETED) {
            throw new BadRequestException("Only COMPLETED assignments can be confirmed");
        }

        // Transfer bounty from task to taker
        Double bounty = task.getBounty() != null ? task.getBounty() : 0.0;
        if (bounty > 0) {
            User taker = assignment.getTaker();
            taker.setBalance(taker.getBalance() + bounty);
            userRepository.save(taker);
            task.setBounty(0.0); // Mark as paid
        }

        task.setStatus(TaskStatus.CONFIRMED); // New status for confirmed tasks

        taskRepository.save(task);
        assignment = assignmentRepository.save(assignment);
        return AssignmentResponse.from(assignment);
    }

    /**
     * Confirm a task completion by task ID (convenience endpoint for the task owner).
     * Finds the COMPLETED assignment for the given task and confirms it.
     */
    @Transactional
    public AssignmentResponse confirmByTaskId(Long taskId, User currentUser) {
        var assignment = assignmentRepository.findByTask_IdAndStatus(taskId, AssignmentStatus.COMPLETED)
            .orElseThrow(() -> new ResourceNotFoundException("No completed assignment found for task " + taskId));
        return confirm(assignment.getId(), currentUser);
    }

    // --- Helpers ---

    private Assignment getAssignmentOrThrow(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Assignment", assignmentId));
    }

    private void validateAssignmentAccess(Assignment assignment, User currentUser) {
        boolean isGiver = assignment.getTask().getGiver().getId().equals(currentUser.getId());
        boolean isTaker = assignment.getTaker().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == edu.info5100.questapp.user.Role.ADMIN;
        if (!isGiver && !isTaker && !isAdmin) {
            throw new BadRequestException("Access denied");
        }
    }
}
