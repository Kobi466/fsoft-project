// listeners/AudioReflexDisconnectListener.java
package com.fivetuat.fsoftprojectserver.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fivetuat.fsoftprojectserver.controllers.AudioReflexSessionTracker;
import com.fivetuat.fsoftprojectserver.services.AudioReflexRoomService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AudioReflexDisconnectListener {

    AudioReflexSessionTracker sessionTracker;
    AudioReflexRoomService roomService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        var roomSession = sessionTracker.remove(sessionId);
        if (roomSession == null) return;
        try {
            roomService.markDisconnected(roomSession.roomCode(), roomSession.userId());
        } catch (Exception e) {
            log.warn("Cleanup room on disconnect failed: {}", e.getMessage());
        }
    }
}