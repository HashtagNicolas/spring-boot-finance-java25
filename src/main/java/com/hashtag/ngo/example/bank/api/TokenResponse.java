package com.hashtag.ngo.example.bank.api;

/**
 * Corps de reponse portant un jeton JWT emis (utilise par le futur
 * AuthController, voir prompt suivant).
 */
public record TokenResponse(String token) {
}
