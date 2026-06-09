package com.appgo.auth.controller;

import com.appgo.auth.dto.LoginRequest;
import com.appgo.auth.dto.RefreshRequest;
import com.appgo.auth.dto.TokenResponse;
import com.appgo.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints d'authentification.
 * 
 * Tous les endpoints retournent un header X-Correlation-Id pour le suivi des requêtes.
 * 
 * Endpoints key:
 * - POST /auth/login - Authentification et génération de tokens JWT
 * - POST /auth/refresh - Renouvellement du token d'accès
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authentifie un utilisateur et génère des tokens JWT.
     * 
     * @param request les identifiants de l'utilisateur (username, password)
     * @return les tokens d'accès et de rafraîchissement
     */
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Renouvelle le token d'accès en utilisant le refresh token.
     * 
     * @param request contient le refresh token
     * @return un nouveau token d'accès
     */
    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }
}
