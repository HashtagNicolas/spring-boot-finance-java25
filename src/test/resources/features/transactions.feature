# language: fr
Fonctionnalité: Transactions et historique
  En tant que client authentifié
  Je veux enregistrer des transactions et consulter mon historique
  Afin de garder une trace de mes operations

  Contexte:
    Etant donné je suis authentifié en tant que "demo" avec le mot de passe "demo123"

  Scénario: Une transaction enregistrée apparait en dernière position de l'historique
    Etant donné un compte courant "Ines" avec un solde initial de "300,00" et un découvert autorisé de "0,00"
    Quand j'enregistre une transaction de type "DEPOT" de "75,00" sur le compte "Ines"
    Alors le code de réponse est 201
    Et la dernière transaction de l'historique du compte "Ines" est de type "DEPOT" et de montant "75,00"

  Scénario: Traitement d'un lot de transactions concurrent
    Etant donné un compte courant "Gustave" avec un solde initial de "100,00" et un découvert autorisé de "0,00"
    Et un compte épargne "Helene" avec un solde initial de "200,00" et un taux d'intérêt de "0,01"
    Quand j'envoie un lot de transactions:
      | compte  | type    | montant |
      | Gustave | DEPOT   | 50,00   |
      | Helene  | DEPOT   | 25,00   |
      | Gustave | RETRAIT | 30,00   |
    Alors le code de réponse est 201
    Et le lot traite 3 transactions
    Et le solde du compte "Gustave" est "120,00"
    Et le solde du compte "Helene" est "225,00"
