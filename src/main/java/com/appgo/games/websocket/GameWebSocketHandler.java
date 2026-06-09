package com.appgo.games.websocket;

import com.appgo.games.service.GameService;
import com.appgo.games.websocket.dto.ConnectionAckMessage;
import com.appgo.games.websocket.dto.ErrorMessage;
import com.appgo.games.websocket.dto.PingMessage;
import com.appgo.games.websocket.dto.PongMessage;
import com.appgo.games.websocket.dto.WebSocketMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;
    private static final long INACTIVITY_TIMEOUT_SECONDS = 60;

    private final GameSessionManager sessionManager;
    private final GameService gameService;
    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService executorService;
    private final Map<String, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessionById = new ConcurrentHashMap<>();

    public GameWebSocketHandler(GameSessionManager sessionManager, GameService gameService, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.gameService = gameService;
        this.objectMapper = objectMapper;
        this.executorService = new ScheduledThreadPoolExecutor(2);
        startHeartbeatScheduler();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String gameId = extractGameId(session);
        if (gameId == null) {
            sendError(session, "invalid.game.id", "Invalid game ID");
            session.close();
            return;
        }

        try {
            gameService.getGameById(gameId);
        } catch (Exception e) {
            sendError(session, "game.not.found", "Game not found");
            session.close();
            return;
        }

        sessionManager.addSession(gameId, session);
        lastActivityTime.put(session.getId(), System.currentTimeMillis());
        sessionById.put(session.getId(), session);

        ConnectionAckMessage ack = new ConnectionAckMessage(gameId);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));

        logger.info("WebSocket connection established for game: {}, session: {}", gameId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String gameId = extractGameId(session);
        if (gameId == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        lastActivityTime.put(session.getId(), System.currentTimeMillis());

        try {
            WebSocketMessage wsMessage = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);

            if (wsMessage instanceof PingMessage) {
                handlePing(session);
            } else if (wsMessage instanceof PongMessage) {
                logger.debug("Received pong from session {} for game {}", session.getId(), gameId);
            }
        } catch (Exception e) {
            logger.warn("Failed to deserialize message from session {} for game {}", session.getId(), gameId, e);
            sendError(session, "invalid.message", "Invalid message format");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String gameId = extractGameId(session);
        if (gameId != null) {
            sessionManager.removeSession(gameId, session);
            lastActivityTime.remove(session.getId());
            sessionById.remove(session.getId());
            logger.info("WebSocket connection closed for game: {}, session: {}, status: {}", gameId, session.getId(), status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String gameId = extractGameId(session);
        logger.error("WebSocket transport error for game: {}, session: {}", gameId, session.getId(), exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    private void handlePing(WebSocketSession session) throws IOException {
        PongMessage pong = new PongMessage();
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
        logger.debug("Sent pong to session {} ", session.getId());
    }

    private void sendError(WebSocketSession session, String code, String message) {
        try {
            ErrorMessage error = new ErrorMessage(code, message);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (IOException e) {
            logger.error("Failed to send error message to session {}", session.getId(), e);
        }
    }

    private String extractGameId(WebSocketSession session) {
        try {
            Object fromAttributes = session.getAttributes().get("id");
            if (fromAttributes instanceof String gameId && !gameId.isBlank()) {
                return gameId;
            }

            if (session.getUri() != null) {
                String path = session.getUri().getPath();
                String prefix = "/ws/games/";
                if (path != null && path.startsWith(prefix) && path.length() > prefix.length()) {
                    return path.substring(prefix.length());
                }
            }

            return null;
        } catch (Exception e) {
            logger.warn("Failed to extract game ID from session {}", session.getId());
            return null;
        }
    }

    private void startHeartbeatScheduler() {
        executorService.scheduleAtFixedRate(this::sendHeartbeat, HEARTBEAT_INTERVAL_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void sendHeartbeat() {
        long currentTime = System.currentTimeMillis();
        lastActivityTime.forEach((sessionId, lastActivity) -> {
            long inactivityDuration = TimeUnit.MILLISECONDS.toSeconds(currentTime - lastActivity);
            if (inactivityDuration >= INACTIVITY_TIMEOUT_SECONDS) {
                logger.debug("Session {} inactive for {} seconds, sending ping", sessionId, inactivityDuration);
                // Find the session and send ping
                try {
                    sendPingToSession(sessionId);
                } catch (Exception e) {
                    logger.warn("Failed to send ping to session {}", sessionId, e);
                }
            }
        });
    }

    private void sendPingToSession(String sessionId) throws IOException {
        WebSocketSession session = sessionById.get(sessionId);
        if (session != null && session.isOpen()) {
            PingMessage ping = new PingMessage();
            String messageJson = objectMapper.writeValueAsString(ping);
            session.sendMessage(new TextMessage(messageJson));
            logger.debug("Sent ping to session {}", sessionId);
        }
    }
}
