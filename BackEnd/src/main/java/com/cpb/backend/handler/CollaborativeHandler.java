package com.cpb.backend.handler;

import com.cpb.backend.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/CollaborativeHandler")
@Component
@Slf4j
public class CollaborativeHandler {

    private static final Set<Session> sessions = new HashSet<>();
    private static String sharedText = "Hello Editor!";
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session) {
        synchronized (sessions) {
            sessions.add(session);
        }
        synchronized (sharedText) {
            Message message = new Message(2, sharedText);
            session.getBasicRemote().sendText(mapper.writeValueAsString(message));
        }
        log.info("New session opened: " + session.getId());
        log.info("Number of sessions: " + sessions.size());
        Message message = new Message(1, String.valueOf(sessions.size()));
        sendAllMessage(mapper.writeValueAsString(message));
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session) {
        session.close();
        synchronized (sessions) {
            sessions.remove(session);
        }
        log.info("Session closed: " + session.getId());
        log.info("Number of sessions: " + sessions.size());
        Message message = new Message(1, String.valueOf(sessions.size()));
        sendAllMessage(mapper.writeValueAsString(message));
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(String receivedMessage) {
        log.info("Message received: " + receivedMessage);
        synchronized (sharedText) {
            sharedText = receivedMessage;
            Message message = new Message(2, sharedText);
            sendAllMessage(mapper.writeValueAsString(message));
        }
    }

    @OnError
    @SneakyThrows
    public void onError(Session session, Throwable throwable) {
        log.error("Error occurred: " + throwable.getMessage());
        synchronized (sessions) {
            sessions.remove(session);
        }
        session.close();
    }

    @SneakyThrows
    public static void sendAllMessage(String message) {
        log.info("Send Message: " + message);
        synchronized (sessions) {
            for (Session webSocket : sessions) {
                webSocket.getBasicRemote().sendText(message);
            }
        }
    }
}

