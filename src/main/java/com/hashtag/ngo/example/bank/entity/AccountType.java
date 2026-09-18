package com.hashtag.ngo.example.bank.entity;

/**
 * Types de comptes geres par l'API.
 * <p>
 * Les noms de ces constantes correspondent volontairement aux valeurs du
 * discriminant JPA declarees via @DiscriminatorValue sur CheckingAccount et
 * SavingsAccount (colonne "account_type"), afin de garder une seule source
 * de verite lisible pour les autres couches (DTO, mapping, etc.) qui
 * viendront s'appuyer sur cet enum plus tard.
 */
public enum AccountType {
    COURANT,
    EPARGNE
}
