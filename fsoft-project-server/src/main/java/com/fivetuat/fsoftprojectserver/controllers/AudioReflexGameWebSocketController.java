// controllers/AudioReflexGameWebSocketController.java
package com.fivetuat.fsoftprojectserver.controllers;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.fivetuat.fsoftprojectserver.dtos.request.SubmitAnswerRequest;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AudioReflexRoomService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AudioReflexGameWebSocketController {

    private final AudioReflexRoomService roomService;
    private final AudioReflexSessionTracker sessionTracker;

    @MessageMapping("/room/{code}/join-socket")
    public void trackJoin(@DestinationVariable String code, Principal principal,
                          SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) return;
        sessionTracker.track(headerAccessor.getSessionId(), code, principal.getName());
        try {
            roomService.markReconnected(code, principal.getName());
        } catch (AppException ignored) {
            // socket connect trước khi gọi REST join lần đầu -> chưa có trong phòng, bỏ qua
        }
    }

    @MessageMapping("/room/{code}/answer")
    public void submitAnswer(@DestinationVariable String code,
                             @Payload SubmitAnswerRequest request,
                             Principal principal) {
        if (principal == null) return;
        roomService.submitAnswer(code, principal.getName(), request.getCardId());
    }
}