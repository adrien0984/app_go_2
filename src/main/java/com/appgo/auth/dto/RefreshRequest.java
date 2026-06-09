package com.appgo.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de rafraîchissement.
 */
public record RefreshRequest(
        @NotBlank String refreshToken) {
}
