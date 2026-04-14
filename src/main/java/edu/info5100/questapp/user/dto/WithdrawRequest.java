package edu.info5100.questapp.user.dto;

import jakarta.validation.constraints.Positive;

/**
 * Request body for withdrawing balance.
 * Used by POST /api/users/withdraw
 */
public record WithdrawRequest(
    @Positive(message = "Amount must be greater than 0")
    Double amount
) {}
