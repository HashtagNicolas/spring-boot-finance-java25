package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acces CRUD Spring Data JPA sur les transactions.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Requete derivee Spring Data : traduite en "where account_id = ?"
     * (navigue automatiquement jusqu'a l'id du compte associe).
     */
    List<Transaction> findByAccountId(Long accountId);
}
