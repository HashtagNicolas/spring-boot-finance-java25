package com.hashtag.ngo.example.bank.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Metadonnees OpenAPI globales et definition du schema de securite "Bearer"
 * (JWT) : @SecurityRequirement l'applique par defaut a tous les endpoints
 * documentes, ce qui affiche le cadenas Swagger UI et permet d'y saisir un
 * jeton (bouton "Authorize").
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API de comptes bancaires",
                version = "0.0.1",
                description = "API pedagogique de comptes bancaires avec transactions tracables"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
