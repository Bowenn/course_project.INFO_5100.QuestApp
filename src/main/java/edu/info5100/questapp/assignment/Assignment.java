package edu.info5100.questapp.assignment;

import edu.info5100.questapp.task.Task;
import edu.info5100.questapp.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Assignment entity. Represents a direct assignment of a task from a GIVER to a TAKER.
 * One task can have multiple assignments over time (e.g. if taker declines and giver reassigns).
 */
@Entity
@Table(name = "assignments", indexes = {
    @Index(name = "idx_assignment_task", columnList = "task_id"),
    @Index(name = "idx_assignment_taker", columnList = "taker_id")
})
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** The user assigned to complete the task (TAKER) */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taker_id", nullable = false)
    private User taker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(nullable = false, updatable = false)
    private Instant assignedAt = Instant.now();

    /** When taker marked the task complete (null if not yet completed) */
    private Instant completedAt;

    /** Optional note from taker when completing or declining */
    @Column(length = 500)
    private String note;

    // --- Constructors ---

    public Assignment() {
    }

    public Assignment(Task task, User taker) {
        this.task = task;
        this.taker = taker;
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getTaker() {
        return taker;
    }

    public void setTaker(User taker) {
        this.taker = taker;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
