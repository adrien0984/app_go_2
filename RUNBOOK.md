# Runbook App Go - Backend

## 🚀 Démarrage et Déploiement

### Prérequis

- **Java 17+** : Vérifier avec `java -version`
- **Maven 3.9.6+** : Utilisé via wrapper `mvnw.cmd` (inclus)
- **Git** : Pour les opérations de versioning

### Vérification de l'environnement

```bash
java -version
# Attendu: openjdk version "17.x" ou supérieur

mvnw.cmd -version
# Attendu: Apache Maven 3.9.6+
```

## 📦 Build et Compilation

### 1. Nettoyage et compilation

```bash
mvnw.cmd clean compile
```

Sortie attendue :
- Aucune erreur de compilation
- Les dépendances sont téléchargées et cachées

### 2. Exécution des tests

```bash
mvnw.cmd test
```

Sortie attendue :
- Tous les tests passent
- Pas d'erreurs liées aux données
- Rapports de couverture de code (optionnel)

### 3. Build du JAR

```bash
mvnw.cmd clean package
```

Résultat :
- JAR généré dans `target/backend-1.0.0-SNAPSHOT.jar`
- ~50-100 MB de taille

## 🏃 Démarrage de l'Application

### Profil Développement (PORT 8080)

**Usage :** Développement local, debug actif

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Logs attendus :
```
2026-06-09 20:00:00.000 INFO - Started BackendApplication in 3.5s
2026-06-09 20:00:01.000 INFO - Tomcat started on port(s): 8080
```

### Profil Test (PORT 8081)

**Usage :** Tests d'intégration, validation en environnement similaire à la prod

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

Logs attendus :
```
2026-06-09 20:00:00.000 INFO - Started BackendApplication in 3.5s
2026-06-09 20:00:01.000 INFO - Tomcat started on port(s): 8081
2026-06-09 20:00:02.000 INFO - Database initialized with test data
```

### Profil Production (PORT 8080)

**Usage :** Déploiement en production

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

Logs attendus :
```
2026-06-09 20:00:00.000 WARN - Started BackendApplication in 4.0s
2026-06-09 20:00:01.000 WARN - Tomcat started on port(s): 8080
```

## ✅ Vérifications de Santé

### 1. Health Check

```bash
curl http://localhost:8080/api/health
```

Réponse attendue (HTTP 200) :
```json
{
  "status": "UP",
  "message": "Application is running"
}
```

### 2. Authentication

**Login**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo",
    "password": "demo-password"
  }'
```

Réponse attendue (HTTP 200) :
```json
{
  "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "refreshToken": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "tokenType": "Bearer",
  "accessTokenExpiresInSeconds": 900,
  "refreshTokenExpiresInSeconds": 604800
}
```

**Accès à route protégée**
```bash
BEARER_TOKEN="<accessToken from login response>"

curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer $BEARER_TOKEN"
```

Réponse attendue (HTTP 200) :
```json
{
  "user": "demo"
}
```

### 3. Création de Partie (Jeu)

```bash
BEARER_TOKEN="<accessToken from login response>"

curl -X POST http://localhost:8080/games \
  -H "Authorization: Bearer $BEARER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "boardSize": 19
  }'
```

Réponse attendue (HTTP 201) :
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 4. État de la Partie

```bash
BEARER_TOKEN="<accessToken from login response>"
GAME_ID="<gameId from create response>"

curl http://localhost:8080/games/$GAME_ID \
  -H "Authorization: Bearer $BEARER_TOKEN"
```

Réponse attendue (HTTP 200) :
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "boardSize": 19,
  "status": "IN_PROGRESS",
  "nextPlayer": "BLACK",
  "moveCount": 0,
  "createdAt": "2026-06-09T20:00:00Z"
}
```

### 5. Effectuer un Coup

```bash
BEARER_TOKEN="<accessToken from login response>"
GAME_ID="<gameId from create response>"

curl -X POST http://localhost:8080/games/$GAME_ID/moves \
  -H "Authorization: Bearer $BEARER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "row": 3,
    "col": 3
  }'
```

Réponse attendue (HTTP 200) :
```json
{
  "gameId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS",
  "nextPlayer": "WHITE",
  "moveCount": 1
}
```

### 6. WebSocket - Connexion Temps Réel

```bash
BEARER_TOKEN="<accessToken from login response>"
GAME_ID="<gameId>"

# Avec wscat (npm install -g wscat)
wscat -c "ws://localhost:8080/ws/$GAME_ID" \
  -H "Authorization: Bearer $BEARER_TOKEN"

# Ou avec curl
curl -i -N -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
  -H "Authorization: Bearer $BEARER_TOKEN" \
  http://localhost:8080/ws/$GAME_ID
```

