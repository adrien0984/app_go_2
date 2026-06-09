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

/**
 * Contrôleur pour la gestion des parties de Go.
 * 
 * Tous les endpoints retournent un header X-Correlation-Id pour le suivi des requêtes.
 * Les opérations clés (création de partie, mouvements) sont tracées via des métriques disponibles sur /actuator/metrics.
 */
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Crée une nouvelle partie de Go.
     * 
     * @param request paramètres optionnels (boardSize)
     * @return la nouvelle partie créée avec son ID unique
     */
    @PostMapping
    public ResponseEntity<CreateGameResponse> createGame(@Valid @RequestBody(required = false) CreateGameRequest request) {
        Integer boardSize = request == null ? null : request.boardSize();
        CreateGameResponse response = gameService.createGame(boardSize);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère l'état actuel d'une partie.
     * 
     * @param gameId l'identifiant unique de la partie
     * @return l'état actuel du plateau et les métadonnées
     */
    @GetMapping("/{id}")
    public GameStateResponse getGame(@PathVariable("id") String gameId) {
        return gameService.getGameById(gameId);
    }

    /**
     * Joue un coup à une position donnée.
     * 
     * Le coup doit être légal selon les règles du Go.
     * Métrique : durée d'exécution tracée en micrométriques.
     * 
     * @param gameId l'identifiant de la partie
     * @param request la position (row, col) du coup
     * @return l'état du plateau après le coup
     */
    @PostMapping("/{id}/moves")
    public ResponseEntity<GameStateResponse> makeMove(
            @PathVariable("id") String gameId,
            @Valid @RequestBody MakeMoveRequest request) {
        GameStateResponse response = gameService.playMove(gameId, request.row(), request.col());
        return ResponseEntity.ok(response);
    }

    /**
     * Passe le tour au joueur suivant.
     * 
     * Utilisé lorsqu'un joueur ne souhaite pas jouer de coup.
     * 
     * @param gameId l'identifiant de la partie
     * @return l'état du plateau après la passe
     */
    @PostMapping("/{id}/pass")
    public ResponseEntity<GameStateResponse> passMove(
            @PathVariable("id") String gameId) {
        GameStateResponse response = gameService.passMove(gameId);
        return ResponseEntity.ok(response);
    }
}
