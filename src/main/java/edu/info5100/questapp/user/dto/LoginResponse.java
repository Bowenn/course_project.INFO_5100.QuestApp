package edu.info5100.questapp.user.dto;

/**
 * Response for login endpoint.
 */
public record LoginResponse(String token, UserResponse user) {
}