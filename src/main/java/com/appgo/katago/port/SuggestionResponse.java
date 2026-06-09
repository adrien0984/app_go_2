package com.appgo.katago.port;

/**
 * Réponse avec la suggestion de coup.
 */
public class SuggestionResponse {

    private final int suggestedRow;
    private final int suggestedCol;
    private final double confidence;
    private final long timeMs;

    public SuggestionResponse(int suggestedRow, int suggestedCol, double confidence, long timeMs) {
        this.suggestedRow = suggestedRow;
        this.suggestedCol = suggestedCol;
        this.confidence = confidence;
        this.timeMs = timeMs;
    }

    public int getSuggestedRow() {
        return suggestedRow;
    }

    public int getSuggestedCol() {
        return suggestedCol;
    }

    public double getConfidence() {
        return confidence;
    }

    public long getTimeMs() {
        return timeMs;
    }
}
