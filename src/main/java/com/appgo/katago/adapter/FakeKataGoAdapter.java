package com.appgo.katago.adapter;

import com.appgo.katago.port.KataGoException;
import com.appgo.katago.port.KataGoSuggestionPort;
import com.appgo.katago.port.SuggestionRequest;
import com.appgo.katago.port.SuggestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adaptateur fake pour KataGo avec suggestions déterministes.
 * Utilise une stratégie prévisible et stable pour les suggestions.
 */
public class FakeKataGoAdapter implements KataGoSuggestionPort {

    private static final Logger logger = LoggerFactory.getLogger(FakeKataGoAdapter.class);
    private static final double DEFAULT_CONFIDENCE = 0.75;

    private int callCount = 0;

    @Override
    public SuggestionResponse getSuggestion(SuggestionRequest request) throws KataGoException {
        long startTime = System.currentTimeMillis();

        try {
            int[] suggestion = computeSuggestion(request);
            int row = suggestion[0];
            int col = suggestion[1];

            long timeMs = System.currentTimeMillis() - startTime;
            logger.debug("Suggestion fake générée: [{}, {}], confiance: {}, temps: {}ms",
                    row, col, DEFAULT_CONFIDENCE, timeMs);

            return new SuggestionResponse(row, col, DEFAULT_CONFIDENCE, timeMs);
        } catch (Exception e) {
            logger.error("Erreur lors de la génération de suggestion fake", e);
            throw new KataGoException("Erreur lors de la génération de suggestion", e);
        }
    }

    /**
     * Compute a deterministic suggestion based on the board state and call count.
     * Strategy: corners first, then edges, then center.
     */
    private int[] computeSuggestion(SuggestionRequest request) {
        callCount++;
        int boardSize = request.getBoardSize();

        // Coin strategy : 4 coins classiques
        int[][] corners = {
                {boardSize - 1, boardSize - 1},
                {boardSize - 1, 0},
                {0, 0},
                {0, boardSize - 1}
        };

        if (callCount <= 4) {
            return corners[callCount - 1];
        }

        // Après les coins, utilisations des positions d'étoiles classiques (pour 19x19)
        if (boardSize == 19) {
            int[][] starPoints = {
                    {3, 3}, {3, 9}, {3, 15},
                    {9, 3}, {9, 9}, {9, 15},
                    {15, 3}, {15, 9}, {15, 15}
            };
            int index = (callCount - 5) % starPoints.length;
            return starPoints[index];
        }

        // Pour autres tailles, centre du plateau
        int center = boardSize / 2;
        return new int[]{center, center};
    }

    /**
     * Reset du compteur pour les tests.
     */
    protected void resetCallCount() {
        callCount = 0;
    }

    protected int getCallCount() {
        return callCount;
    }
}
