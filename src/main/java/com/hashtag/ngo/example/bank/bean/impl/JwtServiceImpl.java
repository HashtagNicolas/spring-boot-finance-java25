package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Implementation JJWT du service JWT, volontairement decouplee derriere
 * l'interface JwtService : le reste de l'application (ex : la future couche
 * api/securite) depend de JwtService, jamais directement de JJWT.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(1);

    // Cle de signature HMAC generee au demarrage (squelette pedagogique). En
    // production, cette cle proviendrait d'un secret externe stable (coffre-
    // fort, variable d'environnement...), par exemple obtenu via
    // KeyDerivationService plutot que genere aleatoirement a chaque demarrage.
    private final SecretKey signingKey = Jwts.SIG.HS256.key().build();

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
