# App Go Mobile

Application mobile pour jouer au jeu de Go avec le backend App Go.

## Caractéristiques

- ✅ Affichage du plateau de jeu interactif (9x9, 13x13, 19x19)
- ✅ Placement de pierres par tap sur le plateau
- ✅ Action de passage du tour
- ✅ Affichage en temps réel de l'état du jeu
- ✅ Gestion des erreurs (coups illégaux, etc.)
- ✅ Authentification avec tokens JWT

## Structure du projet

```
mobile/
├── lib/
│   ├── main.dart                 # Point d'entrée de l'app
│   ├── screens/
│   │   └── game_screen.dart     # Écran principal du jeu
│   ├── widgets/
│   │   └── board_widget.dart    # Composant du plateau
│   ├── services/
│   │   ├── auth_service.dart    # Gestion de l'authentification
│   │   └── game_service.dart    # API du jeu
│   └── models/
│       └── game_state.dart      # Modèle de l'état du jeu
└── pubspec.yaml                 # Dépendances Flutter
```

## Prérequis

- Flutter SDK 3.0.0+
- Dart 3.0.0+
- Backend App Go en cours d'exécution

## Installation

### 1. Installer Flutter

Télécharger et installer [Flutter](https://flutter.dev/docs/get-started/install)

### 2. Cloner le projet

```bash
cd mobile
flutter pub get
```

### 3. Configurer l'API

Éditer `lib/main.dart` et `lib/screens/game_screen.dart` pour changer `baseUrl` si nécessaire :

```dart
GameService(
  baseUrl: 'http://localhost:8080',  // Remplacer par l'URL réelle
  getToken: () => authService.accessToken ?? '',
)
```

## Lancer l'application

### Sur un émulateur Android

```bash
flutter devices                    # Voir les appareils disponibles
flutter run -d <device_id>         # Lancer sur un appareil spécifique
```

### Sur un appareil iOS

```bash
open -a Simulator                  # Lancer le simulateur iOS
flutter run
```

### Sur le web (développement)

```bash
flutter run -d chrome
```

## Utilisation

### 1. Connexion

- **Utilisateur** : demo1
- **Mot de passe** : password1

Autres utilisateurs disponibles : demo2, demo3

### 2. Joindre une partie

Entrer l'ID de la partie et appuyer sur "Jouer"

### 3. Jouer

- **Placer une pierre** : Tapoter sur une intersection vide
- **Passer le tour** : Appuyer sur le bouton "Passer"
- **Terminer la partie** : 2 passages consécutifs terminent la partie

## API Backend utilisée

### Authentification

```
POST /auth/login
{
  "username": "demo1",
  "password": "password1"
}
```

### Jeu

```
POST /games
GET /games/{id}
POST /games/{id}/moves
POST /games/{id}/pass
```

## Gestion des erreurs

L'application affiche des messages d'erreur clairs :

- **Coup illégal** : Intersection occupée ou suicide
- **Partie non trouvée** : ID de partie invalide
- **Erreur réseau** : Vérifier la connexion au backend

## Points importants

1. **Plateau interactif** : Le plateau se redessine en temps réel
2. **Validation côté serveur** : Tous les coups sont validés par le backend
3. **État du jeu** : Affichage du joueur courant, nombre de coups, état (en cours/terminée)
4. **Passages** : 2 passages consécutifs terminent automatiquement la partie
5. **Scrollable** : Le contenu est scrollable sur petits écrans

## Développement

### Ajouter une nouvelle fonctionnalité

1. Créer le modèle dans `lib/models/`
2. Créer le service dans `lib/services/`
3. Créer la UI dans `lib/screens/` ou `lib/widgets/`
4. Tester l'intégration

### Tester l'application

```bash
flutter test
```

### Formater le code

```bash
dart format lib/
```

## Déploiement

### Build APK (Android)

```bash
flutter build apk --release
```

### Build IPA (iOS)

```bash
flutter build ipa --release
```

## Troubleshooting

### "Failed to connect to backend"

Vérifier que :
- Le backend est en cours d'exécution
- L'URL `baseUrl` est correcte
- La machine peut accéder au serveur (pare-feu, réseau, etc.)

### "Illegal move" constant

Vérifier que :
- L'intersection n'est pas déjà occupée
- La pierre n'est pas en suicide (pas de libertés)

### Émulateur très lent

Utiliser l'émulateur Android sous acceleration ou un appareil physique

## License

À définir

## Ressources

- [Flutter Documentation](https://flutter.dev/docs)
- [Dart Documentation](https://dart.dev/guides)
- [Provider Package](https://pub.dev/packages/provider)
- [HTTP Package](https://pub.dev/packages/http)
