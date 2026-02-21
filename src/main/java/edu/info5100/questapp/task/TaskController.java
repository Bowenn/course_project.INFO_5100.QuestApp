package edu.info5100.questapp.task;

import edu.info5100.questapp.assignment.dto.AssignTaskRequest;
import edu.info5100.questapp.security.SecurityUser;
import edu.info5100.questapp.task.dto.CreateTaskRequest;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.task.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task CRUD and lifecycle endpoints.
 * All endpoints require authentication. Role-based access enforced in service.
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
     * POST /api/tasks/{id}/cancel
     * Cancel task. GIVER or ADMIN. Cannot cancel COMPLETED.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TaskResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(taskService.cancel(id, securityUser.getUser()));
    }
}
