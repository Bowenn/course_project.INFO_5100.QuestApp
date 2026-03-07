package edu.info5100.questapp.assignment;

/**
 * Status of a task assignment (self-assign flow).
 * Flow: ASSIGNED → IN_PROGRESS → COMPLETED
 * Assigned user can DECLINE to reject an assignment.
 */
public enum AssignmentStatus {
    /** A user has accepted (self-assigned) the task */
    ASSIGNED,
    /** Assigned user has started working on the task */
    IN_PROGRESS,
    /** Assigned user has completed the task */
    COMPLETED,
    /** Assigned user declined; task returns to PUBLISHED */
    DECLINED
}
