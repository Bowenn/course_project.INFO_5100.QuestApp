package edu.info5100.questapp.task;

import edu.info5100.questapp.assignment.Assignment;
import edu.info5100.questapp.assignment.AssignmentRepository;
import edu.info5100.questapp.assignment.AssignmentStatus;
import edu.info5100.questapp.exception.BadRequestException;
import edu.info5100.questapp.exception.ResourceNotFoundException;
import edu.info5100.questapp.task.dto.CreateTaskRequest;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.task.dto.UpdateTaskRequest;
import edu.info5100.questapp.user.Role;
import edu.info5100.questapp.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * Service for task CRUD and lifecycle (publish, accept, cancel, delete).
 * Any USER can create and accept tasks. Only the task owner or ADMIN can cancel.
 * Only ADMIN can delete tasks.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AssignmentRepository assignmentRepository;

    public TaskService(TaskRepository taskRepository,
                       AssignmentRepository assignmentRepository) {
        this.taskRepository = taskRepository;
        this.assignmentRepository = assignmentRepository;
    }

    /**
     * Create a new task in DRAFT status. Any USER can create.
     */
    @Transactional
    public TaskResponse create(CreateTaskRequest request, User owner) {
        var task = new Task(request.title(), request.description(), owner);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Update task title/description. Only DRAFT tasks; only by the task owner.
     */
    @Transactional
    public TaskResponse update(Long taskId, UpdateTaskRequest request, User currentUser) {
        var task = getTaskAndValidateOwner(taskId, currentUser);
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
     * Publish task (DRAFT → PUBLISHED). Only the task owner can publish.
     */
    @Transactional
    public TaskResponse publish(Long taskId, User currentUser) {
        var task = getTaskAndValidateOwner(taskId, currentUser);
        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT tasks can be published");
        }
        task.setStatus(TaskStatus.PUBLISHED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Accept a published task (self-assign). Any USER except the task owner can accept.
     * Task must be PUBLISHED and not already assigned.
     */
    @Transactional
    public TaskResponse accept(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BadRequestException("Only PUBLISHED tasks can be accepted");
        }
        if (task.getGiver().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Cannot accept your own task");
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
     * Cancel task. Task owner or ADMIN. Cannot cancel COMPLETED or already CANCELLED tasks.
     * Automatically declines any active assignment so takers cannot progress a cancelled task.
     */
    @Transactional
    public TaskResponse cancel(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        boolean isOwner = task.getGiver().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new BadRequestException("Only the task owner or admin can cancel");
        }
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BadRequestException("Completed tasks cannot be cancelled");
        }
        if (task.getStatus() == TaskStatus.CANCELLED) {
            throw new BadRequestException("Task is already cancelled");
        }
        // Once work has started the owner can no longer cancel (only ADMIN can)
        if (!isAdmin && task.getStatus() == TaskStatus.IN_PROGRESS) {
            throw new BadRequestException("Cannot cancel a task that is already in progress");
        }
        // Decline any active assignment so the taker cannot progress it further
        assignmentRepository.findByTaskAndStatusNot(task, AssignmentStatus.DECLINED)
            .ifPresent(a -> {
                a.setStatus(AssignmentStatus.DECLINED);
                assignmentRepository.save(a);
            });
        task.setStatus(TaskStatus.CANCELLED);
        task = taskRepository.save(task);
        return TaskResponse.from(task);
    }

    /**
     * Delete task permanently. ADMIN only.
     */
    @Transactional
    public void delete(Long taskId) {
        var task = getTaskOrThrow(taskId);
        taskRepository.delete(task);
    }

    /**
     * Get task by ID. Access: task owner, any assigned user, or ADMIN.
     */
    @Transactional(readOnly = true)
    public TaskResponse getById(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        validateTaskAccess(task, currentUser);
        return TaskResponse.from(task);
    }

    /**
     * List tasks for current user:
     * - USER: tasks they created + tasks assigned to them
     * - ADMIN: all tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> list(User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return taskRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(TaskResponse::from).toList();
        }

        // USER: union of created tasks and accepted tasks (no duplicates)
        var result = new LinkedHashSet<Task>();
        result.addAll(taskRepository.findByGiverOrderByCreatedAtDesc(currentUser));
        assignmentRepository.findByTakerOrderByAssignedAtDesc(currentUser).stream()
            .filter(a -> a.getStatus() != AssignmentStatus.DECLINED)
            .map(Assignment::getTask)
            .forEach(result::add);
        return result.stream().map(TaskResponse::from).toList();
    }

    /**
     * List all PUBLISHED tasks. Any authenticated user can browse these.
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> listPublished() {
        return taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.PUBLISHED).stream()
            .map(TaskResponse::from).toList();
    }

    // --- Helpers ---

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    private Task getTaskAndValidateOwner(Long taskId, User currentUser) {
        var task = getTaskOrThrow(taskId);
        if (!task.getGiver().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Only the task owner can perform this action");
        }
        return task;
    }

    private void validateTaskAccess(Task task, User currentUser) {
        boolean isOwner = task.getGiver().getId().equals(currentUser.getId());
        boolean isAssigned = task.getAssignments().stream()
            .anyMatch(a -> a.getTaker().getId().equals(currentUser.getId()));
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAssigned && !isAdmin) {
            throw new BadRequestException("Access denied");
        }
    }
}
