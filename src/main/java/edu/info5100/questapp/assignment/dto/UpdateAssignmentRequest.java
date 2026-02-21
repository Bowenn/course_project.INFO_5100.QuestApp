package edu.info5100.questapp.assignment.dto;

import edu.info5100.questapp.assignment.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for taker to update assignment status (start, complete, decline).
 * Used by PUT /api/assignments/{id}
 */
public record UpdateAssignmentRequest(
    @NotNull
    AssignmentStatus status,

    /** Optional note when completing or declining */
    @Size(max = 500)
    String note
) {}
