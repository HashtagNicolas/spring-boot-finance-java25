package com.hashtag.ngo.example.bank.cucumber;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Client HTTP partage par tous les scenarios : un seul RestClient, pointant
 * sur le port aleatoire attribue par @SpringBootTest(webEnvironment =
 * RANDOM_PORT). Le contexte Spring (et donc le port) est le meme pour toute
 * la suite, ce bean singleton peut donc etre reutilise partout.
 * <p>
 * Utilise volontairement RestClient (et non TestRestTemplate) : c'est le
 * client HTTP synchrone moderne de Spring (depuis Spring Framework 6.1), a
 * l'API fluide, qui remplace progressivement RestTemplate/TestRestTemplate.
 * <p>
 * Chaque appel neutralise le comportement par defaut de RestClient qui leve
 * une exception sur un code 4xx/5xx (onStatus(status -> true, no-op)) : les
 * scenarios veulent souvent inspecter un code d'erreur (400, 401...) via une
 * assertion Gherkin plutot que recevoir une exception Java.
 * <p>
 * {@code @Lazy} est indispensable ici : la propriete "local.server.port"
 * n'est publiee dans l'Environment qu'au demarrage effectif du serveur
 * embarque (evenement WebServerInitializedEvent), qui a lieu APRES
 * l'instanciation des beans singletons classiques lors du refresh du
 * contexte. Sans @Lazy, ce bean serait construit trop tot et
 * "${local.server.port}" ne pourrait pas etre resolu. @Lazy differe sa
 * creation jusqu'au premier usage reel (le premier step definition qui en a
 * besoin), soit bien apres la fin du demarrage du serveur.
 */
@Component
@Lazy
class ApiClient {

    private final RestClient restClient;

    ApiClient(@Value("${local.server.port}") int port) {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    ResponseEntity<String> get(String uri, String bearerToken, Object... uriVariables) {
        RestClient.RequestHeadersSpec<?> request = restClient.get().uri(uri, uriVariables);
        if (bearerToken != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        }
        return request.retrieve()
                .onStatus(status -> true, (req, res) -> { })
                .toEntity(String.class);
    }

    ResponseEntity<String> post(String uri, String bearerToken, String jsonBody, Object... uriVariables) {
        RestClient.RequestBodySpec request = restClient.post()
                .uri(uri, uriVariables)
                .contentType(MediaType.APPLICATION_JSON);
        if (bearerToken != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
        }
        RestClient.RequestHeadersSpec<?> ready = (jsonBody != null) ? request.body(jsonBody) : request;
        return ready.retrieve()
                .onStatus(status -> true, (req, res) -> { })
                .toEntity(String.class);
    }
}
