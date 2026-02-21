package edu.info5100.questapp.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new task.
 * Used by POST /api/tasks
 */
public record CreateTaskRequest(
    @NotBlank @Size(max = 200)
    String title,

    @Size(max = 2000)
    String description
) {}
