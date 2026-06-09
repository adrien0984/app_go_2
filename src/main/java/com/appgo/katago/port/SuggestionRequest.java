package com.appgo.katago.port;

/**
 * Requête de suggestion de coup pour KataGo.
 */
public class SuggestionRequest {

    private final int[][] boardState;
    private final String currentPlayer;
    private final int boardSize;

    public SuggestionRequest(int[][] boardState, String currentPlayer, int boardSize) {
        this.boardState = boardState;
        this.currentPlayer = currentPlayer;
        this.boardSize = boardSize;
    }

    public int[][] getBoardState() {
        return boardState;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public int getBoardSize() {
        return boardSize;
    }
}
