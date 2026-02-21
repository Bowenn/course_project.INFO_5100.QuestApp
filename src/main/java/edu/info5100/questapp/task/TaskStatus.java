package edu.info5100.questapp.task;

/**
 * Lifecycle status of a task.
 * Flow: DRAFT → PUBLISHED → ASSIGNED → IN_PROGRESS → COMPLETED
 * Any state except COMPLETED can transition to CANCELLED.
 */
public enum TaskStatus {
    /** Task created but not visible to takers; giver can edit freely */
    DRAFT,
    /** Task is visible to takers; giver can assign to a taker */
    PUBLISHED,
    /** Task has been assigned to a taker */
    ASSIGNED,
    /** Taker is actively working on the task */
    IN_PROGRESS,
    /** Task has been completed by the taker */
    COMPLETED,
    /** Task was cancelled by giver or admin */
    CANCELLED
}
