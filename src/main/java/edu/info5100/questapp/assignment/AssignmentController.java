package edu.info5100.questapp.assignment;

import edu.info5100.questapp.assignment.dto.AssignmentListResponse;
import edu.info5100.questapp.assignment.dto.AssignmentResponse;
import edu.info5100.questapp.assignment.dto.UpdateAssignmentRequest;
import edu.info5100.questapp.security.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Assignment endpoints.
 * Assigned user updates status: ASSIGNED → IN_PROGRESS → COMPLETED, or DECLINED.
 */
@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    /**
     * GET /api/assignments
     * Returns grouped: { asOwner: [...], asTaker: [...] }. ADMIN sees all in both lists.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AssignmentListResponse> list(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.list(securityUser.getUser()));
    }

    /**
     * GET /api/assignments/{id}
     * Get assignment by ID. Access: task owner, assigned user, or ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AssignmentResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.getById(id, securityUser.getUser()));
    }

    /**
     * PUT /api/assignments/{id}
     * Assigned user updates status: IN_PROGRESS, COMPLETED, or DECLINED.
     * Only the assigned user can update.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AssignmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssignmentRequest request,
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ResponseEntity.ok(assignmentService.update(id, request, securityUser.getUser()));
    }
}
