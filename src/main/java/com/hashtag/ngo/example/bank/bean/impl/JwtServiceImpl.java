package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Implementation JJWT du service JWT, volontairement decouplee derriere
 * l'interface JwtService : le reste de l'application (ex : la couche
 * api/securite) depend de JwtService, jamais directement de JJWT.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    private final SecretKey signingKey;

    public JwtServiceImpl(@Value("${app.jwt.secret}") String secret) {
        // Cle HMAC derivee du secret configure (application.yml, propriete
        // app.jwt.secret) : contrairement a une cle generee aleatoirement au
        // demarrage, elle reste stable d'un redemarrage/d'une instance a
        // l'autre, ce qui est indispensable des qu'il y a plusieurs instances
        // de l'application (sinon un jeton emis par l'une serait invalide sur
        // l'autre). Keys.hmacShaKeyFor exige au moins 256 bits (32 octets)
        // pour HS256. Le secret fourni ici est une valeur de DEMONSTRATION,
        // a changer en production (variable d'environnement/coffre-fort,
        // jamais en dur dans le depot de code).
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_VALIDITY)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String extractSubject(String token) {
        Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }
}
