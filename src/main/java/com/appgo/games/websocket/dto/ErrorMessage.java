package com.appgo.games.websocket.dto;

public class ErrorMessage extends WebSocketMessage {
    private String code;
    private String message;

    public ErrorMessage() {
    }

    public ErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getType() {
        return "error";
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
