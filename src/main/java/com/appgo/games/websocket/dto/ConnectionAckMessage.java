package com.appgo.games.websocket.dto;

public class ConnectionAckMessage extends WebSocketMessage {
    private String gameId;
    private String message;

    public ConnectionAckMessage() {
    }

    public ConnectionAckMessage(String gameId) {
        this.gameId = gameId;
        this.message = "Connected to game " + gameId;
    }

    @Override
    public String getType() {
        return "connection.ack";
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
