package edu.info5100.questapp.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for login.
 * Used by POST /api/auth/login
 */
public record LoginRequest(
    @NotBlank
    String email,

    @NotBlank
    String password
) {}
