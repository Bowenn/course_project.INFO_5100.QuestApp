package edu.info5100.questapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request body for PUT /api/users/me.
 * All fields are optional (null = keep current value).
 * currentPassword is required only when newPassword is provided.
 */
public record UpdateProfileRequest(
    @Size(min = 2, max = 50)
    String username,

    @Email
    String email,

    @Size(min = 6)
    String newPassword,

    String currentPassword
) {}
