package com.hashtag.ngo.example.bank.entity;

/**
 * Levee lorsqu'un retrait ferait passer le solde sous le plancher autorise
 * pour le type de compte (decouvert pour un compte courant, zero pour un
 * compte epargne).
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
