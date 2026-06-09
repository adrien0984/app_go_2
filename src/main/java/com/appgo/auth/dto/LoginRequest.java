package com.appgo.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Requête de connexion.
 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password) {
}
