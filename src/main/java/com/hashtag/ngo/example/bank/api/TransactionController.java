package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.bean.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST autour des transactions : enregistrement, historique, lot.
 */
@RestController
@Tag(name = "Transactions", description = "Enregistrement et historique des depots/retraits")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/accounts/{id}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enregistre une transaction (depot ou retrait) sur un compte")
    public TransactionResponse record(@PathVariable Long id, @RequestBody TransactionRequest request) {
        return transactionMapper.toResponse(
                transactionService.recordTransaction(request.type(), id, request.amount()));
    }

    @GetMapping("/accounts/{id}/transactions")
    @Operation(summary = "Historique des transactions d'un compte")
    public List<TransactionResponse> history(@PathVariable Long id) {
        return transactionMapper.toResponseList(transactionService.getHistory(id));
    }

    @PostMapping("/transactions/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Traite un lot de transactions (threads virtuels)")
    public List<TransactionResponse> batch(@RequestBody List<TransactionCommand> commands) {
        return transactionMapper.toResponseList(transactionService.processBatch(commands));
    }
}
