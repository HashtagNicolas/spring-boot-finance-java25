package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acces CRUD Spring Data JPA sur les transactions.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
