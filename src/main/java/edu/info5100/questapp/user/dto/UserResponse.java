package edu.info5100.questapp.user.dto;

import edu.info5100.questapp.user.Role;
import edu.info5100.questapp.user.User;

import java.time.Instant;

/**
 * User data returned in API responses. Excludes password.
 * Used for GET /api/users/me, user lists, etc.
 */
public record UserResponse(
    Long id,
    String username,
    String email,
    Role role,
    Double balance,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getBalance(),
            user.getCreatedAt()
        );
    }
}
