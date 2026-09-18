# API de comptes bancaires — transactions traçables

Ce projet est un **squelette pédagogique** d'API REST bancaire : ouverture de
comptes (courant / épargne), dépôts, retraits, historique des transactions,
authentification par jeton. Son objectif premier n'est pas de livrer une
vraie banque, mais de servir de **support d'apprentissage** pour :

- une architecture Spring Boot en couches, propre et testée ;
- les nouveautés du langage **Java 25**, en particulier trois qui sont
  rarement illustrées ailleurs : les *Flexible Constructor Bodies*, les
  *Scoped Values* et l'API de dérivation de clés (*KDF*) ;
- un socle de tests à trois niveaux : tests d'acceptation **Cucumber**
  (BDD, en français), et tests d'architecture **ArchUnit**.

Techniquement : **Spring Boot 4.1.x** (donc **Spring Framework 7**) sur
**Java 25**, base de données **H2** en mémoire, sécurité **Spring Security**
avec jetons **JWT**, documentation **Swagger / OpenAPI**.

> Tout le code (production et tests) est commenté en français, y compris les
> explications des choix techniques un peu pointus.

---

## 1. Démarrage rapide

### Prérequis

- **JDK 25** installé (`java -version` doit afficher une version 25.x).
- **Maven** (le wrapper n'est pas fourni ici, utilisez votre installation de
  `mvn`).
- Aucune base de données externe à installer : le projet utilise **H2**, une
  base 100 % en mémoire qui démarre et s'arrête avec l'application.

### Lancer les tests

```bash
mvn test
```

Cette commande compile le projet puis exécute :
- les **11 scénarios Cucumber** (tests d'acceptation, qui démarrent une
  vraie instance de l'application sur un port libre et l'appellent en HTTP) ;
