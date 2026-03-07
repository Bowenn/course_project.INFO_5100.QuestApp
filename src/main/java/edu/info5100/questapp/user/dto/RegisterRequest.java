package edu.info5100.questapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for user registration.
 * Used by POST /api/auth/register
 * Role is NOT included — all new users are automatically assigned USER role.
 */
public record RegisterRequest(
    @NotBlank @Size(min = 2, max = 50)
    String username,

    @NotBlank @Email
    String email,

    @NotBlank @Size(min = 6)
    String password
) {}
