package com.appgo.games.service;

import com.appgo.games.dto.CreateGameResponse;
import com.appgo.games.dto.GameStateResponse;
import com.appgo.games.exception.GameNotFoundException;
import com.appgo.games.exception.IllegalMoveException;
import com.appgo.games.model.GameState;
import com.appgo.shared.observability.BusinessMetrics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final int DEFAULT_BOARD_SIZE = 19;

    private final Map<String, GameState> gamesById = new ConcurrentHashMap<>();
    private final BusinessMetrics businessMetrics;

    public GameService(BusinessMetrics businessMetrics) {
        this.businessMetrics = businessMetrics;
    }

    public CreateGameResponse createGame(Integer requestedBoardSize) {
        int boardSize = requestedBoardSize == null ? DEFAULT_BOARD_SIZE : requestedBoardSize;
        GameState game = GameState.createNew(boardSize);
        gamesById.put(game.getGameId(), game);
        businessMetrics.recordGameCreated();
        return new CreateGameResponse(game.getGameId());
    }

    public GameStateResponse getGameById(String gameId) {
        GameState game = gamesById.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return toResponse(game);
    }

    public GameStateResponse playMove(String gameId, int row, int col) {
        GameState game = gamesById.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }

        if (!game.isMoveLegal(row, col)) {
            throw new IllegalMoveException("Illegal move at position [" + row + ", " + col + "]");
        }

        var sample = businessMetrics.recordMoveExecutionStart();
        try {
            game.applyMove(row, col);
            businessMetrics.recordGameMove();
            businessMetrics.recordMoveExecutionStop(sample);
        } catch (IllegalArgumentException e) {
            businessMetrics.recordMoveExecutionStop(sample);
            throw new IllegalMoveException(e.getMessage(), e);
        }

        return toResponse(game);
    }

    public GameStateResponse passMove(String gameId) {
        GameState game = gamesById.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }

        try {
            game.passMove();
            businessMetrics.recordGamePass();
        } catch (IllegalStateException e) {
            throw new IllegalMoveException(e.getMessage(), e);
        }

        return toResponse(game);
    }

    private GameStateResponse toResponse(GameState game) {
        List<List<String>> board = game.getBoard().stream()
                .map(List::copyOf)
                .toList();

        return new GameStateResponse(
                game.getGameId(),
                game.getBoardSize(),
                game.getStatus(),
                game.getNextPlayer(),
                game.getMoveCount(),
                game.getCreatedAt(),
                board);
    }
}