- les **6 règles ArchUnit** (tests qui vérifient que le code respecte
  l'architecture attendue, voir section 6).

`mvn clean test` fait la même chose après avoir supprimé le dossier
`target/` (utile si vous doutez d'un résultat de compilation en cache).

### Démarrer l'application

```bash
mvn spring-boot:run
```

L'API est alors disponible sur **http://localhost:8080**.

### Explorer l'API avec Swagger UI

Une fois l'application démarrée, ouvrez dans un navigateur :

```
http://localhost:8080/swagger-ui.html
```

Vous y trouverez la liste de tous les endpoints, regroupés par thème
(Authentification, Comptes, Transactions), avec la possibilité de les
essayer directement. Pour appeler un endpoint protégé, cliquez sur le bouton
**Authorize** en haut de la page et collez un jeton obtenu via
`POST /auth/token` (voir juste en dessous), au format `Bearer <jeton>`.

### S'authentifier

Un unique utilisateur de démonstration existe : **`demo` / `demo123`**.

```bash
curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}'
```

La réponse contient un jeton JWT (`{"token":"..."}`) à joindre ensuite dans
l'en-tête `Authorization: Bearer <jeton>` de toutes les autres requêtes
(seuls `/auth/token`, `/swagger-ui/**` et `/v3/api-docs/**` sont accessibles
sans jeton).

---

## 2. Structure du projet

Le code de production (`src/main/java`) est organisé en **trois couches**,
chacune dans son propre package, avec un sens de dépendance strict et
vérifié automatiquement par un test ArchUnit (section 6) :

```
com.hashtag.ngo.example.bank
│
├── entity/          Couche la plus basse : rien ne depend d'elle en amont.
│   │                Entites JPA (persistees en base) et exceptions metier.
│   ├── Account.java              compte bancaire, classe scellee (sealed)
│   ├── CheckingAccount.java      compte courant (avec decouvert)
│   ├── SavingsAccount.java       compte epargne (avec taux d'interet)
│   ├── Transaction.java          trace d'un depot/retrait
│   ├── AccountType.java, TransactionType.java   enumerations
│   ├── AccountRepository.java, TransactionRepository.java   acces base (Spring Data JPA)
│   └── *Exception.java           AccountNotFoundException, InsufficientFundsException, InvalidAmountException
│
├── bean/            Couche metier : contient la LOGIQUE, aucune dependance vers api.
│   │                Uniquement des INTERFACES (contrats), + quelques types partages.
│   ├── AccountService.java, TransactionService.java, JwtService.java, KeyDerivationService.java
│   ├── AuditContext.java, AuditContextHolder.java   contexte d'audit (Scoped Values)
│   └── TransactionCommand.java   une operation d'un lot de transactions
│
├── bean/impl/       IMPLEMENTATIONS des interfaces de bean/ (jamais l'inverse).
│   ├── AccountServiceImpl.java
│   ├── TransactionServiceImpl.java
│   ├── JwtServiceImpl.java
│   └── KeyDerivationServiceImpl.java
│
└── api/             Couche la plus haute : point d'entree HTTP de l'application.
    │                Controleurs REST, DTO (objets d'echange), mappers, securite.
    ├── AccountController.java, TransactionController.java, AuthController.java
    ├── AccountRequest.java, AccountResponse.java, TransactionRequest.java, ...   (DTO, voir section 5)
    ├── AccountMapper.java, TransactionMapper.java   conversion entite <-> DTO (MapStruct)
    ├── SecurityConfig.java, JwtAuthenticationFilter.java, UserDetailsConfig.java
    ├── GlobalExceptionHandler.java   traduit les exceptions metier en reponses HTTP
    └── OpenApiConfig.java   configuration Swagger
```

Règle de dépendance (vérifiée par `LayeredArchitectureTest`) :

```
api  --->  bean  --->  entity
```

`api` peut utiliser `bean` et `entity` ; `bean` (et `bean.impl`) ne peut
utiliser que `entity` ; `entity` n'utilise jamais les deux autres. Aucune
"remontée" n'est autorisée.

Les tests (`src/test/java`) vivent dans deux packages :

```
com.hashtag.ngo.example.bank
├── cucumber/        Tests d'acceptation BDD (scenarios .feature + steps Java)
└── architecture/    Tests ArchUnit (regles de dependance et de nommage)
```

---

## 3. Java 25 : ce que ce projet met en valeur

Le tableau ci-dessous relie chaque **besoin métier concret** du projet à la
**nouveauté Java** qui le résout, avec l'endroit exact du code où la
regarder.

| Besoin métier | Nouveauté Java 25 | Où dans le code |
|---|---|---|
| Interdire la construction d'un compte avec un découvert ou un taux d'intérêt négatif, **avant** même que l'objet `Account` sous-jacent n'existe | **Flexible Constructor Bodies** (JEP 513) : on peut exécuter des instructions de validation *avant* l'appel à `super(...)` dans un constructeur | `CheckingAccount`, `SavingsAccount` (constructeurs) |
| Savoir "qui a effectué cette transaction" dans les journaux, sans faire transiter un paramètre `utilisateur` à travers toutes les méthodes appelées | **Scoped Values** (JEP 506) : une valeur immuable, liée le temps d'un traitement, lisible par tout code appelé transitivement | `AuditContext`, `AuditContextHolder`, `JwtAuthenticationFilter`, `TransactionServiceImpl` |
| Dériver une clé de chiffrement à partir d'un secret et d'un sel, avec une API standard (sans bibliothèque tierce) | **API KDF** (*Key Derivation Function*, JEP 510) : `javax.crypto.KDF` | `KeyDerivationServiceImpl` |

Ce projet s'appuie aussi sur des nouveautés un peu plus anciennes
(Java 17 et 21), déjà bien connues mais qu'il est utile de rappeler car
elles sont omniprésentes dans le code :

| Nouveauté | Depuis | Où dans le code |
|---|---|---|
| **Records** — classes immuables ultra-concises pour transporter des données | Java 16 | Tous les DTO de `api` (`AccountRequest`, `AccountResponse`...), `AuditContext`, `TransactionCommand` |
| **Sealed classes** — une hiérarchie de classes dont on connaît, à la compilation, la liste exhaustive des sous-types possibles | Java 17 | `Account` (`permits CheckingAccount, SavingsAccount`) |
| **Pattern matching pour `switch`** — un `switch` qui distingue le type réel d'un objet, et que le compilateur sait vérifier exhaustif sur une hiérarchie scellée (sans `default`) | Java 21 | `AccountServiceImpl.withdraw(...)` (règle de découvert selon le type de compte), `AccountMapper.toResponse(...)` |
| **Threads virtuels** (*virtual threads*, Project Loom) — des threads très légers, adaptés à du code qui attend beaucoup (I/O, base de données) | Java 21 | `TransactionServiceImpl.processBatch(...)` (traitement d'un lot de transactions) |
| **Sequenced Collections** — des méthodes comme `getFirst()`/`getLast()` sur toutes les listes/collections ordonnées, sans manipuler d'index | Java 21 | Steps Cucumber `TransactionSteps` (dernière transaction de l'historique) |

---

## 4. « record vs DTO classique »

Un **DTO** (*Data Transfer Object*) est un simple objet qui ne sert qu'à
transporter des données — ici, entre l'API REST et ses appelants (le JSON
envoyé/reçu). Avant les *records*, écrire un DTO en Java demandait
d'écrire à la main :

```java
public final class AccountRequest {
    private final AccountType type;
    private final String owner;
    private final BigDecimal initialBalance;
    // ... encore 2 champs

    public AccountRequest(AccountType type, String owner, BigDecimal initialBalance /* ... */) {
        this.type = type;
        this.owner = owner;
        this.initialBalance = initialBalance;
        // ...
    }

    public AccountType getType() { return type; }
    public String getOwner() { return owner; }
    // ... encore des getters

    @Override public boolean equals(Object o) { /* ... */ }
    @Override public int hashCode() { /* ... */ }
    @Override public String toString() { /* ... */ }
}
```

Beaucoup de code répétitif (le *boilerplate*), pour un objet qui ne fait
qu'empaqueter des valeurs. Avec un **record**, la même chose tient sur une
ligne :

```java
public record AccountRequest(AccountType type, String owner, BigDecimal initialBalance,
                              BigDecimal overdraftLimit, BigDecimal interestRate) {
}
```

Le compilateur génère automatiquement : le constructeur, les accesseurs
(`type()`, `owner()`... — sans le préfixe `get`), ainsi que `equals()`,
`hashCode()` et `toString()`. Les champs d'un record sont toujours
**immuables** (on ne peut pas les modifier après création), ce qui est
exactement ce qu'on veut pour un DTO : une fois qu'une requête ou une
réponse a été construite, elle ne doit plus changer.

C'est pour cela que **tous les DTO de la couche `api`**
(`AccountRequest`, `AccountResponse`, `TransactionRequest`,
`TransactionResponse`, `AuthRequest`, `TokenResponse`, `ErrorResponse`) sont
des records dans ce projet, plutôt que des classes classiques.

---

## 5. Les briques techniques du projet

### Cucumber — tests d'acceptation en langage naturel

Cucumber permet d'écrire des tests sous forme de **scénarios en langage
presque naturel** (Gherkin), ici en français, dans des fichiers `.feature`
(`src/test/resources/features`). Par exemple :

```gherkin
Scénario: Dépôt sur un compte courant
  Etant donné un compte courant "Carole" avec un solde initial de "100,00" et un découvert autorisé de "0,00"
  Quand je dépose "50,00" sur le compte "Carole"
  Alors le code de réponse est 200
  Et le solde du compte "Carole" est "150,00"
```

Chaque ligne (`Etant donné`, `Quand`, `Alors`...) est reliée à une méthode
Java (une *step definition*, dans `src/test/java/.../cucumber`) qui
l'exécute réellement — ici, en appelant l'API HTTP démarrée pour de vrai sur
un port libre. Ce sont donc des tests de **bout en bout** : ils valident le
comportement observable de l'application, pas les détails internes.

### MapStruct — mappers générés automatiquement

Convertir une entité JPA (`Account`) en DTO exposé par l'API
(`AccountResponse`) demande, là aussi, du code répétitif (recopier chaque
champ). MapStruct génère ce code **à la compilation**, à partir d'une simple
interface annotée `@Mapper` (`AccountMapper`, `TransactionMapper`) : on
regarde le fichier généré dans `target/generated-sources` pour voir le
résultat, mais on n'écrit jamais ce code à la main.

### ArchUnit — tester l'architecture elle-même

ArchUnit permet d'écrire des **tests sur la structure du code** plutôt que
sur son comportement : "telle couche ne doit jamais dépendre de telle
autre", "telle classe doit s'appeler ainsi", etc. Ici (package
`architecture`) :

