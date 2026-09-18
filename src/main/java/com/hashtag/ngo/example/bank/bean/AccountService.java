package com.hashtag.ngo.example.bank.bean;

import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.CheckingAccount;
import com.hashtag.ngo.example.bank.entity.SavingsAccount;

import java.math.BigDecimal;
import java.util.List;

/**
 * Logique metier autour des comptes bancaires : creation, consultation,
 * depot, retrait.
 */
public interface AccountService {

    CheckingAccount createCheckingAccount(String owner, BigDecimal initialBalance, BigDecimal overdraftLimit);

    SavingsAccount createSavingsAccount(String owner, BigDecimal initialBalance, BigDecimal interestRate);

    Account getAccount(Long accountId);

    List<Account> listAccounts();

    /** Depose {@code amount} sur le compte et retourne le compte mis a jour. */
    Account deposit(Long accountId, BigDecimal amount);

    /**
     * Retire {@code amount} du compte, en respectant la regle propre a
     * chaque type de compte (decouvert autorise pour un compte courant, pas
     * de solde negatif pour un compte epargne).
     */
    Account withdraw(Long accountId, BigDecimal amount);
}
