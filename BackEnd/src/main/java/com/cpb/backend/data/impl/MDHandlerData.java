package com.cpb.backend.data.impl;

import com.cpb.backend.data.HandlerData;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MDHandlerData extends HandlerData {


    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();

    @Override
    public void localizeData() {

    }

    public void addSession(String id, Session session) {
        super.addSession(sessions, id, session);
    }

    public void removeSession(String id, Session session) {
        super.removeSession(sessions, id, session);
    }

    public int getTotalNumberOfSessions() {
        return super.getTotalNumberOfSessions(sessions);
    }

    public int getNumberOfSessions(String id) {
        return super.getNumberOfSessions(sessions, id);
    }

    public void sendAllMessage(String message, String id, Session session) {
        super.sendAllMessage(sessions, message, id, session);
    }
}
