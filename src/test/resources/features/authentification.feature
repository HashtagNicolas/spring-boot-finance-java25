# language: fr
Fonctionnalité: Securite de l'API (authentification JWT)
  En tant qu'API de comptes bancaires
  Je dois refuser tout acces non authentifie
  Afin de proteger les donnees sensibles des clients

  Scénario: Accès refusé sans jeton
    Quand j'appelle GET /accounts sans jeton d'authentification
    Alors le code de réponse est 401

  Scénario: Accès refusé avec un jeton invalide
    Quand j'appelle GET /accounts avec le jeton invalide "un.jeton.invalide"
    Alors le code de réponse est 401

  Scénario: Accès autorisé avec un jeton valide
    Etant donné je suis authentifié en tant que "demo" avec le mot de passe "demo123"
    Quand j'appelle GET /accounts avec mon jeton
    Alors le code de réponse est 200
