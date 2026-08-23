package com.fivetuat.fsoftprojectserver.controllers;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com.fivetuat.fsoftprojectserver.dtos.request.SelectDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.SubmitAnswerRequest;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.SpaceStrikerRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SpaceStrikerGameWebSocketController {

    private final SpaceStrikerRoomService roomService;
    private final SpaceStrikerSessionTracker sessionTracker;

    private String resolveUserId(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        if (principal != null && principal.getName() != null) {
            return principal.getName();
        }
        if (headerAccessor != null && headerAccessor.getSessionAttributes() != null) {
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            if (userId != null) return userId;
        }
        return null;
    }

    @MessageMapping("/space-room/{code}/join-socket")
    public void trackJoin(@DestinationVariable String code, Principal principal,
                          SimpMessageHeaderAccessor headerAccessor) {
        String userId = resolveUserId(principal, headerAccessor);
        if (userId == null) return;
        sessionTracker.track(headerAccessor.getSessionId(), code, userId);
        try {
            roomService.markReconnected(code, userId);
        } catch (AppException ignored) {
        }
    }

    @MessageMapping("/space-room/{code}/answer")
    public void submitAnswer(@DestinationVariable String code,
                             @Payload SubmitAnswerRequest request,
                             Principal principal,
                             SimpMessageHeaderAccessor headerAccessor) {
        String userId = resolveUserId(principal, headerAccessor);
        if (userId == null) {
            log.warn("submitAnswer skipped: Unauthenticated STOMP request for room {}", code);
            return;
        }
        Long cardId = request != null ? request.getCardId() : null;
        roomService.submitAnswer(code, userId, cardId);
    }

    @MessageMapping("/space-room/{code}/select-deck")
    public void selectDeck(@DestinationVariable String code,
                           @Payload SelectDeckRequest request,
                           Principal principal,
                           SimpMessageHeaderAccessor headerAccessor) {
        String userId = resolveUserId(principal, headerAccessor);
        if (userId == null) return;
        roomService.selectPlayerDeck(code, userId, request != null ? request.getDeckId() : null);
    }
}