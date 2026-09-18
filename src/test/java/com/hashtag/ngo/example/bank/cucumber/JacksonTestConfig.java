package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fournit l'ObjectMapper utilise par les step definitions Cucumber pour
 * (de)serialiser les DTO de l'API (AccountResponse, TransactionResponse...).
 * <p>
 * findAndRegisterModules() decouvre et enregistre automatiquement
 * jackson-datatype-jsr310 (present transitivement via spring-boot-starter-web),
 * necessaire pour (de)serialiser les champs java.time.Instant exposes par
 * AccountResponse/TransactionResponse.
 * <p>
 * Cette classe vit sous src/test/java : elle n'est donc jamais incluse dans
 * le jar de production, meme si son package est un sous-package de celui
 * scanne par @SpringBootApplication.
 */
@Configuration
class JacksonTestConfig {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
