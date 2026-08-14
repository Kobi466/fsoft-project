package com.fivetuat.fsoftprojectserver.listeners;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketEventListener {

    SimpMessagingTemplate messagingTemplate;

    static ConcurrentHashMap<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        Principal user = headerAccessor.getUser();

        if (sessionId != null) {
            String userId = (user != null && user.getName() != null) ? user.getName() : sessionId;
            SESSION_USER_MAP.put(sessionId, userId);

            int count = getOnlineUserCount();
            log.info("Client connected: sessionId={}, userId={}, onlineCount={}", sessionId, userId, count);
            broadcastOnlineCount(count);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            String removedUser = SESSION_USER_MAP.remove(sessionId);
            int count = getOnlineUserCount();
            log.info("Client disconnected: sessionId={}, userId={}, onlineCount={}", sessionId, removedUser, count);
            broadcastOnlineCount(count);
        }
    }

    public static int getOnlineUserCount() {
        return (int) SESSION_USER_MAP.values().stream().distinct().count();
    }

    private void broadcastOnlineCount(int count) {
        messagingTemplate.convertAndSend((String) "/topic/online-count", (Object) Map.of("onlineUsers", count));
    }
}