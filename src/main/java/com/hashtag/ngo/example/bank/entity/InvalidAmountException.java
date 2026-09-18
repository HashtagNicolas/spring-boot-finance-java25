package com.hashtag.ngo.example.bank.entity;

/**
 * Levee lorsqu'un montant (solde initial, decouvert, taux d'interet, depot,
 * retrait...) est manquant ou invalide (ex : negatif alors qu'il doit etre >= 0).
 */
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
