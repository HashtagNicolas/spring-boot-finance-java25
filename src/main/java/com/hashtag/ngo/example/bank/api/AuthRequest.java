package com.hashtag.ngo.example.bank.api;

/**
 * Corps de requete pour l'authentification (POST /auth/token).
 */
public record AuthRequest(String username, String password) {
}
