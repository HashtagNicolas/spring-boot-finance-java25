package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountType;

import java.math.BigDecimal;

/**
 * Corps de requete pour la creation d'un compte (POST /accounts).
 * <p>
 * {@code overdraftLimit} n'est utilise que si {@code type == COURANT},
 * {@code interestRate} que si {@code type == EPARGNE} (voir AccountController).
 */
public record AccountRequest(
        AccountType type,
        String owner,
        BigDecimal initialBalance,
        BigDecimal overdraftLimit,
        BigDecimal interestRate) {
}
