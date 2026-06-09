package com.appgo.games.dto;

import java.time.Instant;
import java.util.List;

public record GameStateResponse(
        String gameId,
        int boardSize,
        String status,
        String nextPlayer,
        int moveCount,
        Instant createdAt,
        List<List<String>> board) {
}
