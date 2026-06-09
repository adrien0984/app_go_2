package com.appgo.games.controller;

import com.appgo.games.dto.CreateGameRequest;
import com.appgo.games.dto.CreateGameResponse;
import com.appgo.games.dto.GameStateResponse;
import com.appgo.games.dto.MakeMoveRequest;
import com.appgo.games.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@Valid @RequestBody(required = false) CreateGameRequest request) {
        Integer boardSize = request == null ? null : request.boardSize();
        CreateGameResponse response = gameService.createGame(boardSize);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable("id") String gameId) {
        return gameService.getGameById(gameId);
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<GameStateResponse> makeMove(
            @PathVariable("id") String gameId,
            @Valid @RequestBody MakeMoveRequest request) {
        GameStateResponse response = gameService.playMove(gameId, request.row(), request.col());
        return ResponseEntity.ok(response);
    }
}
