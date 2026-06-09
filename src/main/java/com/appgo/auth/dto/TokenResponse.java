package com.appgo.auth.dto;

/**
 * Réponse contenant les jetons JWT.
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresInSeconds,
        long refreshTokenExpiresInSeconds) {
}
