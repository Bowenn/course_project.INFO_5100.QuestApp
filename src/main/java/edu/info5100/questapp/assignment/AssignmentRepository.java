package edu.info5100.questapp.assignment;

import edu.info5100.questapp.task.Task;
import edu.info5100.questapp.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Assignment} entities.
 * Supports queries by task, taker, and status.
 */
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** All assignments for a task (including history) */
    List<Assignment> findByTaskOrderByAssignedAtDesc(Task task);

    /** All assignments for a taker */
    List<Assignment> findByTakerOrderByAssignedAtDesc(User taker);

    /** Active assignment for a task (not DECLINED); used to check if task is already assigned */
    Optional<Assignment> findByTaskAndStatusNot(Task task, AssignmentStatus status);

    /** Assignments by status for a taker */
    List<Assignment> findByTakerAndStatusOrderByAssignedAtDesc(User taker, AssignmentStatus status);
}
