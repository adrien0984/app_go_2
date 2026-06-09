package com.appgo.games.websocket.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GameEventMessage.class, name = "move.played"),
        @JsonSubTypes.Type(value = PingMessage.class, name = "ping"),
        @JsonSubTypes.Type(value = PongMessage.class, name = "pong"),
        @JsonSubTypes.Type(value = ConnectionAckMessage.class, name = "connection.ack"),
        @JsonSubTypes.Type(value = ErrorMessage.class, name = "error")
})
public abstract class WebSocketMessage {
    public abstract String getType();
}
