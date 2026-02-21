package edu.info5100.questapp.assignment.dto;

import edu.info5100.questapp.assignment.Assignment;
import edu.info5100.questapp.assignment.AssignmentStatus;
import edu.info5100.questapp.task.dto.TaskResponse;
import edu.info5100.questapp.user.dto.UserResponse;

import java.time.Instant;

/**
 * Assignment data returned in API responses.
 */
public record AssignmentResponse(
    Long id,
    TaskResponse task,
    UserResponse taker,
    AssignmentStatus status,
    Instant assignedAt,
    Instant completedAt,
    String note
) {
    public static AssignmentResponse from(Assignment assignment) {
        return new AssignmentResponse(
            assignment.getId(),
            TaskResponse.from(assignment.getTask()),
            UserResponse.from(assignment.getTaker()),
            assignment.getStatus(),
            assignment.getAssignedAt(),
            assignment.getCompletedAt(),
            assignment.getNote()
        );
    }
}
