package com.appgo.games.websocket.dto;

public class PingMessage extends WebSocketMessage {
    private long timestamp;

    public PingMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getType() {
        return "ping";
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
