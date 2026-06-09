# Checklist Démo - Sprint Hardening (T016)

**Date :** 2026-06-10  
**Version :** 1.0.0-SNAPSHOT  
**Sprint :** Sprint 1  
**Responsable Démo :** Dev Team  

---

## ✅ Critères d'Acceptation

### 1. Smoke Test : Flux Complet Login → Partie → Coup → WebSocket

**Description :** Valider que le flux utilisateur de bout en bout fonctionne sans interruption.

**Étapes de validation :**

#### 1.1 - Authentification

- [ ] L'application démarre sans erreur en profil `prod`
- [ ] `/api/health` retourne HTTP 200 avec status "UP"
- [ ] POST `/auth/login` avec credentials `demo/demo-password` retourne tokens JWT
  - [ ] `accessToken` présent et au format JWT valide
  - [ ] `refreshToken` présent et au format JWT valide
  - [ ] `tokenType` = "Bearer"
  - [ ] `accessTokenExpiresInSeconds` = 900 (15 minutes)
  - [ ] `refreshTokenExpiresInSeconds` = 604800 (7 jours)

**Critère acceptation :** Tokens générés sans erreur, format valide

---

#### 1.2 - Création de Partie

- [ ] POST `/games` avec authorization JWT crée une partie de jeu
  - [ ] HTTP 201 (Created) retourné
  - [ ] Réponse contient un `gameId` au format UUID
  - [ ] Statut initial = "IN_PROGRESS"
  - [ ] `nextPlayer` = "BLACK"
  - [ ] `boardSize` = 19 (ou valeur envoyée)
  - [ ] `moveCount` = 0

**Scénarios supplémentaires :**
- [ ] Créer une partie sans authorization → HTTP 401
- [ ] Créer une partie avec token expiré → HTTP 401
- [ ] Créer une partie avec boardSize invalide → HTTP 400

**Critère acceptation :** Partie créée avec tous les champs requis, validation d'autorisation fonctionne

---

#### 1.3 - Effectuer un Coup

- [ ] POST `/games/{gameId}/moves` avec coordonnées valides ajoute une pierre
  - [ ] HTTP 200 retourné
  - [ ] `moveCount` incrémenté (passe de 0 à 1)
  - [ ] `nextPlayer` basculé de "BLACK" à "WHITE"
  - [ ] Statut demeure "IN_PROGRESS"
  - [ ] Plateau mis à jour (si visible)

**Scénarios supplémentaires :**
- [ ] Coups multiples alternés (noir, blanc, noir) → moveCount = 3, nextPlayer = "WHITE"
- [ ] Coup sur position occupée → HTTP 422 (UNPROCESSABLE_ENTITY)
- [ ] Coup hors limites du plateau → HTTP 400
- [ ] Coup après fin de partie → HTTP 422

**Critère acceptation :** Coups valides acceptés, états incorrects rejetés, mouvements alternent

---

#### 1.4 - WebSocket Temps Réel

- [ ] WebSocket `/ws/{gameId}` peut être connectée avec authorization
  - [ ] Première connexion reçoit `ConnectionAckMessage`
  - [ ] Chaque coup envoie un `GameEventMessage` aux clients connectés
  - [ ] Les clients reçoivent les mises à jour en temps réel
  - [ ] Fin de partie signalée par message approprié
  - [ ] Déconnexion ferme la session sans erreur

**Scénarios supplémentaires :**
- [ ] Plusieurs clients connectés reçoivent les mêmes événements
- [ ] Message ping/pong fonctionne pour keep-alive
- [ ] Déconnexion client n'affecte pas les autres clients
- [ ] Reconnexion possible après déconnexion

**Critère acceptation :** WebSocket établie, événements envoyés en temps réel, gestion des erreurs fonctionnelle

---

#### 1.5 - Fin de Partie

- [ ] Après deux passes consécutives, statut change à "FINISHED"
  - [ ] POST `/games/{gameId}/pass` par noir → nextPlayer = "WHITE"
  - [ ] POST `/games/{gameId}/pass` par blanc → statut = "FINISHED"
  - [ ] Coups interdits après fin

**Critère acceptation :** Logique de fin de partie correcte, transitions d'états valides

---

### 2. Runbook Validé par l'Équipe

**Description :** Documentation complète et validée du processus de déploiement.

**Points de validation :**

#### 2.1 - Contenu du Runbook

- [ ] Instructions de build clairement documentées
  - [ ] Commande `mvnw.cmd clean compile` fonctionne
  - [ ] Commande `mvnw.cmd test` passe tous les tests
  - [ ] Commande `mvnw.cmd package` génère le JAR
  
- [ ] Instructions de démarrage par profil
  - [ ] Dev (port 8080) démarre sans erreur
  - [ ] Test (port 8081) démarre avec données initiales
  - [ ] Prod (port 8080) démarre en mode sécurisé
  
- [ ] Vérifications de santé documentées
  - [ ] `/api/health` décrit et testable
  - [ ] Authentification décrite avec exemples curl
  - [ ] Endpoints jeu décrits avec réponses attendues
  
- [ ] Troubleshooting inclus
  - [ ] Solutions pour "port déjà utilisé"
  - [ ] Solutions pour "Java version not found"
  - [ ] Solutions pour "Tests qui échouent"
  - [ ] Solutions pour erreurs JWT

#### 2.2 - Exécution du Runbook

