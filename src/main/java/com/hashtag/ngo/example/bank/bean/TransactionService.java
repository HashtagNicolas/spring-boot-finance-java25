package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Transaction;
import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Enregistrement et consultation des transactions (depots/retraits), avec
 * tracabilite de l'auteur via le contexte d'audit courant (voir
 * AuditContextHolder).
 */
public interface TransactionService {

    /** Applique l'operation sur le compte (via AccountService) et trace la transaction. */
    Transaction recordTransaction(TransactionType type, Long accountId, BigDecimal amount);

    /** Historique complet des transactions d'un compte, du plus ancien au plus recent. */
    List<Transaction> getHistory(Long accountId);

    /** Traite un lot d'operations, potentiellement en parallele (threads virtuels). */
    List<Transaction> processBatch(List<TransactionCommand> commands);
}