- `LayeredArchitectureTest` vérifie le sens de dépendance
  `api → bean → entity` décrit en section 2 ;
- `NamingConventionsTest` vérifie les conventions de nommage : les
  contrôleurs REST s'appellent `*Controller`, les interfaces de service
  (dans `bean`) s'appellent `*Service`, leurs implémentations (dans
  `bean.impl`) s'appellent `*ServiceImpl`.

La **règle « impl »** mérite un mot d'explication : toute classe dont le nom
se termine par `Impl` doit résider dans un package qui se termine par
`.impl` (ex. `bean.impl`) — sauf les classes `*MapperImpl`, qui sont générées
par MapStruct **à côté** de leur interface `@Mapper` (donc dans `api`, pas
dans un `.impl`), une exception assumée et documentée dans le test lui-même.
À l'inverse, un package `.impl` ne doit contenir **aucune interface** : il
ne sert qu'à ranger des implémentations concrètes.

### Spring Security + JWT — qui a le droit de faire quoi

L'API est protégée par un jeton **JWT** (*JSON Web Token*, un jeton signé
qui prouve l'identité de son porteur sans que le serveur ait besoin de
garder de session en mémoire — on dit qu'elle est *stateless*). Le
fonctionnement :

1. `POST /auth/token` (ouvert à tous) vérifie le couple utilisateur/mot de
   passe et renvoie un jeton signé (`JwtService`, implémenté avec la
   bibliothèque JJWT).
