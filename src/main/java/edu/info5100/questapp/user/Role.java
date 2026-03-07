package edu.info5100.questapp.user;

/**
 * User roles in the system.
 * <ul>
 *   <li>USER  - Regular user; can create tasks, accept tasks, and complete assignments</li>
 *   <li>ADMIN - System administrator; can manage all tasks and users</li>
 * </ul>
 * Roles are assigned by the system, not chosen by users at registration.
 */
public enum Role {
    USER,
    ADMIN
}
