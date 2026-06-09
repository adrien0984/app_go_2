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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
@DisplayName("GameController Integration Tests for Game Flow")
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthProperties authProperties;

    @Test
    @DisplayName("Make pass move on a game")
    void makePassMoveReturnsUpdatedGameState() throws Exception {
        String accessToken = loginAndReadAccessToken();

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

        // Make a pass move
        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.nextPlayer").value("WHITE"));
    }

    @Test
    @DisplayName("Game ends after two consecutive passes")
    void gameEndsAfterTwoConsecutivePasses() throws Exception {
        String accessToken = loginAndReadAccessToken();

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

        // First pass by black
        var firstPass = mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstPassBody = objectMapper.readTree(firstPass.getResponse().getContentAsString());
        String status1 = firstPassBody.get("status").asText();
        assert status1.equals("IN_PROGRESS");

        // Second pass by white
        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"));
    }

    @Test
    @DisplayName("Cannot pass on non-existent game")
    void passOnNonExistentGameReturns404() throws Exception {
        String accessToken = loginAndReadAccessToken();

        mockMvc.perform(post("/games/{id}/pass", "non-existent")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Pass move without authorization returns 401")
    void passMoveWithoutAuthorizationReturns401() throws Exception {
        mockMvc.perform(post("/games/{id}/pass", "some-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cannot make move after game has ended")
    void cannotMakeMoveAfterGameEnds() throws Exception {
        String accessToken = loginAndReadAccessToken();

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

        // Two passes to finish the game
        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Try to make a move after game is finished
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
    @DisplayName("Game flow: move then pass then move")
    void gameFlowWithMixedMovesAndPasses() throws Exception {
        String accessToken = loginAndReadAccessToken();

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

        // Black makes a move
        mockMvc.perform(post("/games/{id}/moves", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "row": 3,
                                  "col": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("WHITE"))
                .andExpect(jsonPath("$.moveCount").value(1));

        // White passes
        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("BLACK"));

        // Black makes another move
        mockMvc.perform(post("/games/{id}/moves", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "row": 5,
                                  "col": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextPlayer").value("WHITE"))
                .andExpect(jsonPath("$.moveCount").value(2));
    }

    @Test
    @DisplayName("Pass move does not change move count")
    void passMoveDoesNotIncrementMoveCount() throws Exception {
        String accessToken = loginAndReadAccessToken();

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

        // Make a move
        mockMvc.perform(post("/games/{id}/moves", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "row": 3,
                                  "col": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moveCount").value(1));

        // White passes
        mockMvc.perform(post("/games/{id}/pass", gameId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moveCount").value(1));
    }

    private String loginAndReadAccessToken() throws Exception {
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
