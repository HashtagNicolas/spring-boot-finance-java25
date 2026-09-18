package com.hashtag.ngo.example.bank.bean;

/**
 * Point d'acces unique au contexte d'audit courant, propage via un
 * {@link ScopedValue} (JEP 506, finalise en Java 25).
 * <p>
 * Pourquoi un ScopedValue plutot qu'un ThreadLocal ou un parametre de
 * methode :
 * <ul>
 *   <li><b>Immuable</b> : une fois lie via {@code ScopedValue.where(...)},
 *       la valeur ne peut plus etre modifiee dans la portee courante (pas
 *       d'equivalent de {@code ThreadLocal#set} permettant une mutation
 *       incontrolee en profondeur dans la pile d'appel).</li>
 *   <li><b>Structure</b> : la duree de vie de la liaison est bornee
 *       lexicalement par le bloc {@code where(...).run(...)} ou
 *       {@code where(...).call(...)}. Il n'y a pas de {@code remove()} a ne
 *       pas oublier : a la sortie du bloc, la liaison redevient
 *       automatiquement absente, ce qui supprime le risque classique de fuite
 *       de contexte d'une requete vers la suivante (thread reutilise dans un
 *       pool sans reinitialisation du ThreadLocal).</li>
 *   <li><b>Peu couteux et lisible partout</b> : accessible depuis n'importe
 *       quelle methode appelee transitivement dans la portee, sans avoir a
 *       faire transiter l'objet en parametre de chaque signature (ici,
 *       TransactionServiceImpl le lit sans que AccountService/Repository
 *       n'aient besoin de le connaitre).</li>
 * </ul>
 * La liaison sera typiquement posee plus tard par la couche api (ex : un
 * filtre/intercepteur web qui lit l'utilisateur authentifie) via
 * {@code ScopedValue.where(AuditContextHolder.CURRENT, contexte).run(() -> ...)}.
 */
public final class AuditContextHolder {

    public static final ScopedValue<AuditContext> CURRENT = ScopedValue.newInstance();

    private AuditContextHolder() {
    }
}
