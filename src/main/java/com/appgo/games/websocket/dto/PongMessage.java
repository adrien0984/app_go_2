package com.appgo.games.websocket.dto;

public class PongMessage extends WebSocketMessage {
    private long timestamp;

    public PongMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getType() {
        return "pong";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
