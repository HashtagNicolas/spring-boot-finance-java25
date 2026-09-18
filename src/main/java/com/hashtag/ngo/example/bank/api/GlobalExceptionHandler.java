package com.hashtag.ngo.example.bank.api;

import com.hashtag.ngo.example.bank.entity.AccountNotFoundException;
import com.hashtag.ngo.example.bank.entity.InsufficientFundsException;
import com.hashtag.ngo.example.bank.entity.InvalidAmountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Instant;

/**
 * Traduction centralisee des exceptions en reponses HTTP uniformes
 * (ErrorResponse), plutot que de dupliquer des try/catch dans chaque
 * controleur.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAccountNotFound(AccountNotFoundException ex) {
        return errorResponse(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_FUNDS", ex.getMessage());
    }

    @ExceptionHandler(InvalidAmountException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException ex) {
        return errorResponse(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        // Erreur technique non prevue : on journalise le detail cote serveur
        // mais on ne renvoie JAMAIS le message/la stack trace au client (pas
        // de fuite d'information interne sur l'implementation).
        log.error("Erreur technique inattendue", ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Une erreur inattendue est survenue");
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String code, String message) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), code, message);
        return ResponseEntity.status(status).body(body);
    }
}
