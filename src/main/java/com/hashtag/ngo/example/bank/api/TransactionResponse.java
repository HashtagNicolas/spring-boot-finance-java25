package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representation exposee d'une transaction.
 */
public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        Long accountId) {
}
