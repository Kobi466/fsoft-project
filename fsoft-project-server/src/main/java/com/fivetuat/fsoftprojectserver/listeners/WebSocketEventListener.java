package com.fivetuat.fsoftprojectserver.listeners;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fivetuat.fsoftprojectserver.services.ChatMessageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSocketEventListener {

    ChatMessageService chatMessageService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        chatMessageService.incrementOnlineUsers();
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        chatMessageService.decrementOnlineUsers();
    }
}
