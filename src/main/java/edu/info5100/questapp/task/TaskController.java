package edu.info5100.questapp.task;

import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.task.dto.CreateTaskRequest;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task CRUD and lifecycle endpoints.
 * All endpoints require authentication.
 * USERs can create, view, and accept tasks.
 * ADMIN can additionally delete any task.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * POST /api/tasks
     * Create a new task in DRAFT status. USER only.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.create(request, securityUser.getUser()));
    }

    /**
     * GET /api/tasks
     * List tasks: USER sees own + accepted tasks; ADMIN sees all.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TaskResponse>> list(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.list(securityUser.getUser()));
    }

    /**
     * GET /api/tasks/published
     * List all published tasks available to accept.
     */
    @GetMapping("/published")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<TaskResponse>> listPublished() {
        return ResponseEntity.ok(taskService.listPublished());
    }

    /**
     * GET /api/tasks/{id}
     * Get task by ID. Access: task owner, assigned user, or ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.getById(id, securityUser.getUser()));
    }

    /**
     * PUT /api/tasks/{id}
     * Update task. Only DRAFT; only by the task owner.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.update(id, request, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/publish
     * Publish task (DRAFT → PUBLISHED). Task owner only.
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> publish(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.publish(id, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/accept
     * Accept (self-assign) a published task. Any USER except the task owner.
     */
    @PostMapping("/{id}/accept")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TaskResponse> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.accept(id, securityUser.getUser()));
    }

    /**
     * POST /api/tasks/{id}/cancel
     * Cancel task. Task owner or ADMIN. Cannot cancel COMPLETED tasks.
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TaskResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.cancel(id, securityUser.getUser()));
    }

    /**
     * DELETE /api/tasks/{id}
     * Permanently delete a task. ADMIN can delete any task; owner can delete their own CANCELLED tasks.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        taskService.delete(id, securityUser.getUser());
        return ResponseEntity.noContent().build();
    }
}
