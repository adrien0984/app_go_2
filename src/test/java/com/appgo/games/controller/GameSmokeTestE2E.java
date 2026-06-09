package com.appgo.games.controller;

import com.appgo.auth.config.AuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Smoke Test E2E : Validate the complete user flow
 * Login -> Create Game -> Make Move -> Pass -> Game End
 * 
 * This test is critical for release validation (Sprint Hardening - T016)
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("Smoke Test E2E - Complete User Flow")
class GameSmokeTestE2E {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthProperties authProperties;

    @Test
    @DisplayName("E2E Smoke Test: Login -> Create Game -> Make Moves -> Game Ends")
    void completeGameFlowE2E() throws Exception {
        // ========== PHASE 1: AUTHENTICATION ==========
        var loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(authProperties.getDemo().getUsername(), authProperties.getDemo().getPassword())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("accessToken").asText();
        String refreshToken = loginBody.get("refreshToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        // ========== PHASE 2: HEALTH CHECK ==========
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // ========== PHASE 3: PROTECTED ROUTE CHECK ==========
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value(authProperties.getDemo().getUsername()));

        // ========== PHASE 4: CREATE GAME ==========
        var createGameResult = mockMvc.perform(post("/games")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "boardSize": 19
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").exists())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createGameResult.getResponse().getContentAsString());
        String gameId = createBody.get("gameId").asText();

        assertThat(gameId).isNotBlank().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

        // ========== PHASE 5: VERIFY GAME STATE ==========
        mockMvc.perform(get("/games/{id}", gameId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.boardSize").value(19))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.nextPlayer").value("BLACK"))
                .andExpect(jsonPath("$.moveCount").value(0));

        // ========== PHASE 6: MAKE MOVES ==========
        // BLACK makes first move
        var move1Result = mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 3,
                          "col": 3
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.nextPlayer").value("WHITE"))
                .andExpect(jsonPath("$.moveCount").value(1))
                .andReturn();

        // WHITE makes second move
        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 15,
                          "col": 15
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("BLACK"))
                .andExpect(jsonPath("$.moveCount").value(2));

        // BLACK makes third move
        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 9,
                          "col": 9
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("WHITE"))
                .andExpect(jsonPath("$.moveCount").value(3));

        // ========== PHASE 7: TEST PASS MOVES ==========
        // WHITE passes
        mockMvc.perform(post("/games/{id}/pass", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("BLACK"))
                .andExpect(jsonPath("$.moveCount").value(3)); // Move count should not change

        // BLACK passes to end game
        mockMvc.perform(post("/games/{id}/pass", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));

        // ========== PHASE 8: VERIFY GAME ENDED ==========
        mockMvc.perform(get("/games/{id}", gameId)
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.moveCount").value(3));

        // ========== PHASE 9: TEST TOKEN REFRESH ==========
        var refreshResult = mockMvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "refreshToken": "%s"
                        }
                        """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        JsonNode refreshBody = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        String newAccessToken = refreshBody.get("accessToken").asText();
        assertThat(newAccessToken).isNotBlank();

        // ========== PHASE 10: VERIFY NEW TOKEN WORKS ==========
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value(authProperties.getDemo().getUsername()));

        // ========== PHASE 11: VERIFY CANNOT MOVE AFTER GAME ENDS ==========
        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 5,
                          "col": 5
                        }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    @DisplayName("Error Handling: Invalid moves are rejected")
    void errorHandlingForInvalidMoves() throws Exception {
        String accessToken = loginAndGetToken();

        // Create game
        var createResult = mockMvc.perform(post("/games")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "boardSize": 9
                        }
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String gameId = createBody.get("gameId").asText();

        // Test: Move out of bounds
        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 100,
                          "col": 100
                        }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));

        // Test: Duplicate move on same position
        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 3,
                          "col": 3
                        }
                        """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/games/{id}/moves", gameId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "row": 3,
                          "col": 3
                        }
                        """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("UNPROCESSABLE_ENTITY"));
    }

    @Test
    @DisplayName("Authentication: Invalid/missing token is rejected")
    void authenticationValidation() throws Exception {
        // Test: Missing authorization header
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());

        // Test: Invalid token format
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer invalid.token.format"))
                .andExpect(status().isUnauthorized());

        // Test: Wrong credentials
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "demo",
                          "password": "wrong-password"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private String loginAndGetToken() throws Exception {
        var result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """.formatted(authProperties.getDemo().getUsername(), authProperties.getDemo().getPassword())))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
