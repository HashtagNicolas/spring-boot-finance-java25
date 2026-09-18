package com.hashtag.ngo.example.bank.cucumber;

import java.math.BigDecimal;

/**
 * Les scenarios sont ecrits en francais et utilisent la notation francaise
 * des nombres decimaux (virgule, ex : "100,50"). On capture volontairement
 * ces montants avec le type Cucumber {string} - jamais {double}, dont
 * l'interpretation depend de la locale par defaut de la JVM d'execution et
 * serait donc non deterministe d'un environnement a l'autre - puis on les
 * parse nous-memes ici, explicitement.
 */
final class Amounts {

    private Amounts() {
    }

    static BigDecimal parse(String raw) {
        return new BigDecimal(raw.replace(',', '.'));
    }
}
