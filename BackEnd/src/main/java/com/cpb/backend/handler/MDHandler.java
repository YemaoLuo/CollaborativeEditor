package com.cpb.backend.handler;

import com.cpb.backend.data.impl.MDHandlerData;
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

import java.util.SortedSet;

@ServerEndpoint("/MDHandler/{id}")
@Component
@Slf4j
public class MDHandler {

    private static final MDHandlerData handlerData = new MDHandlerData();
    private static final ObjectMapper mapper = new ObjectMapper();

    @OnOpen
    @SneakyThrows
    public void onOpen(Session session, @PathParam("id") String id) {
        handlerData.addSession(id, session);
        if (handlerData.containsData(id)) {
            session.getBasicRemote().sendText(mapper.writeValueAsString(new Message(2, handlerData.getData(id))));
        }
        log.info("Number of sessions for ID {}: {}", id, handlerData.getNumberOfSessions(id));
        log.info("Total number of sessions: {}", handlerData.getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(handlerData.getNumberOfSessions(id)));
        handlerData.sendAllMessage(mapper.writeValueAsString(sessionCountMessage), id, null);
    }

    @OnClose
    @SneakyThrows
    public void onClose(Session session, @PathParam("id") String id) {
        handlerData.removeSession(id, session);
        session.close();
        log.info("Number of sessions for ID {}: {}", id, handlerData.getNumberOfSessions(id));
        log.info("Total number of sessions: {}", handlerData.getTotalNumberOfSessions());
        Message sessionCountMessage = new Message(1, String.valueOf(handlerData.getNumberOfSessions(id)));
        handlerData.sendAllMessage(mapper.writeValueAsString(sessionCountMessage), id, session);
    }

    @OnMessage
    @SneakyThrows
    public void onMessage(Session session, String message, @PathParam("id") String id) {
        Operation receivedMessage = mapper.readValue(message, Operation.class);
        log.info("Message received from: " + session.getId());
        handlerData.setData(id, receivedMessage);
        SortedSet<Operation> operations = handlerData.getData(id);
        if (receivedMessage.getType().equals("init")) {
            session.getBasicRemote().sendText(mapper.writeValueAsString(new Message(2, mapper.writeValueAsString(operations))));
        } else if (OperationUtil.addIfOperationValid(receivedMessage, operations)) {
            handlerData.sendAllMessage(mapper.writeValueAsString(new Message(3, receivedMessage)), id, session);
        } else {
            log.info("Operation is not valid for ID {} from: {}", id, session.getId());
            session.getBasicRemote().sendText(mapper.writeValueAsString(new Message(2, operations)));
        }
    }

    @OnError
    @SneakyThrows
    public void onError(Session session, Throwable throwable, @PathParam("id") String id) {
        log.error("Error occurred: " + throwable.getMessage());
        handlerData.removeSession(id, session);
    }
}