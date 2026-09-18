package com.hashtag.ngo.example.bank.api;

import java.time.Instant;

/**
 * Corps de reponse d'erreur standardise (utilise par le futur gestionnaire
 * d'exceptions global, voir prompt suivant).
 */
public record ErrorResponse(Instant timestamp, int status, String code, String message) {
}
