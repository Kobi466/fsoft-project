package com.fivetuat.fsoftprojectserver.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SpaceStrikerSessionTracker {
    public record RoomSession(String roomCode, String userId) {}

    private final Map<String, RoomSession> sessions = new ConcurrentHashMap<>();

    public void track(String sessionId, String roomCode, String userId) {
        sessions.put(sessionId, new RoomSession(roomCode, userId));
    }

    public RoomSession remove(String sessionId) {
        return sessions.remove(sessionId);
    }
}