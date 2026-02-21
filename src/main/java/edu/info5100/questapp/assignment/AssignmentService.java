package edu.info5100.questapp.assignment;

import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.task.Task;
import edu.info5100.questapp.task.TaskRepository;
import edu.info5100.questapp.task.TaskStatus;
import edu.info5100.questapp.assignment.dto.AssignmentResponse;
import edu.info5100.questapp.assignment.dto.UpdateAssignmentRequest;
import edu.info5100.questapp.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for assignment lifecycle. Taker updates status: ASSIGNED → IN_PROGRESS → COMPLETED,
 * or DECLINED to reject.
 */
@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, TaskRepository taskRepository) {
        this.assignmentRepository = assignmentRepository;
        this.taskRepository = taskRepository;
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
