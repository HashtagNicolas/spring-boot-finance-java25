package com.hashtag.ngo.example.bank.bean;

/**
 * Contexte d'audit : identifie qui est a l'origine d'une operation (ex : une
 * transaction) et permet de correler les appels via un identifiant de requete.
 * <p>
 * Implemente comme un record : immuable par construction, ce qui est
 * precisement la propriete recherchee pour une valeur propagee via
 * {@link java.lang.ScopedValue} (voir AuditContextHolder).
 */
public record AuditContext(String username, String requestId) {

    /**
     * Contexte par defaut utilise quand aucun contexte n'a ete lie (ex : appel
     * technique/batch sans utilisateur authentifie).
     */
    public static AuditContext system() {
        return new AuditContext("system", "n/a");
    }
}
