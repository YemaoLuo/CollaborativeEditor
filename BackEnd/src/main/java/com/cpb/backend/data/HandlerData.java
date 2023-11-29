package com.cpb.backend.data;

import jakarta.websocket.Session;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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

    public abstract void removeSession(String id, Session session);

    @SneakyThrows
    public void localizeData(String fileName, Object o) {
        log.info("Localizing data to file: {}", fileName);
        FileOutputStream fileOut = new FileOutputStream(fileName);
        ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
        objectOut.writeObject(o);
        objectOut.close();
        fileOut.close();
    }

    public Object getLocalizeData(String fileName) {
        log.info("Getting data from file: {}", fileName);
        Object object = null;
        ObjectInputStream objectIn = null;
        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(fileName);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();
            objectIn.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            log.info("File not found: {}", fileName);
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return object;
    }

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
            }
        });
    }
}
