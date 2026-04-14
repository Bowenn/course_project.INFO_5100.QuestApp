package edu.info5100.questapp.assignment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.info5100.questapp.assignment.dto.AssignmentResponse;
import edu.info5100.questapp.assignment.dto.UpdateAssignmentRequest;
import edu.info5100.questapp.security.SecurityUser;
import jakarta.validation.Valid;

/**
 * Assignment endpoints.
 * Taker updates status: ASSIGNED → IN_PROGRESS → COMPLETED, or DECLINED.
 */
@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * GET /api/assignments
     * List assignments: GIVER=for my tasks, TAKER=mine, ADMIN=all.
     */
    @GetMapping
    public ResponseEntity<List<AssignmentResponse>> list(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.list(securityUser.getUser()));
    }

    /**
     * GET /api/assignments/{id}
     * Get assignment by ID. Access: giver, taker, or admin.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.getById(id, securityUser.getUser()));
    }

    /**
     * PUT /api/assignments/{id}
     * Taker updates status: IN_PROGRESS, COMPLETED, or DECLINED.
     * Only the assigned taker can update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.update(id, request, securityUser.getUser()));
    }

    /**
     * POST /api/assignments/{id}/confirm
     * Giver confirms task completion and transfers bounty to taker.
     * Only the task giver can confirm.
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<AssignmentResponse> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.confirm(id, securityUser.getUser()));
    }
}
