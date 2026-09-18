package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;

/**
 * Corps de requete pour enregistrer une transaction.
 * <p>
 * Utilise tel quel par TransactionController (POST /accounts/{id}/transactions,
 * ou {@code type} distingue DEPOT/RETRAIT). Reutilise egalement par
 * AccountController pour /deposit et /withdraw : le champ {@code type} y est
 * alors ignore puisque l'operation est deja determinee par l'URL.
 */
public record TransactionRequest(TransactionType type, BigDecimal amount) {
}
