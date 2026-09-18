package com.hashtag.ngo.example.bank.bean;

/**
 * Emission et validation de jetons JWT (implementation decouplee via JJWT).
 */
public interface JwtService {

    /** Genere un jeton signe pour le sujet donne (ex : identifiant utilisateur). */
    String generateToken(String subject);

    /** Verifie la signature et l'expiration du jeton. */
    boolean validateToken(String token);

    /** Extrait le sujet (subject) porte par le jeton. */
    String extractSubject(String token);
}
