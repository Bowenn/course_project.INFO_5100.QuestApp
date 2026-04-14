package edu.info5100.questapp.task;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.info5100.questapp.assignment.AssignmentService;
import edu.info5100.questapp.assignment.dto.AssignmentResponse;
import edu.info5100.questapp.assignment.dto.AssignTaskRequest;
import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.task.dto.CreateTaskRequest;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;

/**
 * Task CRUD and lifecycle endpoints.
 * All endpoints require authentication. Role-based access enforced in service.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final AssignmentService assignmentService;

    public TaskController(TaskService taskService, AssignmentService assignmentService) {
        this.taskService = taskService;
        this.assignmentService = assignmentService;
    }

    /**
     * POST /api/tasks
     * Create a new task in DRAFT status. GIVER only.
     */
    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.create(request, securityUser.getUser()));
    }

    /**
     * GET /api/tasks
     * List tasks based on role: GIVER=own, TAKER=assigned to me, ADMIN=all.
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> list(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.list(securityUser.getUser()));
    }

    /**
     * GET /api/tasks/published
     * List all published tasks (available for assignment). GIVER and ADMIN.
     */
    @GetMapping("/published")
    public ResponseEntity<List<TaskResponse>> listPublished(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.listPublished(securityUser.getUser()));
    }

    /**
     * GET /api/tasks/{id}
     * Get task by ID. Access: giver, assigned taker, or admin.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.getById(id, securityUser.getUser()));
    }

    /**
     * PUT /api/tasks/{id}
     * Update task. Only DRAFT; only by giver.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.update(id, request, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/publish
     * Publish task (DRAFT → PUBLISHED). GIVER only.
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<TaskResponse> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.publish(id, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/accept
     * Accept task from published pool. TAKER only.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<TaskResponse> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.accept(id, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/assign
     * Assign task to a taker (direct assign). Task must be PUBLISHED. GIVER only.
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignTaskRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.assign(id, request.takerId(), securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/confirm
     * Giver confirms task completion and pays bounty to taker.
     * Finds the COMPLETED assignment for the task and confirms it.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AssignmentResponse> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.confirmByTaskId(id, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/cancel
     * Cancel task. GIVER or ADMIN. Cannot cancel COMPLETED.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TaskResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.cancel(id, securityUser.getUser()));
    }

    /**
     * DELETE /api/tasks/{id}
     * Delete task. ADMIN only.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        taskService.delete(id, securityUser.getUser());
        return ResponseEntity.noContent().build();
    }
}
