package edu.info5100.questapp.task;

import edu.info5100.questapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link Task} entities.
 * Supports queries by giver, status, and combined filters.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /** All tasks created by a specific giver */
    List<Task> findByGiverOrderByCreatedAtDesc(User giver);

    /** Tasks by status (e.g. PUBLISHED for takers to see) */
    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    /** Tasks by giver and status */
    List<Task> findByGiverAndStatusOrderByCreatedAtDesc(User giver, TaskStatus status);
}
