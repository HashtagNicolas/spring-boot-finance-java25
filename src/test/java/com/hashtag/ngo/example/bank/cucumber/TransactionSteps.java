package com.hashtag.ngo.example.bank.cucumber;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hashtag.ngo.example.bank.api.TransactionRequest;
import com.hashtag.ngo.example.bank.api.TransactionResponse;
import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.entity.TransactionType;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Steps lies a l'enregistrement de transactions, a l'historique et au
 * traitement par lot - voir transactions.feature.
 */
public class TransactionSteps {

    private final ApiClient apiClient;
    private final TestContext testContext;
    private final ObjectMapper objectMapper;

    public TransactionSteps(ApiClient apiClient, TestContext testContext, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.testContext = testContext;
        this.objectMapper = objectMapper;
    }

    @When("j'enregistre une transaction de type {string} de {string} sur le compte {string}")
    public void jenregistreUneTransaction(String type, String amount, String label) throws Exception {
        Long accountId = testContext.getAccountId(label);
        TransactionRequest request = new TransactionRequest(TransactionType.valueOf(type), Amounts.parse(amount));
        ResponseEntity<String> response = apiClient.post("/accounts/" + accountId + "/transactions",
                testContext.getJwtToken(), objectMapper.writeValueAsString(request));
        testContext.setLastResponse(response);
    }

    @When("j'envoie un lot de transactions:")
    public void jenvoieUnLotDeTransactions(DataTable table) throws Exception {
        List<TransactionCommand> commands = new ArrayList<>();
        for (Map<String, String> row : table.asMaps(String.class, String.class)) {
            Long accountId = testContext.getAccountId(row.get("compte"));
            commands.add(new TransactionCommand(
                    TransactionType.valueOf(row.get("type")),
                    accountId,
                    Amounts.parse(row.get("montant"))));
        }
        ResponseEntity<String> response = apiClient.post("/transactions/batch",
                testContext.getJwtToken(), objectMapper.writeValueAsString(commands));
        testContext.setLastResponse(response);
    }

    @Then("la dernière transaction de l'historique du compte {string} est de type {string} et de montant {string}")
    public void laDerniereTransactionEstDe(String label, String type, String amount) throws Exception {
        Long accountId = testContext.getAccountId(label);
        ResponseEntity<String> response = apiClient.get(
                "/accounts/" + accountId + "/transactions", testContext.getJwtToken());
        List<TransactionResponse> history = objectMapper.readValue(
                response.getBody(), new TypeReference<List<TransactionResponse>>() { });

        // Sequenced Collections (JEP 431, Java 21) : List#getLast() recupere
        // directement le dernier element de l'historique, sans manipuler
        // d'index (history.get(history.size() - 1)).
        TransactionResponse last = history.getLast();
        assertThat(last.type()).isEqualTo(TransactionType.valueOf(type));
        assertThat(last.amount()).isEqualByComparingTo(Amounts.parse(amount));
    }

    @Then("le lot traite {int} transactions")
    public void leLotTraite(int count) throws Exception {
        List<TransactionResponse> results = objectMapper.readValue(
                testContext.getLastResponse().getBody(), new TypeReference<List<TransactionResponse>>() { });
        assertThat(results).hasSize(count);
    }
}
