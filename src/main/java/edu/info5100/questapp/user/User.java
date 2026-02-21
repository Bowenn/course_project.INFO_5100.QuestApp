package edu.info5100.questapp.user;

import edu.info5100.questapp.assignment.Assignment;
import edu.info5100.questapp.task.Task;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity. Each user has one primary role (GIVER, TAKER, or ADMIN).
 * A user can have multiple roles in the future by extending to a join table.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true)
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt-hashed password; never expose in API responses */
    @NotBlank
    @Size(min = 6)
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    /** Tasks created by this user (when role is GIVER) */
    @OneToMany(mappedBy = "giver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasksGiven = new ArrayList<>();

    /** Assignments where this user is the taker */
    @OneToMany(mappedBy = "taker", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Assignment> assignmentsTaken = new ArrayList<>();

    // --- Constructors ---

    public User() {
    }

    public User(String username, String email, String passwordHash, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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

    public List<Task> getTasksGiven() {
        return tasksGiven;
    }

    public void setTasksGiven(List<Task> tasksGiven) {
        this.tasksGiven = tasksGiven;
    }

    public List<Assignment> getAssignmentsTaken() {
        return assignmentsTaken;
    }

    public void setAssignmentsTaken(List<Assignment> assignmentsTaken) {
        this.assignmentsTaken = assignmentsTaken;
    }
}