Flux attendu :
1. Connexion ACK reçu
2. Messages de mouvements des joueurs
3. Messages de fin de partie

## 🐛 Troubleshooting

### Port déjà utilisé

**Symptôme :** `Address already in use: bind`

**Solution :**
```bash
# Windows - Trouver le processus utilisant le port 8080
netstat -ano | findstr :8080

# Récupérer le PID et le tuer
taskkill /PID <PID> /F

# OU démarrer sur un port différent
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8090 --spring.profiles.active=dev"
```

### Erreur "Java version not found"

**Symptôme :** `java: command not found` ou version incompatible

**Solution :**
```bash
# Vérifier Java
java -version

# Si absent, installer depuis https://www.oracle.com/java/technologies/downloads/
# Ou utiliser un gestionnaire de versions (jenv, SDKMAN)
```

### Tests qui échouent

**Symptôme :** `[ERROR] Tests run: X, Failures: Y`

**Solution :**
```bash
# 1. Nettoyer le cache
mvnw.cmd clean

# 2. Réinstaller sans tests
mvnw.cmd install -DskipTests

# 3. Relancer les tests
mvnw.cmd test

# 4. Vérifier la base de données (H2 en test)
# Pas d'action requise - H2 est in-memory
```

### Application ne démarre pas

**Symptôme :** `Failed to start application context`

**Solution :**
1. Vérifier les logs pour l'erreur spécifique
2. S'assurer que la base de données est accessible (H2 local)
3. Vérifier les configurations dans `application-<profile>.yml`
4. Relancer avec `--debug` : 
   ```bash
   mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --debug"
   ```

### Erreur JWT/Authentification

**Symptôme :** `401 Unauthorized`, token invalide

**Solution :**
1. Vérifier que le login a retourné un `accessToken`
2. Vérifier que le token est formaté comme `Bearer <token>`
3. Vérifier que le token n'a pas expiré (expiration = 900 secondes = 15 minutes)
4. Rafraîchir le token avec `/auth/refresh` et le `refreshToken`

```bash
REFRESH_TOKEN="<refreshToken from login>"

curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\": \"$REFRESH_TOKEN\"}"
```

## 📊 Monitoring et Logs

### Chemins des logs

- **Dev :** Console + logs de debug
- **Test :** Console + logs informatifs
- **Prod :** Fichier + logs de warning

### Niveaux de log par profil

| Profil | Niveau | Package `com.appgo` | Reste |
|--------|--------|---------------------|-------|
| dev    | DEBUG  | DEBUG               | INFO  |
| test   | INFO   | INFO                | WARN  |
| prod   | WARN   | WARN                | WARN  |

### Activer le debug

```bash
mvnw.cmd spring-boot:run \
  -Dspring-boot.run.arguments="--spring.profiles.active=dev --debug" \
  --debug
```

## 🔄 Cycle de Mise à Jour

### 1. Code Source

```bash
# Récupérer les dernières modifications
git pull origin main

# Vérifier les changements
git status
```

### 2. Dépendances

```bash
# Mettre à jour les dépendances Maven
mvnw.cmd dependency:resolve

# Vérifier les nouvelles versions
mvnw.cmd versions:display-dependency-updates
```

### 3. Validation

```bash
# Compiler et tester
mvnw.cmd clean test

# Si succès, builder
mvnw.cmd clean package
```

### 4. Déploiement

```bash
# Arrêter l'ancienne instance (Ctrl+C ou kill)

# Démarrer la nouvelle version
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

## 📝 Checklist de Déploiement

- [ ] Java 17+ installé et vérifié
- [ ] Git cloné et à jour (`git pull`)
- [ ] Build passe : `mvnw.cmd clean package`
- [ ] Tests passent : `mvnw.cmd test`
- [ ] Port 8080/8081 libre (profil correspondant)
- [ ] Variable d'environnement `SPRING_PROFILES_ACTIVE` définie (optionnel)
- [ ] Application démarre sans erreur
- [ ] `/api/health` retourne HTTP 200
- [ ] Login avec `demo/demo-password` fonctionne
- [ ] Partie peut être créée et jouée
- [ ] WebSocket se connecte pour les mises à jour temps réel

## 🆘 Support et Ressources

- **Documentation Spring Boot :** https://spring.io/projects/spring-boot
- **JWT Reference :** https://jwt.io/
- **Go Rules :** https://www.usgo.org/usa-go-foundation
- **Architecture du projet :** `./README.md`

---

**Dernière mise à jour :** 2026-06-10  
**Version :** 1.0.0-SNAPSHOT  
**Responsable :** Dev Team
