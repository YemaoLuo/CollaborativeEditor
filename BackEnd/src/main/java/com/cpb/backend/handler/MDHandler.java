package com.cpb.backend.handler;

import com.cpb.backend.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/MDHandler/{id}")
@Component
@Slf4j
public class MDHandler {

    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final Map<String, String> sharedTextMap = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session, @PathParam("id") String id) {
        sessions.computeIfAbsent(id, k -> new HashSet<>()).add(session);
        Message message = new Message(2, sharedTextMap.getOrDefault(id, ""));
        session.getBasicRemote().sendText(mapper.writeValueAsString(message));
        log.info("New session opened: " + session.getId());
        log.info("Number of sessions for ID {}: {}", id, sessions.get(id).size());
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(getTotalNumberOfSessions()));
        sendAllMessage(null, mapper.writeValueAsString(sessionCountMessage), id);
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session, @PathParam("id") String id) {
        List<Session> sessionList = sessions.get(id);
        if (sessionList != null) {
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessions.remove(id);
            }
        }
        session.close();
        log.info("Session closed: " + session.getId());
        log.info("Number of sessions for ID {}: {}", id, sessions.get(id) == null ? 0 : sessions.get(id).size());
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(getTotalNumberOfSessions()));
        sendAllMessage(null, mapper.writeValueAsString(sessionCountMessage), id);
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(Session session, String receivedMessage, @PathParam("id") String id) {
        log.info("Message received: " + receivedMessage);
        sharedTextMap.put(id, receivedMessage);
        Message message = new Message(2, sharedTextMap.getOrDefault(id, ""));
        sendAllMessage(session, mapper.writeValueAsString(message), id);
    }

    @OnError
    @SneakyThrows
    public void onError(Session session, Throwable throwable, @PathParam("id") String id) {
        log.error("Error occurred: " + throwable.getMessage());
        Set<Session> sessionList = sessions.get(id);
        if (sessionList != null) {
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessions.remove(id);
            }
        }
        session.close();
    }

    @SneakyThrows
    public static void sendAllMessage(Session session, String message, String id) {
        log.info("Sending message with length {} to {} sessions", message.length(), sessions.get(id) == null ? 0 : sessions.get(id).size());
        Set<Session> sessionList = sessions.get(id);
        if (sessionList != null) {
            for (Session webSocket : sessionList) {
                if (session != null && session.getId().equals(webSocket.getId())) {
                    continue;
                }
                webSocket.getBasicRemote().sendText(message);
            }
        }
    }

    private static int getTotalNumberOfSessions() {
        int total = 0;
        for (Set<Session> sessionList : sessions.values()) {
            total += sessionList.size();
        }
        return total;
    }
}