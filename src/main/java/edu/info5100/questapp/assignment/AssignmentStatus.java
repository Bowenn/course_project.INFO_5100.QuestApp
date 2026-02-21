package edu.info5100.questapp.assignment;

/**
 * Status of a task assignment (direct-assign flow).
 * Flow: ASSIGNED → IN_PROGRESS → COMPLETED
 * Taker can DECLINED to reject an assignment.
 */
public enum AssignmentStatus {
    /** Giver has assigned the task to a taker */
    ASSIGNED,
    /** Taker has started working on the task */
    IN_PROGRESS,
    /** Taker has completed the task */
    COMPLETED,
    /** Taker declined the assignment; task returns to PUBLISHED */
    DECLINED
}
