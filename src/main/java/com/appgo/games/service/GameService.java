package com.appgo.games.service;

import com.appgo.games.dto.CreateGameResponse;
import com.appgo.games.dto.GameStateResponse;
import com.appgo.games.exception.GameNotFoundException;
import com.appgo.games.model.GameState;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    private static final int DEFAULT_BOARD_SIZE = 19;

    private final Map<String, GameState> gamesById = new ConcurrentHashMap<>();

    public CreateGameResponse createGame(Integer requestedBoardSize) {
        int boardSize = requestedBoardSize == null ? DEFAULT_BOARD_SIZE : requestedBoardSize;
        GameState game = GameState.createNew(boardSize);
        gamesById.put(game.getGameId(), game);
        return new CreateGameResponse(game.getGameId());
    }

    public GameStateResponse getGameById(String gameId) {
        GameState game = gamesById.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
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
