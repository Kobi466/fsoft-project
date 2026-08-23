package com.fivetuat.fsoftprojectserver.controllers;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.fivetuat.fsoftprojectserver.dtos.request.ChatMessageRequest;
import com.fivetuat.fsoftprojectserver.services.ChatMessageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatWebSocketController {

    ChatMessageService chatMessageService;

    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal != null && principal.getName() != null) {
            chatMessageService.saveAndBroadcastMessage(request, principal.getName());
        }
    }
}
