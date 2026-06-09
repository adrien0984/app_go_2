package com.appgo.games.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class GameSessionManager {

    private final Map<String, Set<WebSocketSession>> sessionsByGameId = new ConcurrentHashMap<>();

    public void addSession(String gameId, WebSocketSession session) {
        sessionsByGameId.computeIfAbsent(gameId, k -> new CopyOnWriteArraySet<>())
                .add(session);
    }

    public void removeSession(String gameId, WebSocketSession session) {
        Set<WebSocketSession> sessions = sessionsByGameId.get(gameId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByGameId.remove(gameId);
            }
        }
    }

    public Set<WebSocketSession> getGameSessions(String gameId) {
        return sessionsByGameId.getOrDefault(gameId, new CopyOnWriteArraySet<>());
    }

    public boolean hasActiveSessions(String gameId) {
        return sessionsByGameId.containsKey(gameId) && !sessionsByGameId.get(gameId).isEmpty();
    }
}
