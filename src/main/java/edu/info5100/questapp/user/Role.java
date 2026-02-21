package edu.info5100.questapp.user;

/**
 * User roles in the task system.
 * <ul>
 *   <li>GIVER - Creates tasks and directly assigns them to takers</li>
 *   <li>TAKER - Receives assigned tasks and completes them</li>
 *   <li>ADMIN - Manages users, tasks, and assignments system-wide</li>
 * </ul>
 */
public enum Role {
    GIVER,
    TAKER,
    ADMIN
}
