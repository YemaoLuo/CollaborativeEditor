package com.cpb.backend.handler;

import com.cpb.backend.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/DrawingHandler/{id}")
@Component
@Slf4j
public class DrawingHandler {

    private static final ConcurrentHashMap<String, byte[]> picDataMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session, @PathParam("id") String id) {
        sessions.computeIfAbsent(id, k -> new HashSet<>()).add(session);
        if (picDataMap.containsKey(id)) {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(picDataMap.get(id)));
        }
        log.info("Number of sessions for ID {}: {}", id, sessions.containsKey(id) ? sessions.get(id).size() : 0);
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        sendAllMessage(mapper.writeValueAsString(new Message(1, String.valueOf(sessions.get(id).size()))), id, null);
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session, @PathParam("id") String id) {
        sessions.get(id).remove(session);
        if (sessions.get(id).isEmpty()) {
            sessions.remove(id);
        }
        log.info("Number of sessions for ID {}: {}", id, sessions.containsKey(id) ? sessions.get(id).size() : 0);
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        if (sessions.get(id) != null) {
            Message message = new Message(1, String.valueOf(sessions.get(id).size()));
            sendAllMessage(mapper.writeValueAsString(message), id, session);
        }
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(Session session, byte[] message, @PathParam("id") String id) {
        log.info("Message received from: " + session.getId());
        picDataMap.put(id, message);
        sendAllMessage(message, id, session);
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
    }

    @SneakyThrows
    public static void sendAllMessage(Object message, String id, Session session) {
        log.info("Sending message to {}", id);
        Set<Session> sessionSet = sessions.get(id);
        if (sessionSet != null) {
            for (Session webSocket : sessionSet) {
                if (webSocket.equals(session)) {
                    continue;
                }
                if (message.getClass().equals(String.class)) {
                    webSocket.getBasicRemote().sendText(message.toString());
                } else {
                    webSocket.getBasicRemote().sendObject(message);
                }
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
