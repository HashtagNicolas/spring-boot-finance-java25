package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

/**
 * Compte courant : autorise un decouvert jusqu'a overdraftLimit.
 */
@Entity
@DiscriminatorValue("COURANT")
public final class CheckingAccount extends Account {

    /**
     * Nullable en base (SINGLE_TABLE) : cette colonne n'a de sens que pour les
     * comptes courants, elle reste vide pour les lignes de comptes epargne.
     */
    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    /** Constructeur exige par la specification JPA. */
    protected CheckingAccount() {
        super();
    }

    public CheckingAccount(String owner, BigDecimal balance, BigDecimal overdraftLimit) {
        // Nouveaute Java 25 (JEP 513, "Flexible Constructor Bodies") : on peut
        // desormais executer des instructions AVANT l'appel a super(...), tant
        // qu'elles ne lisent/ecrivent pas l'etat de l'instance en construction
        // (this). Cela permet de valider les arguments du constructeur avant
        // meme que la partie "Account" de l'objet n'existe, et donc de garantir
        // qu'un CheckingAccount invalide (decouvert negatif) ne peut jamais
        // etre observe, meme partiellement construit.
        if (overdraftLimit == null || overdraftLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le decouvert autorise doit etre >= 0");
        }
        super(owner, balance);
        this.overdraftLimit = overdraftLimit;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }
}
