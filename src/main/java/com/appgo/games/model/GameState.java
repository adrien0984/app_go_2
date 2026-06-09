package com.appgo.games.model;

import com.appgo.game.domain.CouleurPierre;
import com.appgo.game.domain.PartieGo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState {

    private static final String EMPTY_INTERSECTION = "EMPTY";
    private static final String BLACK_STONE = "BLACK";
    private static final String WHITE_STONE = "WHITE";

    private final String gameId;
    private final int boardSize;
    private final Instant createdAt;
    private final List<List<String>> board;
    private final String status;
    private final PartieGo partie;
    private String nextPlayer;
    private int moveCount;

    private GameState(String gameId, int boardSize, Instant createdAt, List<List<String>> board, String status, PartieGo partie) {
        this.gameId = gameId;
        this.boardSize = boardSize;
        this.createdAt = createdAt;
        this.board = board;
        this.status = status;
        this.partie = partie;
        this.nextPlayer = "BLACK";
        this.moveCount = 0;
    }

    public static GameState createNew(int boardSize) {
        PartieGo partie = new PartieGo(boardSize);
        return new GameState(
                UUID.randomUUID().toString(),
                boardSize,
                Instant.now(),
                buildEmptyBoard(boardSize),
                "IN_PROGRESS",
                partie);
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

    public boolean isMoveLegal(int row, int col) {
        return partie.estCoupLegal(row, col);
    }

    public void applyMove(int row, int col) {
        partie.jouer(row, col);
        updateBoardFromPartie();
        moveCount++;
        nextPlayer = partie.getJoueurCourant() == CouleurPierre.NOIR ? "BLACK" : "WHITE";
    }

    private void updateBoardFromPartie() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                CouleurPierre couleur = partie.pierreA(row, col);
                if (couleur == null) {
                    board.get(row).set(col, EMPTY_INTERSECTION);
                } else if (couleur == CouleurPierre.NOIR) {
                    board.get(row).set(col, BLACK_STONE);
                } else {
                    board.get(row).set(col, WHITE_STONE);
                }
            }
        }
    }

    public PartieGo getPartie() {
        return partie;
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
