package com.hashtag.ngo.example.bank.cucumber;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Etat partage entre les differentes classes de step definitions au sein
 * d'UN MEME scenario Cucumber (derniere reponse HTTP, jeton courant, comptes
 * crees identifies par un libelle lisible cote Gherkin).
 * <p>
 * {@code @ScenarioScope} (fourni par cucumber-spring) cree une instance
 * fraiche a chaque scenario et la detruit a la fin de celui-ci : aucune fuite
 * d'etat d'un scenario vers le suivant, meme si le contexte Spring (et donc
 * le port HTTP/ApiClient) est partage pour toute la suite.
 */
@Component
@ScenarioScope
class TestContext {

    private ResponseEntity<String> lastResponse;
    private String jwtToken;
    private final Map<String, Long> accountIdsByLabel = new HashMap<>();

    void setLastResponse(ResponseEntity<String> lastResponse) {
        this.lastResponse = lastResponse;
    }

    ResponseEntity<String> getLastResponse() {
        return lastResponse;
    }

    void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    String getJwtToken() {
        return jwtToken;
    }

    void rememberAccountId(String label, Long id) {
        accountIdsByLabel.put(label, id);
    }

    Long getAccountId(String label) {
        return accountIdsByLabel.get(label);
    }
}
