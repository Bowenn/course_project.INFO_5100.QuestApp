package edu.info5100.questapp.assignment.dto;

import java.util.List;

/**
 * Grouped assignment list for the current user.
 * Separates assignments where the user is the task owner vs the acceptor.
 */
public record AssignmentListResponse(
    /** Assignments for tasks this user created */
    List<AssignmentResponse> asOwner,

    /** Assignments for tasks this user accepted */
    List<AssignmentResponse> asTaker
) {}
