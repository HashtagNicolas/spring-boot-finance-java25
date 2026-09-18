package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.hibernate.annotations.ConcreteProxy;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Racine de la hierarchie des comptes bancaires.
 * <p>
 * Hierarchie scellee (sealed) : seuls CheckingAccount et SavingsAccount
 * peuvent etendre Account, ce qui garantit au compilateur (et au lecteur)
 * qu'il n'existe pas d'autre type de compte possible.
 * <p>
 * Mapping JPA en SINGLE_TABLE : toutes les sous-classes sont stockees dans
 * une seule table "accounts", differenciees par la colonne de discriminant
 * "account_type". C'est la strategie la plus simple et la plus performante
 * (pas de jointure), au prix de colonnes nullables pour les champs propres
 * a chaque sous-type (voir overdraft_limit / interest_rate).
 */
@Entity
@Table(name = "accounts")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
// @ConcreteProxy (Hibernate 7) : obligatoire ici. Hibernate cree normalement des
// proxies "lazy" en generant une sous-classe bytecode de l'entite. Mais nos
// sous-classes concretes (CheckingAccount, SavingsAccount) sont "final" (imposees
// par la hierarchie scellee), donc impossibles a sous-classer davantage : Hibernate
// ne peut plus generer de proxy classique pour cette hierarchie. @ConcreteProxy
// indique a Hibernate d'utiliser a la place un proxy base sur l'instance concrete
// elle-meme (via l'enhancement bytecode), ce qui reste compatible avec des
// classes scellees/final.
@ConcreteProxy
public abstract sealed class Account permits CheckingAccount, SavingsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Constructeur exige par la specification JPA (instanciation par reflexion). */
    protected Account() {
    }

    protected Account(String owner, BigDecimal balance) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("Le titulaire du compte est obligatoire");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Le solde initial est obligatoire");
        }
        this.owner = owner;
        this.balance = balance;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Reserve aux sous-classes (ex : SavingsAccount#applyInterest). */
    protected void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
