package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashtag.ngo.example.bank.api.AuthRequest;
import com.hashtag.ngo.example.bank.api.TokenResponse;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps lies a l'authentification et a la securite (jeton absent, invalide,
 * valide) - voir authentification.feature.
 */
public class AuthSteps {

    private final ApiClient apiClient;
    private final TestContext testContext;
    private final ObjectMapper objectMapper;

    public AuthSteps(ApiClient apiClient, TestContext testContext, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.testContext = testContext;
        this.objectMapper = objectMapper;
    }

    @Given("je suis authentifié en tant que {string} avec le mot de passe {string}")
    public void jeSuisAuthentifie(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(new AuthRequest(username, password));
        var response = apiClient.post("/auth/token", null, body);
        assertThat(response.getStatusCode().is2xxSuccessful())
                .as("Authentification de %s echouee (%s) : %s", username, response.getStatusCode(), response.getBody())
                .isTrue();
        TokenResponse token = objectMapper.readValue(response.getBody(), TokenResponse.class);
        testContext.setJwtToken(token.token());
    }

    // Note : dans une Cucumber Expression, "/" introduit une alternative
    // (ex : "a/b" veut dire "a" ou "b"), il faut donc l'echapper en "\/"
    // pour matcher un "/" litteral dans un chemin d'URL comme "/accounts".
    @When("j'appelle GET \\/accounts sans jeton d'authentification")
    public void jappelleGetAccountsSansJeton() {
        testContext.setLastResponse(apiClient.get("/accounts", null));
    }

    @When("j'appelle GET \\/accounts avec le jeton invalide {string}")
    public void jappelleGetAccountsAvecJetonInvalide(String invalidToken) {
        testContext.setLastResponse(apiClient.get("/accounts", invalidToken));
    }

    @When("j'appelle GET \\/accounts avec mon jeton")
    public void jappelleGetAccountsAvecMonJeton() {
        testContext.setLastResponse(apiClient.get("/accounts", testContext.getJwtToken()));
    }

    @Then("le code de réponse est {int}")
    public void leCodeDeReponseEst(int expectedStatus) {
        assertThat(testContext.getLastResponse().getStatusCode().value()).isEqualTo(expectedStatus);
    }
}
