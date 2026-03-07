package edu.info5100.questapp.user.dto;

/**
 * Response body for POST /api/auth/login.
 * Contains the JWT token to use in subsequent requests.
 */
public record LoginResponse(
    String token,
    String type,
    String email,
    String role
) {
    public LoginResponse(String token, String email, String role) {
        this(token, "Bearer", email, role);
    }
}
