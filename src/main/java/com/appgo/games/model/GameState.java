package com.appgo.games.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState {

    private static final String EMPTY_INTERSECTION = "EMPTY";

    private final String gameId;
    private final int boardSize;
    private final Instant createdAt;
    private final List<List<String>> board;
    private final String status;
    private String nextPlayer;
    private int moveCount;

    private GameState(String gameId, int boardSize, Instant createdAt, List<List<String>> board, String status) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.createdAt = createdAt;
        this.board = board;
        this.status = status;
        this.nextPlayer = "BLACK";
        this.moveCount = 0;
    }

    public static GameState createNew(int boardSize) {
        return new GameState(
                UUID.randomUUID().toString(),
                boardSize,
                Instant.now(),
                buildEmptyBoard(boardSize),
                "IN_PROGRESS");
    }

    private static List<List<String>> buildEmptyBoard(int boardSize) {
        List<List<String>> grid = new ArrayList<>(boardSize);
        for (int row = 0; row < boardSize; row++) {
            List<String> line = new ArrayList<>(boardSize);
            for (int col = 0; col < boardSize; col++) {
                line.add(EMPTY_INTERSECTION);
            }
            grid.add(line);
        }
        return grid;
    }

    public String getGameId() {
        return gameId;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<List<String>> getBoard() {
        return board;
    }

    public String getStatus() {
        return status;
    }

    public String getNextPlayer() {
        return nextPlayer;
    }

    public int getMoveCount() {
        return moveCount;
    }
}
