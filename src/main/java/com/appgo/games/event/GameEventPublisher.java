package com.appgo.games.event;

import com.appgo.games.websocket.GameSessionManager;
import com.appgo.games.websocket.dto.GameEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class GameEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(GameEventPublisher.class);

    private final GameSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public GameEventPublisher(GameSessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void broadcastGameEvent(String gameId, String eventType, Object eventData) {
        var sessions = sessionManager.getGameSessions(gameId);
        if (sessions.isEmpty()) {
            logger.debug("No active sessions for game {}", gameId);
            return;
        }

        GameEventMessage event = new GameEventMessage(eventType, eventData);
        String messageJson;
        try {
            messageJson = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            logger.error("Failed to serialize event for game {}", gameId, e);
            return;
        }

        TextMessage message = new TextMessage(messageJson);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException e) {
                    logger.warn("Failed to send message to session {} for game {}", session.getId(), gameId, e);
                }
            }
        }
    }
}
