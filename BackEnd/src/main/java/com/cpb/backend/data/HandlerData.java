package com.cpb.backend.data;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class HandlerData {

    public Set<Session> getSessions(Map<String, Set<Session>> sessions, String id) {
        return sessions.get(id);
    }

    public void addSession(Map<String, Set<Session>> sessions, String id, Session session) {
        sessions.computeIfAbsent(id, k -> new HashSet<>()).add(session);
    }

    public void removeSession(Map<String, Set<Session>> sessions, String id, Session session) {
        sessions.get(id).remove(session);
        if (sessions.get(id).isEmpty()) {
            sessions.remove(id);
        }
        // Check if 0 -> localize data
        if (sessions.get(id) == null || sessions.get(id).isEmpty()) {
            localizeData();
        }
    }

    public abstract void localizeData();

    public int getTotalNumberOfSessions(Map<String, Set<Session>> sessions) {
        return sessions.values().stream().mapToInt(Set::size).sum();
    }

    public int getNumberOfSessions(Map<String, Set<Session>> sessions, String id) {
        return sessions.containsKey(id) ? sessions.get(id).size() : 0;
    }

    public void sendAllMessage(Map<String, Set<Session>> sessions, Object message, String id, Session session) {
        if (sessions.get(id) == null) {
            return;
        }
        sessions.get(id).stream().filter(s -> !s.equals(session)).forEach(s -> {
            try {
                if (message.getClass().equals(String.class))
                    s.getBasicRemote().sendText((String) message);
                else {
                    s.getBasicRemote().sendObject(message);
                }
            } catch (Exception e) {
                log.error("Error sending message to session: {}", s.getId());
                log.error(e.getMessage(), e);
            }
        });
    }
}
