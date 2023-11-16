package com.cpb.backend.server;

import com.cpb.backend.entity.Message;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
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

    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
    private static String sharedText = "";

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session) {
        sessions.add(session);
        synchronized (sharedText) {
            session.getBasicRemote().sendText(sharedText);
        }
        log.info("New session opened: " + session.getId());
        log.info("Number of sessions: " + sessions.size());
        Message message = new Message(1, String.valueOf(sessions.size()));
        sendAllMessage(message.toString());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        log.info("Session closed: " + session.getId());
        log.info("Number of sessions: " + sessions.size());
        Message message = new Message(1, String.valueOf(sessions.size()));
        sendAllMessage(message.toString());
    }

    @OnMessage
    public void onMessage(String receivedMessage) {
        log.info("Message received: " + receivedMessage);
        synchronized (sharedText) {
            sharedText = receivedMessage;
            Message message = new Message(2, sharedText);
            sendAllMessage(message.toString());
        }
    }

    @SneakyThrows
    public static void sendAllMessage(String message) {
        log.info("Send Message " + message);
        for (Session webSocket : sessions) {
            webSocket.getBasicRemote().sendText(message);
        }
    }
}
