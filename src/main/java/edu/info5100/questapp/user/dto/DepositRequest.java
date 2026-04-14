package edu.info5100.questapp.user.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request body for depositing balance.
 * Used by POST /api/users/deposit
 */
public record DepositRequest(
    @Positive(message = "Amount must be greater than 0")
    Double amount
) {}
