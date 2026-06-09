# App Go - Backend

Backend pour l'application App Go, construite avec Spring Boot 3.3.0 et Java 17.

## Architecture

```
src/
├── main/
│   ├── java/com/appgo/
│   │   ├── api/
│   │   │   └── controller/        # Controllers REST
│   │   ├── shared/
│   │   │   └── error/             # Gestion globale des erreurs
│   │   └── BackendApplication.java
│   └── resources/
│       ├── application.yml        # Configuration commune
│       ├── application-dev.yml    # Profil développement
│       ├── application-test.yml   # Profil test
│       └── application-prod.yml   # Profil production
└── test/
    └── java/com/appgo/            # Tests unitaires et intégration
```

## Prérequis

- **Java 17 ou supérieur** : [Télécharger JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
- **Maven 3.9.6+** : Utilisé via le wrapper `mvnw` (inclus)
- **Git** : Pour cloner le repository

## Installation et Configuration

### 1. Cloner le repository

```bash
git clone <repository-url>
cd app_go_2
```

### 2. Vérifier Java

```bash
java -version
```

## Commandes de Build et Test

### Compiler le projet

```bash
# Linux/Mac
./mvnw clean compile

# Windows
mvnw.cmd clean compile
```

### Exécuter les tests

```bash
# Linux/Mac
./mvnw test

# Windows
mvnw.cmd test
```

### Construire le JAR

```bash
# Linux/Mac
./mvnw clean package

# Windows
mvnw.cmd clean package
```

## Démarrage Local

### Profil Développement (par défaut)

```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

L'application démarre sur `http://localhost:8080`

### Profil Test

```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=test"
```

L'application démarre sur `http://localhost:8081`

### Profil Production

```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

L'application démarre sur `http://localhost:8080`

## Endpoints

### Health Check

```bash
curl http://localhost:8080/api/health
```

Réponse attendue (HTTP 200):
```json
{
  "status": "UP",
  "message": "Application is running"
}
```

### Auth JWT

#### Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"demo-password"}'
```

#### Refresh

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refresh-token>"}'
```

#### Route protégée

```bash
curl http://localhost:8080/api/protected \
  -H "Authorization: Bearer <access-token>"
```

#### Réponse

```json
{
  "accessToken": "<jwt>",
  "refreshToken": "<jwt>",
  "tokenType": "Bearer",
  "accessTokenExpiresInSeconds": 900,
  "refreshTokenExpiresInSeconds": 604800
}
```

### Games

#### Créer une partie

```bash
curl -X POST http://localhost:8080/games \
  -H "Authorization: Bearer <access-token>" \
  -H "Content-Type: application/json" \
  -d '{"boardSize":19}'
```

Réponse (HTTP 201):
```json
{
  "gameId": "<uuid>"
}
```

#### Lire l'état d'une partie

```bash
curl http://localhost:8080/games/<game-id> \
  -H "Authorization: Bearer <access-token>"
```

Réponse (HTTP 200):
```json
{
  "gameId": "<uuid>",
  "boardSize": 19,
  "status": "IN_PROGRESS",
  "nextPlayer": "BLACK",
  "moveCount": 0,
  "createdAt": "2026-06-09T20:00:00Z",
  "board": [
    ["EMPTY", "..."]
  ]
}
```

## Gestion des Erreurs

L'API utilise un format d'erreur standardisé :

```json
{
  "code": "BAD_REQUEST",
  "message": "Validation failed",
  "timestamp": "2026-06-09T20:15:41.184+02:00",
  "details": [
    {
      "field": "username",
      "code": "BAD_REQUEST",
      "message": "Username is required"
    }
  ],
  "request_id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Codes d'erreur

- `BAD_REQUEST` (400) : Requête invalide ou validation échouée
- `UNAUTHORIZED` (401) : Authentification requise
- `FORBIDDEN` (403) : Accès refusé
- `NOT_FOUND` (404) : Ressource non trouvée
- `CONFLICT` (409) : Conflit (ex: duplication)
- `UNPROCESSABLE_ENTITY` (422) : Entité non traitée
- `INTERNAL_SERVER_ERROR` (500) : Erreur serveur interne

## Logging

Les logs sont configurés par profil :

- **dev** : Niveau DEBUG pour `com.appgo`, INFO pour le reste
- **test** : Niveau INFO pour `com.appgo`, WARN pour le reste
- **prod** : Niveau WARN pour tout

Voir les fichiers `application-*.yml` pour les configurations détaillées.

## Développement

### Ajouter une nouvelle dépendance

Éditer `pom.xml` et ajouter la dépendance :

```xml
<dependency>
    <groupId>org.example</groupId>
    <artifactId>my-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

Puis recharger les dépendances :

```bash
./mvnw clean install
```

### Conventions de Code

- Utiliser Lombok pour réduire le boilerplate
- Nommer les packages en camelCase (`com.appgo.api.controller`)
- Utiliser les annotations Spring (`@RestController`, `@Service`, etc.)
- Ajouter des tests pour chaque contrôleur et service
- Documenter les classes publiques avec JavaDoc

## Critères d'Acceptation

✓ `./mvnw test` passe sans erreur  
✓ `/health` retourne HTTP 200  
✓ Guide de démarrage local validé  

## Troubleshooting

### "mvnw command not found"

**Linux/Mac** : Rendre le script exécutable :
```bash
chmod +x mvnw
```

**Windows** : Utiliser `mvnw.cmd` à la place de `mvnw`

### "Java version not found"

Vérifier que Java 17+ est installé :
```bash
java -version
```

Si absent, télécharger depuis [oracle.com](https://www.oracle.com/java/technologies/downloads/)

### Tests qui échouent

1. Nettoyer le cache Maven :
   ```bash
   ./mvnw clean
   ```

2. Réinstaller les dépendances :
   ```bash
   ./mvnw install -DskipTests
   ```

3. Relancer les tests :
   ```bash
   ./mvnw test
   ```

## Ressources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Maven Documentation](https://maven.apache.org/)
- [Java 17 Features](https://docs.oracle.com/en/java/javase/17/)

## License

À définir
