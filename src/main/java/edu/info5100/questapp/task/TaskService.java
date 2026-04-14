package edu.info5100.questapp.task;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.info5100.questapp.assignment.Assignment;
import edu.info5100.questapp.assignment.AssignmentRepository;
import edu.info5100.questapp.assignment.AssignmentStatus;
import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.task.dto.CreateTaskRequest;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.task.dto.UpdateTaskRequest;
import edu.info5100.questapp.user.User;
import edu.info5100.questapp.user.UserService;

/**
 * Service for task CRUD and lifecycle (publish, assign, cancel).
 * Enforces role-based rules: only giver can manage own tasks.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository,
                       AssignmentRepository assignmentRepository,
                       UserService userService) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
        this.userService = userService;
    }

    /**
     * Create a new task in DRAFT status. Only GIVER.
     */
    @Transactional
    public TaskResponse create(CreateTaskRequest request, User giver) {
        Double bounty = request.bounty() != null ? request.bounty() : 0.0;
        if (bounty < 0) {
            throw new BadRequestException("Bounty cannot be negative");
        }
        if (giver.getBalance() < bounty) {
            throw new BadRequestException("Insufficient balance for bounty");
        }
        // Deduct bounty from giver's balance
        giver.setBalance(giver.getBalance() - bounty);
        var task = new Task(request.title(), request.description(), bounty, giver);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Update task. Only DRAFT tasks can be updated; only by the giver.
     */
    @Transactional
    public TaskResponse update(Long taskId, UpdateTaskRequest request, User currentUser) {
        var task = getTaskAndValidateGiver(taskId, currentUser);
        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT tasks can be updated");
        }
        if (request.title() != null && !request.title().isBlank()) {
            task.setTitle(request.title());
        }
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Publish task (DRAFT → PUBLISHED). Makes it visible to takers.
     */
    @Transactional
    public TaskResponse publish(Long taskId, User currentUser) {
        var task = getTaskAndValidateGiver(taskId, currentUser);
        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT tasks can be published");
        }
        task.setStatus(TaskStatus.PUBLISHED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Assign task to a taker (direct assign). Task must be PUBLISHED.
     */
    @Transactional
    public TaskResponse assign(Long taskId, Long takerId, User currentUser) {
        var task = getTaskAndValidateGiver(taskId, currentUser);
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED tasks can be assigned");
        }
        if (assignmentRepository.findByTaskAndStatusNot(task, AssignmentStatus.DECLINED).isPresent()) {
            throw new BadRequestException("Task is already assigned");
        }

        var taker = userService.getById(takerId);
        if (taker.getRole() != edu.info5100.questapp.user.Role.TAKER) {
            throw new BadRequestException("Target user must be a TAKER");
        }

        var assignment = new Assignment(task, taker);
        assignmentRepository.save(assignment);
        task.setStatus(TaskStatus.ASSIGNED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Accept a published task. Only TAKER can do this, and task must be PUBLISHED.
     */
    @Transactional
    public TaskResponse accept(Long taskId, User currentUser) {
        if (currentUser.getRole() != edu.info5100.questapp.user.Role.TAKER) {
            throw new BadRequestException("Only a TAKER can accept tasks");
        }

        var task = getTaskOrThrow(taskId);
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED tasks can be accepted");
        }
        if (assignmentRepository.findByTaskAndStatusNot(task, AssignmentStatus.DECLINED).isPresent()) {
            throw new BadRequestException("Task is already assigned");
        }

        var assignment = new Assignment(task, currentUser);
        assignmentRepository.save(assignment);
        task.setStatus(TaskStatus.ASSIGNED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Cancel task. Giver or Admin. Task cannot be COMPLETED.
     */
    @Transactional
    public TaskResponse cancel(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        boolean isGiver = task.getGiver().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == edu.info5100.questapp.user.Role.ADMIN;
        if (!isGiver && !isAdmin) {
            throw new BadRequestException("Only giver or admin can cancel");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BadRequestException("Completed tasks cannot be cancelled");
        }
        task.setStatus(TaskStatus.CANCELLED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Delete task. ADMIN or task giver for DRAFT/CANCELLED tasks.
     */
    @Transactional
    public void delete(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        boolean isAdmin = currentUser.getRole() == edu.info5100.questapp.user.Role.ADMIN;
        boolean isGiverAndDeletable = task.getGiver().getId().equals(currentUser.getId()) &&
            (task.getStatus() == TaskStatus.DRAFT || task.getStatus() == TaskStatus.CANCELLED);
        if (!isAdmin && !isGiverAndDeletable) {
            throw new BadRequestException("Only admin or task giver can delete draft/cancelled tasks");
        }
        // Delete all related assignments first
        assignmentRepository.deleteAll(task.getAssignments());
        // Then delete the task
        taskRepository.delete(task);
    }

    /**
     * Get task by ID. Access: giver, assigned taker, or admin.
     */
    public TaskResponse getById(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        validateTaskAccess(task, currentUser);
        return TaskResponse.from(task);
    }

    /**
     * List tasks for current user based on role:
     * - GIVER: own tasks
     * - TAKER: tasks assigned to me (from assignments)
     * - ADMIN: all tasks
     */
    public List<TaskResponse> list(User currentUser) {
        return switch (currentUser.getRole()) {
            case GIVER -> taskRepository.findByGiverOrderByCreatedAtDesc(currentUser).stream()
                .map(TaskResponse::from).toList();
            case TAKER -> assignmentRepository.findByTakerOrderByAssignedAtDesc(currentUser).stream()
                .filter(a -> a.getStatus() != AssignmentStatus.DECLINED)
                .map(a -> TaskResponse.from(a.getTask()))
                .toList();
            case ADMIN -> taskRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(TaskResponse::from).toList();
        };
    }

    /**
     * List published tasks (for takers to browse). Admin can also use.
     */
    public List<TaskResponse> listPublished(User currentUser) {
        return taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PUBLISHED).stream()
            .map(TaskResponse::from).toList();
    }

    // --- Helpers ---

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private Task getTaskAndValidateGiver(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        if (!task.getGiver().getId().equals(currentUser.getId()) && currentUser.getRole() != edu.info5100.questapp.user.Role.ADMIN) {
            throw new BadRequestException("Only the giver or admin can perform this action");
        }
        return task;
    }

    private void validateTaskAccess(Task task, User currentUser) {
        boolean isGiver = task.getGiver().getId().equals(currentUser.getId());
        boolean isTaker = task.getAssignments().stream()
            .anyMatch(a -> a.getTaker().getId().equals(currentUser.getId()));
        boolean isAdmin = currentUser.getRole() == edu.info5100.questapp.user.Role.ADMIN;
        if (!isGiver && !isTaker && !isAdmin) {
            throw new BadRequestException("Access denied");
        }
    }
}
