package com.appgo.games.websocket.dto;

public class GameEventMessage extends WebSocketMessage {
    private String eventType;
    private Object data;

    public GameEventMessage() {
    }

    public GameEventMessage(String eventType, Object data) {
        this.eventType = eventType;
        this.data = data;
    }

    @Override
    public String getType() {
        return eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
