# language: fr
Fonctionnalité: Gestion des comptes bancaires
  En tant que client de la banque
  Je veux ouvrir des comptes et effectuer des depots/retraits
  Afin de gerer mon argent en toute tracabilite

  Contexte:
    Etant donné je suis authentifié en tant que "demo" avec le mot de passe "demo123"

  Scénario: Création d'un compte courant
    Quand je crée un compte courant "Alice" avec un solde initial de "1000,00" et un découvert autorisé de "200,00"
    Alors le code de réponse est 201
    Et le compte créé est de type "COURANT" avec un solde de "1000,00"

  Scénario: Création d'un compte épargne
    Quand je crée un compte épargne "Bob" avec un solde initial de "500,00" et un taux d'intérêt de "0,05"
    Alors le code de réponse est 201
    Et le compte créé est de type "EPARGNE" avec un solde de "500,00"

  Scénario: Dépôt sur un compte courant
    Etant donné un compte courant "Carole" avec un solde initial de "100,00" et un découvert autorisé de "0,00"
    Quand je dépose "50,00" sur le compte "Carole"
    Alors le code de réponse est 200
    Et le solde du compte "Carole" est "150,00"

  Scénario: Retrait autorisé grace au découvert
    Etant donné un compte courant "David" avec un solde initial de "100,00" et un découvert autorisé de "50,00"
    Quand je retire "120,00" du compte "David"
    Alors le code de réponse est 200
    Et le solde du compte "David" est "-20,00"

  Scénario: Retrait refusé au-delà du découvert autorisé
    Etant donné un compte courant "Emma" avec un solde initial de "100,00" et un découvert autorisé de "0,00"
    Quand je retire "150,00" du compte "Emma"
    Alors le code de réponse est 400

  Scénario: Application des intérêts sur un compte épargne
    Etant donné un compte épargne "Farid" avec un solde initial de "1000,00" et un taux d'intérêt de "0,10"
    Quand j'applique les intérêts sur le compte "Farid"
    Alors le code de réponse est 200
    Et le solde du compte "Farid" est "1100,00"
