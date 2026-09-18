package com.hashtag.ngo.example.bank.bean;

import javax.crypto.SecretKey;

/**
 * Derivation de cles de chiffrement a partir d'un secret maitre et d'un sel.
 */
public interface KeyDerivationService {

    /**
     * Derive une cle symetrique de {@code keyLengthBytes} octets a partir de
     * {@code masterSecret} et {@code salt}.
     */
    SecretKey deriveKey(byte[] masterSecret, byte[] salt, int keyLengthBytes);
}
