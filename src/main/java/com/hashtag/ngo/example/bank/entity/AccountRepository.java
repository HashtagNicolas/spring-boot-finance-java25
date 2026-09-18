package com.hashtag.ngo.example.bank.entity;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acces CRUD Spring Data JPA sur la hierarchie des comptes.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
}
