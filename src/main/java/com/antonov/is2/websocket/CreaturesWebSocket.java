package com.antonov.is2.websocket;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
@ServerEndpoint("/creatures-updates")
public class CreaturesWebSocket {

    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("WebSocket connected: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("WebSocket disconnected: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // Обработка входящих сообщений
        System.out.println("Received message: " + message);
    }

    // Метод для рассылки обновлений всем подключенным клиентам
    public static void broadcast(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // Методы для разных типов событий
    public static void notifyCreatureCreated(Long creatureId) {
        broadcast("CREATURE_CREATED:" + creatureId);
    }

    public static void notifyCreatureUpdated(Long creatureId) {
        broadcast("CREATURE_UPDATED:" + creatureId);
    }

    public static void notifyCreatureDeleted(Long creatureId) {
        broadcast("CREATURE_DELETED:" + creatureId);
    }
    
    public static void notifyCityCreated(Long cityId) {
        broadcast("CITY_CREATED:" + cityId);
    }

    public static void notifyCityUpdated(Long cityId) {
        broadcast("CITY_UPDATED:" + cityId);
    }

    public static void notifyCityDeleted(Long cityId) {
        broadcast("CITY_DELETED:" + cityId);
    }
    
    public static void notifyRingCreated(Long ringId) {
        broadcast("RING_CREATED:" + ringId);
    }

    public static void notifyRingUpdated(Long ringId) {
        broadcast("RING_UPDATED:" + ringId);
    }

    public static void notifyRingDeleted(Long ringId) {
        broadcast("RING_DELETED:" + ringId);
    }
}