package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.AccountRepository;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public CheckingAccount createCheckingAccount(String owner, BigDecimal initialBalance, BigDecimal overdraftLimit) {
        // La validation (decouvert >= 0) est deja assuree par le constructeur
        // de CheckingAccount (Flexible Constructor Bodies, JEP 513).
        return accountRepository.save(new CheckingAccount(owner, initialBalance, overdraftLimit));
    }

    @Override
    public SavingsAccount createSavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate) {
        return accountRepository.save(new SavingsAccount(owner, initialBalance, interestRate));
    }

    @Override
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Compte introuvable : " + accountId));
    }

    @Override
    public List<Account> listAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);
        Account account = getAccount(accountId);
        account.setBalance(account.getBalance().add(amount));
        return account;
    }

    @Override
    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        requirePositiveAmount(amount);
        Account account = getAccount(accountId);

        // Pattern matching for switch (JEP 441, Java 21) sur la hierarchie
        // scellee Account : comme Account est "sealed" et ne permet que
        // CheckingAccount et SavingsAccount, le compilateur peut verifier que
        // ce switch est EXHAUSTIF sans clause "default". Si une nouvelle
        // sous-classe de compte est ajoutee un jour, ce switch ne compilera
        // plus tant qu'elle n'aura pas ete traitee ici : la regle metier
        // "solde plancher autorise par type de compte" ne peut pas etre
        // oubliee silencieusement.
        BigDecimal minimumBalance = switch (account) {
            case CheckingAccount checking -> checking.getOverdraftLimit().negate();
            case SavingsAccount savings -> BigDecimal.ZERO;
        };

        BigDecimal newBalance = account.getBalance().subtract(amount);
        if (newBalance.compareTo(minimumBalance) < 0) {
            throw new IllegalStateException("Solde insuffisant pour ce retrait (limite : " + minimumBalance + ")");
        }
        account.setBalance(newBalance);
        return account;
    }

    private static void requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit etre strictement positif");
        }
    }
}
