package com.appgo.games.controller;

import com.appgo.auth.config.AuthProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthProperties authProperties;

    @Test
    void createGameReturnsGameId() throws Exception {
        String accessToken = loginAndReadAccessToken();

        mockMvc.perform(post("/games")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").exists());
    }

    @Test
    void getGameByIdReturnsStateForExistingGame() throws Exception {
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

        mockMvc.perform(get("/games/{id}", gameId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.boardSize").value(9))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.nextPlayer").value("BLACK"))
                .andExpect(jsonPath("$.moveCount").value(0))
                .andExpect(jsonPath("$.board").isArray())
                .andExpect(jsonPath("$.board.length()").value(9))
                .andExpect(jsonPath("$.board[0].length()").value(9))
                .andExpect(jsonPath("$.board[0][0]").value("EMPTY"));
    }

    @Test
    void getGameByIdReturnsNotFoundWhenGameDoesNotExist() throws Exception {
        String accessToken = loginAndReadAccessToken();

        mockMvc.perform(get("/games/{id}", "unknown-id")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
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
