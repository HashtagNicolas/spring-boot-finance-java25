package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashtag.ngo.example.bank.api.AccountRequest;
import com.hashtag.ngo.example.bank.api.AccountResponse;
import com.hashtag.ngo.example.bank.api.TransactionRequest;
import com.hashtag.ngo.example.bank.entity.AccountType;
import com.hashtag.ngo.example.bank.entity.TransactionType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps lies a la gestion des comptes : creation, depot, retrait, interets -
 * voir comptes.feature.
 */
public class AccountSteps {

    private final ApiClient apiClient;
    private final TestContext testContext;
    private final ObjectMapper objectMapper;

    public AccountSteps(ApiClient apiClient, TestContext testContext, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.testContext = testContext;
        this.objectMapper = objectMapper;
    }

    @Given("un compte courant {string} avec un solde initial de {string} et un découvert autorisé de {string}")
    public void unCompteCourantExiste(String label, String initialBalance, String overdraftLimit) throws Exception {
        creerCompte(label, AccountType.COURANT, initialBalance, overdraftLimit, null);
    }

    @Given("un compte épargne {string} avec un solde initial de {string} et un taux d'intérêt de {string}")
    public void unCompteEpargneExiste(String label, String initialBalance, String interestRate) throws Exception {
        creerCompte(label, AccountType.EPARGNE, initialBalance, null, interestRate);
    }

    @When("je crée un compte courant {string} avec un solde initial de {string} et un découvert autorisé de {string}")
    public void jeCreeUnCompteCourant(String label, String initialBalance, String overdraftLimit) throws Exception {
        creerCompte(label, AccountType.COURANT, initialBalance, overdraftLimit, null);
    }

    @When("je crée un compte épargne {string} avec un solde initial de {string} et un taux d'intérêt de {string}")
    public void jeCreeUnCompteEpargne(String label, String initialBalance, String interestRate) throws Exception {
        creerCompte(label, AccountType.EPARGNE, initialBalance, null, interestRate);
    }

    @When("je dépose {string} sur le compte {string}")
    public void jeDepose(String amount, String label) throws Exception {
        actionMontant(label, "deposit", amount);
    }

    @When("je retire {string} du compte {string}")
    public void jeRetire(String amount, String label) throws Exception {
        actionMontant(label, "withdraw", amount);
    }

    @When("j'applique les intérêts sur le compte {string}")
    public void jappliqueLesInterets(String label) {
        Long accountId = testContext.getAccountId(label);
        testContext.setLastResponse(
                apiClient.post("/accounts/" + accountId + "/apply-interest", testContext.getJwtToken(), null));
    }

    @Then("le compte créé est de type {string} avec un solde de {string}")
    public void leCompteCreeEstDeType(String type, String balance) throws Exception {
        AccountResponse account = objectMapper.readValue(testContext.getLastResponse().getBody(), AccountResponse.class);
        assertThat(account.type()).isEqualTo(AccountType.valueOf(type));
        assertThat(account.balance()).isEqualByComparingTo(Amounts.parse(balance));
    }

    @Then("le solde du compte {string} est {string}")
    public void leSoldeDuCompteEst(String label, String expectedBalance) throws Exception {
        Long accountId = testContext.getAccountId(label);
        ResponseEntity<String> response = apiClient.get("/accounts/" + accountId, testContext.getJwtToken());
        AccountResponse account = objectMapper.readValue(response.getBody(), AccountResponse.class);
        assertThat(account.balance()).isEqualByComparingTo(Amounts.parse(expectedBalance));
    }

    private void creerCompte(String label, AccountType type, String initialBalance,
                              String overdraftLimit, String interestRate) throws Exception {
        AccountRequest request = new AccountRequest(
                type,
                label,
                Amounts.parse(initialBalance),
                overdraftLimit != null ? Amounts.parse(overdraftLimit) : null,
                interestRate != null ? Amounts.parse(interestRate) : null);

        ResponseEntity<String> response = apiClient.post(
                "/accounts", testContext.getJwtToken(), objectMapper.writeValueAsString(request));
        testContext.setLastResponse(response);

        if (response.getStatusCode().is2xxSuccessful()) {
            AccountResponse account = objectMapper.readValue(response.getBody(), AccountResponse.class);
            testContext.rememberAccountId(label, account.id());
        }
    }

    private void actionMontant(String label, String operation, String amount) throws Exception {
        Long accountId = testContext.getAccountId(label);
        // Le champ "type" de TransactionRequest est ignore par ces deux
        // endpoints (l'operation est deja determinee par l'URL /deposit ou
        // /withdraw), on met neanmoins une valeur coherente.
        TransactionRequest request = new TransactionRequest(TransactionType.DEPOT, Amounts.parse(amount));
        ResponseEntity<String> response = apiClient.post("/accounts/" + accountId + "/" + operation,
                testContext.getJwtToken(), objectMapper.writeValueAsString(request));
        testContext.setLastResponse(response);
    }
}
