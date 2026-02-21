package edu.info5100.questapp.assignment.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for assigning a task to a taker (direct assign).
 * Used by POST /api/tasks/{taskId}/assign
 */
public record AssignTaskRequest(
    /** ID of the user (TAKER) to assign the task to */
    @NotNull
    Long takerId
) {}
