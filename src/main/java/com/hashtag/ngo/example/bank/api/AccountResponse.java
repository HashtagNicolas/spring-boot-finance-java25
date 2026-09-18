package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representation exposee d'un compte, quel que soit son type concret.
 * <p>
 * {@code overdraftLimit} n'est renseigne que pour un compte courant,
 * {@code interestRate} que pour un compte epargne (l'autre champ vaut null).
 */
public record AccountResponse(
        Long id,
        AccountType type,
        String owner,
        BigDecimal balance,
        Instant createdAt,
        BigDecimal overdraftLimit,
        BigDecimal interestRate) {
}
