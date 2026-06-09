# App Go - Mobile App

A React Native mobile application built with Expo for the App Go Go game platform. Features JWT-based authentication, environment management, and protected navigation.

## Features

- **JWT Authentication**: Secure login with token-based authentication
- **Token Persistence**: Automatic token storage and retrieval using AsyncStorage
- **Protected Navigation**: Automatic redirection based on authentication state
- **Environment Management**: Support for dev and production configurations
- **API Integration**: Axios-based HTTP client with interceptors for token injection
- **Responsive UI**: Built with React Native for iOS and Android

## Quick Start

### Prerequisites

- Node.js 16+ and npm 8+
- Expo Go app on your phone (optional, for testing without emulator)

### Installation

```bash
npm install
```

### Start Development Server

```bash
npm start
```

Options:
- **Scan QR code** with Expo Go app
- **Press `a`** to open Android emulator
- **Press `i`** to open iOS simulator
- **Press `w`** to open in web browser

## Configuration

Environment variables are automatically loaded based on build mode:
- **Development** (`.env.dev`): Backend at `http://localhost:8080`
- **Production** (`.env.prod`): Backend at `https://api.appgo.com`

## Project Structure

```
src/
├── app/                    # Expo Router pages
│   ├── (auth)/            # Authentication screens
│   │   └── login.tsx      # Login screen
│   ├── (app)/             # Protected screens
│   │   └── home.tsx       # Home screen
│   └── _layout.tsx        # Root layout
├── contexts/              # Authentication context
├── screens/               # Screen components
├── services/              # API & storage
├── types/                 # TypeScript definitions
└── utils/                 # Configuration
```

## Authentication

### Test Credentials

- **Username**: `demo`
- **Password**: `demo-password`

### Flow

1. App checks for stored token on startup
2. No token → Redirect to `/login`
3. Login → Call `/auth/login` API
4. Success → Store tokens, redirect to `/home`
5. Valid token → Show `/home` with logout option
6. Logout → Clear tokens, redirect to `/login`

## API Endpoints

Backend runs on `http://localhost:8080` (dev)

- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh token
- `GET /health` - Health check

## Running on Devices

### Android Emulator
```bash
npm run android
```

### iOS Simulator (macOS only)
```bash
npm run ios
```

### Web Browser
```bash
npm run web
```

## Development

### Hot Reload
Automatic on file save. Press `r` to manually reload.

### Debugging
Press `j` to open Expo DevTools with console and network inspector.

### ESLint
```bash
npm run lint
```

## Building for Production

```bash
eas build --platform android
eas build --platform ios
```

Requires [Expo Application Services](https://docs.expo.dev/eas/) account.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| App won't start | Run `npm start -c` to clear cache |
| Dependencies missing | Run `npm install` again |
| API connection fails | Check `API_URL` in env files, ensure backend is running |
| Token not saving | Verify AsyncStorage is installed, check storage permissions |

## Resources

- [Expo Docs](https://docs.expo.dev/)
- [React Native Docs](https://reactnative.dev/)
- [Expo Router Guide](https://docs.expo.dev/routing/introduction/)

## Backend

See parent [README.md](../README.md) for backend setup and API documentation.
