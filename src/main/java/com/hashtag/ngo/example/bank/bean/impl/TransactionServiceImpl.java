package com.hashtag.ngo.example.bank.bean.impl;

import com.hashtag.ngo.example.bank.bean.AccountService;
import com.hashtag.ngo.example.bank.bean.AuditContext;
import com.hashtag.ngo.example.bank.bean.AuditContextHolder;
import com.hashtag.ngo.example.bank.bean.TransactionCommand;
import com.hashtag.ngo.example.bank.bean.TransactionService;
import com.hashtag.ngo.example.bank.entity.Account;
import com.hashtag.ngo.example.bank.entity.Transaction;
import com.hashtag.ngo.example.bank.entity.TransactionRepository;
import com.hashtag.ngo.example.bank.entity.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    @Override
    @Transactional
    public Transaction recordTransaction(TransactionType type, Long accountId, BigDecimal amount) {
        Account account = switch (type) {
            case DEPOT -> accountService.deposit(accountId, amount);
            case RETRAIT -> accountService.withdraw(accountId, amount);
        };

        // Scoped Values (JEP 506, finalise en Java 25) : on lit ici le
        // contexte d'audit courant, lie plus haut dans la pile d'appel (plus
        // tard, typiquement par un filtre web de la couche api) via
        // ScopedValue.where(AuditContextHolder.CURRENT, ctx).run(...). Voir
        // le detail des avantages (immuabilite, portee structuree, pas de
        // ThreadLocal a nettoyer) dans AuditContextHolder.
        AuditContext auditContext = AuditContextHolder.CURRENT.orElse(AuditContext.system());

        Transaction transaction = transactionRepository.save(new Transaction(type, amount, account));
        log.info("Transaction {} de {} sur le compte {} effectuee par {} (requete {})",
                type, amount, accountId, auditContext.username(), auditContext.requestId());
        return transaction;
    }

    @Override
    public List<Transaction> getHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Override
    public List<Transaction> processBatch(List<TransactionCommand> commands) {
        // On capture le contexte d'audit courant AVANT de lancer les threads
        // virtuels : contrairement a la "structured concurrency" (qui propage
        // automatiquement les liaisons ScopedValue aux threads qu'elle
        // demarre, mais reste en preview en Java 25), un simple
        // ExecutorService ne fait PAS heriter ses taches de la liaison
        // ScopedValue du thread appelant. Il faut donc la relier explicitement
        // dans chaque tache via ScopedValue.where(...).call(...).
        AuditContext auditContext = AuditContextHolder.CURRENT.orElse(AuditContext.system());

        // Threads virtuels (Project Loom, JEP 444, Java 21) : chaque commande
        // du lot est executee sur son propre thread virtuel. Le traitement
        // d'une transaction est majoritairement de l'attente (I/O base de
        // donnees), le cas d'usage typique pour lequel les threads virtuels
        // sont pertinents : on peut en lancer des milliers sans epuiser les
        // threads du systeme d'exploitation, contrairement a un pool de
        // threads plateforme classique.
        try (ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Transaction>> futures = commands.stream()
                    .map(command -> virtualThreadExecutor.submit(() ->
                            ScopedValue.where(AuditContextHolder.CURRENT, auditContext)
                                    .call(() -> recordTransaction(command.type(), command.accountId(), command.amount()))))
                    .toList();

            return futures.stream().map(this::await).toList();
        }
    }

    private Transaction await(Future<Transaction> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Traitement du lot de transactions interrompu", e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Echec du traitement d'une transaction du lot", e.getCause());
        }
    }
}
