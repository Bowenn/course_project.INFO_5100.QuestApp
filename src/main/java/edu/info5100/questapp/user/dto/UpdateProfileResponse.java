package edu.info5100.questapp.user.dto;

/**
 * Response for PUT /api/users/me.
 * Always returns a fresh JWT token so the frontend can handle email changes gracefully.
 */
public record UpdateProfileResponse(UserResponse user, String token) {}
