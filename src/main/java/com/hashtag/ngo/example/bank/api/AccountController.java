package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.entity.Account;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints REST autour des comptes : creation, consultation, depot, retrait.
 */
@RestController
@RequestMapping("/accounts")
@Tag(name = "Comptes", description = "Creation, consultation, depot et retrait sur les comptes")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    public AccountController(AccountService accountService, AccountMapper accountMapper) {
        this.accountService = accountService;
        this.accountMapper = accountMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cree un compte courant ou epargne")
    public AccountResponse create(@RequestBody AccountRequest request) {
        // Switch exhaustif sur l'enum AccountType : selon le type demande, on
        // delegue a la fabrique adequate d'AccountService (chacune validant
        // ses propres champs via les constructeurs d'entite, cf. JEP 513).
        Account account = switch (request.type()) {
            case COURANT -> accountService.createCheckingAccount(
                    request.owner(), request.initialBalance(), request.overdraftLimit());
            case EPARGNE -> accountService.createSavingsAccount(
                    request.owner(), request.initialBalance(), request.interestRate());
        };
        return accountMapper.toResponse(account);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulte un compte par son identifiant")
    public AccountResponse get(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.getAccount(id));
    }

    @GetMapping
    @Operation(summary = "Liste tous les comptes")
    public List<AccountResponse> list() {
        return accountMapper.toResponseList(accountService.listAccounts());
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Depose un montant sur le compte")
    public AccountResponse deposit(@PathVariable Long id, @RequestBody TransactionRequest request) {
        return accountMapper.toResponse(accountService.deposit(id, request.amount()));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Retire un montant du compte")
    public AccountResponse withdraw(@PathVariable Long id, @RequestBody TransactionRequest request) {
        return accountMapper.toResponse(accountService.withdraw(id, request.amount()));
    }
}
