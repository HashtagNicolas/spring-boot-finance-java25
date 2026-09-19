package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Acces CRUD Spring Data JPA sur la hierarchie des comptes.
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Lit un compte en posant un verrou pessimiste en ecriture (equivalent
     * SQL : {@code SELECT ... FOR UPDATE}) sur la ligne correspondante.
     * <p>
     * A utiliser pour toute lecture qui precede une modification du solde
     * (depot, retrait, interets) : voir AccountServiceImpl pour le detail de
     * la raison (deux threads virtuels visant le meme compte doivent se
     * serialiser, pas lire-modifier-ecrire en parallele).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
