package edu.info5100.questapp.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link User} entities.
 * Used for authentication, user lookup, and admin operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find user by email (unique) for login */
    Optional<User> findByEmail(String email);

    /** Find user by username (unique) */
    Optional<User> findByUsername(String username);

    /** Check if email is already taken */
    boolean existsByEmail(String email);

    /** Check if username is already taken */
    boolean existsByUsername(String username);

    /** Find all users with a specific role (e.g. USER for assignment dropdown) */
    List<User> findByRole(Role role);
}