2. Pour tout autre appel, le client doit fournir ce jeton dans l'en-tête
   `Authorization: Bearer <jeton>`.
3. `JwtAuthenticationFilter` intercepte chaque requête, vérifie le jeton, et
   si tout est correct, autorise la suite du traitement.
4. En l'absence de jeton valide, `SecurityConfig` garantit une réponse
   **401** (non authentifié) plutôt que 403 (qui signifierait "identifié
   mais pas autorisé", ce qui ne correspond pas à la situation).

### JPA / H2 — enregistrer les données

**JPA** (*Java Persistence API*) est la façon standard, en Java, de faire
correspondre des objets (`Account`, `Transaction`) à des lignes de table en
base de données, via **Hibernate** (l'implémentation utilisée ici). La base
elle-même est **H2**, une base de données qui tourne entièrement en mémoire
: aucune installation nécessaire, mais les données sont perdues à chaque
redémarrage — un choix délibéré pour un projet pédagogique.

Un détail technique mérite d'être signalé : `Account` est une classe
**scellée** (voir section 3) dont les deux sous-types (`CheckingAccount`,
`SavingsAccount`) sont `final`. Hibernate a normalement besoin de pouvoir
créer une sous-classe technique de chaque entité pour ses "objets
différés" (des objets qui ne vont chercher les données en base qu'au
moment où on les utilise réellement) — impossible sur une classe `final`.
L'annotation `@ConcreteProxy` (posée sur `Account`) indique à Hibernate
d'utiliser une autre stratégie, compatible avec les classes scellées.

### Swagger UI — explorer et tester l'API dans le navigateur

`springdoc-openapi` génère automatiquement, à partir des contrôleurs et de
leurs annotations (`@Tag`, `@Operation`...), une documentation **OpenAPI**
de toute l'API, consultable et testable dans un navigateur via
**Swagger UI** (voir section 1). C'est l'équivalent d'un mode d'emploi
toujours à jour, puisqu'il est généré depuis le code lui-même.

---

## 6. Exemples d'appels `curl`

Les exemples suivants supposent l'application démarrée (`mvn spring-boot:run`)
sur `http://localhost:8080`.

### 1. Obtenir un jeton

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/token \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo123"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')

echo "$TOKEN"
```

### 2. Créer un compte courant

```bash
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
        "type": "COURANT",
        "owner": "Alice",
        "initialBalance": 1000.00,
        "overdraftLimit": 200.00
      }'
```

Réponse (`201 Created`) : le compte créé, avec son identifiant (`id`), par
exemple `{"id":1,"type":"COURANT","owner":"Alice","balance":1000.00,...}`.

### 3. Déposer de l'argent sur ce compte

```bash
curl -s -X POST http://localhost:8080/accounts/1/deposit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"amount": 50.00}'
```

### 4. Envoyer un lot de transactions (traité via des threads virtuels)

```bash
curl -s -X POST http://localhost:8080/transactions/batch \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
        {"type": "DEPOT",   "accountId": 1, "amount": 50.00},
        {"type": "RETRAIT", "accountId": 1, "amount": 20.00}
      ]'
```

La réponse est la liste des transactions enregistrées, chacune horodatée
individuellement (elles sont traitées en parallèle, un thread virtuel par
transaction — voir section 3).

D'autres endpoints existent (consultation d'un compte, historique des
transactions, application des intérêts sur un compte épargne...) : la
façon la plus simple de tous les découvrir est d'ouvrir **Swagger UI**
(section 1).
