package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.TransactionType;

import java.math.BigDecimal;

/**
 * Une operation unitaire a traiter dans un lot (voir TransactionService#processBatch).
 */
public record TransactionCommand(TransactionType type, Long accountId, BigDecimal amount) {
}
