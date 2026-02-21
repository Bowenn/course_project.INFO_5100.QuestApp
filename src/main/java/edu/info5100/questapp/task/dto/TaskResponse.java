package edu.info5100.questapp.task.dto;

import edu.info5100.questapp.task.Task;
import edu.info5100.questapp.task.TaskStatus;
import edu.info5100.questapp.user.dto.UserResponse;

import java.time.Instant;

/**
 * Task data returned in API responses.
 */
public record TaskResponse(
    Long id,
    String title,
    String description,
    TaskStatus status,
    UserResponse giver,
    Instant createdAt,
    Instant updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            UserResponse.from(task.getGiver()),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
