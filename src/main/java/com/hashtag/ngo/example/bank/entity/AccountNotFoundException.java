package com.hashtag.ngo.example.bank.entity;

/**
 * Levee lorsqu'aucun compte ne correspond a l'identifiant demande.
 */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(String message) {
        super(message);
    }
}
