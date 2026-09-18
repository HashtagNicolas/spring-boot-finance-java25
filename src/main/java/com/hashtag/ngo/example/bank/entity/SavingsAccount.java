package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte epargne : produit des interets au taux interestRate.
 */
@Entity
@DiscriminatorValue("EPARGNE")
public final class SavingsAccount extends Account {

    /**
     * Nullable en base (SINGLE_TABLE) : cette colonne n'a de sens que pour les
     * comptes epargne, elle reste vide pour les lignes de comptes courants.
     */
    @Column(name = "interest_rate", precision = 6, scale = 4)
    private BigDecimal interestRate;

    /** Constructeur exige par la specification JPA. */
    protected SavingsAccount() {
        super();
    }

    public SavingsAccount(String owner, BigDecimal balance, BigDecimal interestRate) {
        // Nouveaute Java 25 (JEP 513, "Flexible Constructor Bodies"), voir le
        // commentaire equivalent dans CheckingAccount : on valide le taux
        // d'interet AVANT l'appel a super(...), avant que l'objet ne soit
        // construit, pour interdire tout SavingsAccount avec un taux negatif.
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Le taux d'interet doit etre >= 0");
        }
        super(owner, balance);
        this.interestRate = interestRate;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    /**
     * Applique les interets au solde courant : balance += balance * interestRate.
     */
    public void applyInterest() {
        setBalance(getBalance().add(getBalance().multiply(interestRate)));
    }
}
