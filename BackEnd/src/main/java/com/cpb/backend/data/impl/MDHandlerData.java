package com.cpb.backend.data.impl;

import com.cpb.backend.data.HandlerData;
import com.cpb.backend.entity.Operation;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MDHandlerData extends HandlerData {


    private static final Map<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final Map<String, SortedSet<Operation>> dataMap = new ConcurrentHashMap<>();

    @Override
    public void localizeData() {

    }

    public boolean containsData(String id) {
        return dataMap.containsKey(id);
    }

    public SortedSet<Operation> getData(String id) {
        return dataMap.get(id);
    }

    public void setData(String id, Operation data) {
        dataMap.computeIfAbsent(id, k -> new TreeSet<>()).add(data);
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
