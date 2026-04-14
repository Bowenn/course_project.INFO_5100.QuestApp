package edu.info5100.questapp.task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import edu.info5100.questapp.assignment.Assignment;
import edu.info5100.questapp.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Task entity. Created by a GIVER, assigned to a TAKER via {@link Assignment}.
 * Status flows: DRAFT → PUBLISHED → ASSIGNED → IN_PROGRESS → COMPLETED.
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_giver", columnList = "giver_id")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String description;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2) DEFAULT 0.00")
    private Double bounty = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.DRAFT;

    /** The user who created and owns this task (GIVER) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giver_id", nullable = false)
    private User giver;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    /** Assignments for this task (typically one active; history if reassigned) */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignments = new ArrayList<>();

    // --- Constructors ---

    public Task() {
    }

    public Task(String title, String description, Double bounty, User giver) {
        this.title = title;
        this.description = description;
        this.bounty = bounty;
        this.giver = giver;
    }

    // --- Lifecycle ---

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Getters / Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getBounty() {
        return bounty;
    }

    public void setBounty(Double bounty) {
        this.bounty = bounty;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public User getGiver() {
        return giver;
    }

    public void setGiver(User giver) {
        this.giver = giver;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
}