- [ ] Runbook testé en environnement dev
  - [ ] Chaque commande s'exécute sans erreur
  - [ ] Chaque vérification retourne le résultat attendu
  - [ ] Ports corrects (8080, 8081)

- [ ] Runbook testé en environnement test
  - [ ] Application démarre correctement
  - [ ] Tests d'intégration passent
  - [ ] Données de test initialisées

- [ ] Runbook compréhensible par nouveau développeur
  - [ ] Prérequis clairs et vérifiables
  - [ ] Chaque étape numérotée et indépendante
  - [ ] Résultats attendus décrits
  - [ ] Solutions pour problèmes courants

#### 2.3 - Validation Équipe

- [ ] Runbook examiné et approuvé par au moins 1 autre développeur
  - [ ] Aucune ambiguïté détectée
  - [ ] Tous les cas d'erreur couverts
  - [ ] Instructions reproductibles

- [ ] Format et présentation satisfaisants
  - [ ] Markdown valide et bien formaté
  - [ ] Commandes copiables-collables
  - [ ] Exemples de réponse fournis
  - [ ] Liens vers ressources externes utiles

**Critère acceptation :** Runbook.md existe, contient tous les points, testé et validé

---

### 3. Checklist Démo Signée par l'Équipe

**Description :** Confirmation formelle que tous les critères de démo sont couverts et validés.

**Points de signature :**

#### 3.1 - Test E2E Complet

- [ ] Flux login → créer partie → effectuer coup → websocket exécuté avec succès
- [ ] Aucune erreur observée durant le flux
- [ ] Tous les états intermédiaires corrects
- [ ] Performance acceptable (<2s par opération)

#### 3.2 - Runbook Applicable

- [ ] Instructions claires et complètes
- [ ] Au moins un développeur l'a suivi avec succès
- [ ] Tous les prérequis vérifiables
- [ ] Troubleshooting couvert

#### 3.3 - Qualité du Code

- [ ] Build passe sans warnings ou erreurs
- [ ] Tests passent à 100%
- [ ] Checkstyle passe sans violations
- [ ] Pas de code legacy ou TODO critiques

#### 3.4 - Documentation

- [ ] README.md à jour
- [ ] RUNBOOK.md complet et testé
- [ ] Code commenté où nécessaire
- [ ] Endpoints documentés avec exemples

---

## 📋 Scénarios de Démo

### Scénario 1 : Happy Path Complet (10 minutes)

**Objectif :** Montrer le flux normal d'un utilisateur

```
1. Démarrage de l'app
2. Login avec demo/demo-password → obtenir JWT
3. Créer une partie 19x19
4. Faire 3-4 coups alternés (noir/blanc)
5. Ouvrir WebSocket et montrer les événements temps réel
6. Résumé de partie
```

**Énoncé attendu :**
- ✅ Application responsive
- ✅ Authentification transparente
- ✅ Partie jouable
- ✅ Synchronisation temps réel

---

### Scénario 2 : Gestion des Erreurs (5 minutes)

**Objectif :** Montrer la robustesse

```
1. Tentative login avec mauvais mot de passe → 401
2. Création de partie sans token → 401
3. Coup hors limites → 422
4. Coup sur position occupée → 422
5. Coup après fin de partie → 422
```

**Énoncé attendu :**
- ✅ Erreurs claires et documentées
- ✅ Codes HTTP appropriés
- ✅ Messages d'erreur utiles

---

### Scénario 3 : Concurrence et WebSocket (5 minutes)

**Objectif :** Montrer la synchronisation multi-client

```
1. Ouvrir 2 connexions WebSocket pour même partie
2. Client 1 fait un coup
3. Client 2 reçoit l'événement en temps réel
4. Montrer la cohérence des états
```

**Énoncé attendu :**
- ✅ Synchronisation temps réel fonctionnelle
- ✅ Pas de race condition
- ✅ États cohérents

---

## 👥 Signatures d'Approbation

### Validations Requises

| Validateur | Rôle | Signature | Date |
|------------|------|-----------|------|
| Dev 1      | Lead Dev | _________________ | _____ |
| Dev 2      | Reviewer | _________________ | _____ |
| QA         | QA Tester | _________________ | _____ |

### Approbation Finale

**Tous les critères acceptés ?** 
- [ ] OUI → Approuvé pour release
- [ ] NON → Éléments bloquants listés ci-dessous

**Éléments bloquants (si applicable) :**
```
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
```

---

## 📊 Métriques de Validation

| Métrique | Cible | Observé | ✅/❌ |
|----------|-------|---------|-------|
| Build time | < 2 min | _____ | ___ |
| Test time | < 1 min | _____ | ___ |
| Startup time | < 5s | _____ | ___ |
| Login response | < 500ms | _____ | ___ |
| Create game | < 1s | _____ | ___ |
| Make move | < 200ms | _____ | ___ |
| WebSocket latency | < 100ms | _____ | ___ |

---

## 🔗 Liens et Ressources

- **Runbook :** `RUNBOOK.md`
- **README :** `README.md`
- **Tests :** `src/test/java/com/appgo/`
- **API OpenAPI :** `openapi.yaml`

---

## 📝 Notes

**Observations pendant la validation :**
```
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
```

**Action items ou améliorations futures :**
```
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
```

---

**Validée le :** _______________  
**Version applicative :** 1.0.0-SNAPSHOT  
**Branche :** adrien0984/issue-16-sprint-1-t016-hardening-release-sprint-7a7edc
