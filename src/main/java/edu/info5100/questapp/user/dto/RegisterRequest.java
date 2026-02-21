package edu.info5100.questapp.user.dto;

import edu.info5100.questapp.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for user registration.
 * Used by POST /api/auth/register
 */
public record RegisterRequest(
    @NotBlank @Size(min = 2, max = 50)
    String username,

    @NotBlank @Email
    String email,

    @NotBlank @Size(min = 6)
    String password,

    @NotNull
    Role role
) {}
