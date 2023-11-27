package com.cpb.backend.handler;

import com.cpb.backend.entity.Message;
import com.cpb.backend.entity.Operation;
import com.cpb.backend.util.OperationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/MDHandler/{id}")
@Component
@Slf4j
public class MDHandler {

    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final Map<String, SortedSet<Operation>> sharedOperationMap = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session, @PathParam("id") String id) {
        sessions.computeIfAbsent(id, k -> new HashSet<>()).add(session);
        SortedSet operations = sharedOperationMap.get(id);
        if (operations != null) {
            Message message = new Message(2, mapper.writeValueAsString(operations));
            session.getBasicRemote().sendText(mapper.writeValueAsString(message));
        }
        log.info("Number of sessions for ID {}: {}", id, sessions.containsKey(id) ? sessions.get(id).size() : 0);
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(getTotalNumberOfSessions()));
        sendAllMessage(mapper.writeValueAsString(sessionCountMessage), id, null);
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(Session session, Operation receivedMessage, @PathParam("id") String id) {
        log.info("Message received from: " + session.getId());
        sharedOperationMap.computeIfAbsent(id, k -> new TreeSet<>()).add(receivedMessage);
        if (OperationUtil.addIfOperationValid(receivedMessage, sharedOperationMap.get(id))) {
            Message message = new Message(3, mapper.writeValueAsString(receivedMessage));
            sendAllMessage(mapper.writeValueAsString(message), id, session);
        } else {
            log.info("Operation is not valid for ID {} from: {}", id, session.getId());
            SortedSet operations = sharedOperationMap.get(id);
            if (operations != null) {
                Message message = new Message(2, mapper.writeValueAsString(operations));
                session.getBasicRemote().sendText(mapper.writeValueAsString(message));
            }
        }
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session, @PathParam("id") String id) {
        Set<Session> sessionList = sessions.get(id);
        if (sessionList != null) {
            sessionList.remove(session);
            if (sessionList.isEmpty()) {
                sessions.remove(id);
            }
        }
        session.close();
        log.info("Number of sessions for ID {}: {}", id, sessions.containsKey(id) ? sessions.get(id).size() : 0);
        log.info("Total number of sessions: {}", getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(getTotalNumberOfSessions()));
        sendAllMessage(mapper.writeValueAsString(sessionCountMessage), id, session);
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
    public static void sendAllMessage(String message, String id, Session session) {
        log.info("Sending message to {}", id);
        Set<Session> sessionSet = sessions.get(id);
        if (sessionSet != null) {
            for (Session webSocket : sessionSet) {
                if (webSocket.equals(session)) {
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