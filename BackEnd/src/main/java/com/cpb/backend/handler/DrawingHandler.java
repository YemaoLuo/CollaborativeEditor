package com.cpb.backend.handler;

import com.cpb.backend.data.impl.DrawingHandlerData;
import com.cpb.backend.entity.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@ServerEndpoint("/DrawingHandler/{id}")
@Component
@Slf4j
public class DrawingHandler {

    private static final DrawingHandlerData handlerData = new DrawingHandlerData();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session, @PathParam("id") String id) {
        handlerData.addSession(id, session);
        if (handlerData.containsData(id)) {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(handlerData.getData(id)));
        }
        log.info("Number of sessions for ID {}: {}", id, handlerData.getNumberOfSessions(id));
        log.info("Total number of sessions: {}", handlerData.getTotalNumberOfSessions());
        handlerData.sendAllMessage(mapper.writeValueAsString(new Message(1, String.valueOf(handlerData.getNumberOfSessions(id)))), id, null);
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session, @PathParam("id") String id) {
        handlerData.removeSession(id, session);
        session.close();
        log.info("Number of sessions for ID {}: {}", id, handlerData.getNumberOfSessions(id));
        log.info("Total number of sessions: {}", handlerData.getTotalNumberOfSessions());
        handlerData.sendAllMessage(mapper.writeValueAsString(new Message(1, String.valueOf(handlerData.getNumberOfSessions(id)))), id, session);
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(Session session, byte[] message, @PathParam("id") String id) {
        log.info("Message received from: " + session.getId());
        handlerData.setData(id, message);
        handlerData.sendAllMessage(message, id, session);
    }

    @OnError
    @SneakyThrows
    public void onError(Session session, Throwable throwable, @PathParam("id") String id) {
        log.error("Error occurred: " + throwable.getMessage());
        handlerData.removeSession(id, session);
    }
}
