package edu.info5100.questapp.task.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for updating a task (partial update).
 * Used by PUT /api/tasks/{id}
 * Only DRAFT tasks can be updated.
 */
public record UpdateTaskRequest(
    @Size(max = 200)
    String title,

    @Size(max = 2000)
    String description
) {}
