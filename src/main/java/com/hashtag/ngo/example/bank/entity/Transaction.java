package com.hashtag.ngo.example.bank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Trace d'un mouvement (depot ou retrait) sur un compte, a des fins de
 * tracabilite des operations.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Instant timestamp;

    // EAGER : on veut systematiquement connaitre le compte associe a une
    // transaction des qu'on la charge (ecrans/rapports de tracabilite),
    // plutot que de multiplier les allers-retours en base.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** Constructeur exige par la specification JPA. */
    protected Transaction() {
    }

    public Transaction(TransactionType type, BigDecimal amount, Account account) {
        this.type = type;
        this.amount = amount;
        this.account = account;
        this.timestamp = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Account getAccount() {
        return account;
    }
}
