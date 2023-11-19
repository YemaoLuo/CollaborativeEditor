package com.cpb.backend.handler;

import com.cpb.backend.util.GzipUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/DrawingHandler/{id}")
@Component
@Slf4j
public class DrawingHandler {

    private static final ConcurrentHashMap<String, String> history = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> dataMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Set<Session>> sessions = new ConcurrentHashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    public void onOpen(Session session, @PathParam("id") String id) throws IOException {
        if (sessions.containsKey(id)) {
            sessions.get(id).add(session);
        } else {
            Set<Session> set = new HashSet<>();
            set.add(session);
            sessions.put(id, set);
        }
        log.info("onOpen " + id + "：{}", sessions.get(id).size());
        if (history.containsKey(id)) {
            session.getBasicRemote().sendText(history.get(id));
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("id") String id) {
        sessions.get(id).remove(session);
        log.info("onClose " + id + "：{}", sessions.get(id).size());
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(String message, @PathParam("id") String id) {
        Map map = mapper.readValue(message, Map.class);
        String key = (String) map.get("id");
        String value = (String) map.get("data");
        if (value.equals("END")) {
            ConcurrentHashMap<String, String> map1 = dataMap.get(id);
            history.put(id, map1.get(key));
            sendAllMessage(map1.get(key), id);
            map1.remove(key);
        } else {
            if (dataMap.containsKey(id)) {
                if (dataMap.get(id).containsKey(key)) {
                    ConcurrentHashMap<String, String> map1 = dataMap.get(id);
                    map1.put(key, map1.get(key) + value);
                } else {
                    ConcurrentHashMap<String, String> map1 = new ConcurrentHashMap<>();
                    map1.put(key, value);
                    dataMap.put(id, map1);
                }
            } else {
                ConcurrentHashMap<String, String> map1 = new ConcurrentHashMap<>();
                map1.put(key, value);
                dataMap.put(id, map1);
            }
        }
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

    public static void sendAllMessage(String message, String id) {
        log.info("Send Message " + id);
        Set<Session> sessionSet = sessions.get(id);
        for (Session webSocket : sessionSet) {
            try {
                byte[] compressed = GzipUtils.compressString(message);
                webSocket.getBasicRemote().sendObject(compressed);
            } catch (Exception e) {
                sessionSet.remove(webSocket);
            }
        }
    }
}
