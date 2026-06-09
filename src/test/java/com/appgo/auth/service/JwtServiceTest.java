package com.appgo.auth.service;

import com.appgo.auth.config.AuthProperties;
import com.appgo.auth.dto.TokenResponse;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private AuthProperties authProperties;

    @BeforeEach
    void setUp() {
        authProperties = new AuthProperties();
        authProperties.getJwt().setIssuer("test-issuer");
        authProperties.getJwt().setSecret("test-secret-test-secret-test-secret");
        authProperties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        authProperties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));

        jwtService = new JwtService(authProperties);
    }

    @Test
    @DisplayName("generateTokenResponse should create valid access and refresh tokens")
    void generateTokenResponseReturnsValidTokens() {
        TokenResponse response = jwtService.generateTokenResponse("testuser", new ArrayList<>());

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertNotNull(response.refreshToken());
        assertFalse(response.accessToken().isEmpty());
        assertFalse(response.refreshToken().isEmpty());
        assertNotEquals(response.accessToken(), response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(900L, response.accessTokenExpiresInSeconds());
        assertEquals(604800L, response.refreshTokenExpiresInSeconds());
    }

    @Test
    @DisplayName("verifyAccessToken should successfully decode valid access token")
    void verifyAccessTokenWithValidToken() {
        TokenResponse tokens = jwtService.generateTokenResponse("john", new ArrayList<>());

        DecodedJWT decoded = jwtService.verifyAccessToken(tokens.accessToken());

        assertNotNull(decoded);
        assertEquals("john", decoded.getSubject());
        assertEquals("test-issuer", decoded.getIssuer());
        assertEquals("access", decoded.getClaim("token_type").asString());
    }

    @Test
    @DisplayName("verifyRefreshToken should successfully decode valid refresh token")
    void verifyRefreshTokenWithValidToken() {
        TokenResponse tokens = jwtService.generateTokenResponse("jane", new ArrayList<>());

        DecodedJWT decoded = jwtService.verifyRefreshToken(tokens.refreshToken());

        assertNotNull(decoded);
        assertEquals("jane", decoded.getSubject());
        assertEquals("test-issuer", decoded.getIssuer());
        assertEquals("refresh", decoded.getClaim("token_type").asString());
    }

    @Test
    @DisplayName("verifyAccessToken should throw exception for invalid token")
    void verifyAccessTokenWithInvalidToken() {
        assertThrows(JWTVerificationException.class, () -> 
            jwtService.verifyAccessToken("invalid.token.here"));
    }

    @Test
    @DisplayName("verifyRefreshToken should throw exception for invalid token")
    void verifyRefreshTokenWithInvalidToken() {
        assertThrows(JWTVerificationException.class, () -> 
            jwtService.verifyRefreshToken("invalid.token.here"));
    }

    @Test
    @DisplayName("Different users should have different tokens")
    void differentUsersDifferentTokens() {
        TokenResponse tokens1 = jwtService.generateTokenResponse("user1", new ArrayList<>());
        TokenResponse tokens2 = jwtService.generateTokenResponse("user2", new ArrayList<>());

        assertNotEquals(tokens1.accessToken(), tokens2.accessToken());
        assertNotEquals(tokens1.refreshToken(), tokens2.refreshToken());

        DecodedJWT decoded1 = jwtService.verifyAccessToken(tokens1.accessToken());
        DecodedJWT decoded2 = jwtService.verifyAccessToken(tokens2.accessToken());

        assertEquals("user1", decoded1.getSubject());
        assertEquals("user2", decoded2.getSubject());
    }

    @Test
    @DisplayName("Access token and refresh token should be different even for same user")
    void accessAndRefreshTokensDifferentForSameUser() {
        TokenResponse tokens = jwtService.generateTokenResponse("user", new ArrayList<>());

        assertNotEquals(tokens.accessToken(), tokens.refreshToken());

        DecodedJWT accessDecoded = jwtService.verifyAccessToken(tokens.accessToken());
        DecodedJWT refreshDecoded = jwtService.verifyRefreshToken(tokens.refreshToken());

        assertEquals("access", accessDecoded.getClaim("token_type").asString());
        assertEquals("refresh", refreshDecoded.getClaim("token_type").asString());
    }

    @Test
    @DisplayName("Token with wrong secret should fail verification")
    void tokenWithWrongSecretShouldFail() {
        TokenResponse tokens = jwtService.generateTokenResponse("user", new ArrayList<>());

        // Create a new service with different secret
        AuthProperties wrongProperties = new AuthProperties();
        wrongProperties.getJwt().setIssuer("test-issuer");
        wrongProperties.getJwt().setSecret("different-secret-different-secret-diff");
        wrongProperties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        wrongProperties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));

        JwtService wrongService = new JwtService(wrongProperties);

        assertThrows(JWTVerificationException.class, () -> 
            wrongService.verifyAccessToken(tokens.accessToken()));
    }

    @Test
    @DisplayName("Token with wrong issuer should fail verification")
    void tokenWithWrongIssuerShouldFail() {
        TokenResponse tokens = jwtService.generateTokenResponse("user", new ArrayList<>());

        // Create a new service with different issuer
        AuthProperties wrongProperties = new AuthProperties();
        wrongProperties.getJwt().setIssuer("wrong-issuer");
        wrongProperties.getJwt().setSecret("test-secret-test-secret-test-secret");
        wrongProperties.getJwt().setAccessTokenTtl(Duration.ofMinutes(15));
        wrongProperties.getJwt().setRefreshTokenTtl(Duration.ofDays(7));

        JwtService wrongService = new JwtService(wrongProperties);

        assertThrows(JWTVerificationException.class, () -> 
            wrongService.verifyAccessToken(tokens.accessToken()));
    }

    @Test
    @DisplayName("Access token cannot be verified as refresh token")
    void accessTokenCannotBeVerifiedAsRefreshToken() {
        TokenResponse tokens = jwtService.generateTokenResponse("user", new ArrayList<>());

        // Try to verify access token as refresh token - should fail
        assertThrows(JWTVerificationException.class, () -> 
            jwtService.verifyRefreshToken(tokens.accessToken()));
    }

    @Test
    @DisplayName("Refresh token cannot be verified as access token")
    void refreshTokenCannotBeVerifiedAsAccessToken() {
        TokenResponse tokens = jwtService.generateTokenResponse("user", new ArrayList<>());

        // Try to verify refresh token as access token - should fail
        assertThrows(JWTVerificationException.class, () -> 
            jwtService.verifyAccessToken(tokens.refreshToken()));
    }

    @Test
    @DisplayName("generateTokenResponse with special characters in subject")
    void generateTokenResponseWithSpecialCharactersInSubject() {
        String specialUsername = "user-123_test@domain";
        TokenResponse response = jwtService.generateTokenResponse(specialUsername, new ArrayList<>());

        DecodedJWT decoded = jwtService.verifyAccessToken(response.accessToken());
        assertEquals(specialUsername, decoded.getSubject());
    }
}
