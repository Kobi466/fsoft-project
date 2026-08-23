package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ChatMessageRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;
import com.fivetuat.fsoftprojectserver.entities.ChatMessage;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.ChatMessageMapper;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.ChatMessageRepository;
import com.fivetuat.fsoftprojectserver.services.ChatMessageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatMessageServiceImpl implements ChatMessageService {

    ChatMessageRepository chatMessageRepository;
    AuthRepository authRepository;
    ChatMessageMapper chatMessageMapper;
    SimpMessagingTemplate messagingTemplate;

    AtomicInteger onlineUserCount = new AtomicInteger(1);

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> getMessageHistory(Pageable pageable) {
        Page<ChatMessage> page = chatMessageRepository.findRecentMessages(pageable);
        List<ChatMessageResponse> content = page.getContent().stream()
                .map(chatMessageMapper::toResponse)
                .toList();

        List<ChatMessageResponse> reversedContent = new java.util.ArrayList<>(content);
        Collections.reverse(reversedContent);

        return PageResponse.<ChatMessageResponse>builder()
                .pageNo(pageable.getPageNumber() + 1)
                .totalPages(page.getTotalPages())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .last(page.isLast())
                .content(reversedContent)
                .build();
    }

    @Override
    @Transactional
    public ChatMessageResponse saveAndBroadcastMessage(ChatMessageRequest request, String senderUserId) {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(senderUserId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Auth sender = authRepository.findById(userUuid)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));

        ChatMessage replyToMessage = null;
        if (request.getReplyToId() != null && !request.getReplyToId().isBlank()) {
            replyToMessage = chatMessageRepository.findById(request.getReplyToId()).orElse(null);
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .content(request.getContent())
                .replyTo(replyToMessage)
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = chatMessageMapper.toResponse(saved);

        try {
            messagingTemplate.convertAndSend("/topic/community", response);
        } catch (Exception ignored) {}

        return response;
    }

    @Override
    public int getOnlineUserCount() {
        return Math.max(1, onlineUserCount.get());
    }

    @Override
    public void incrementOnlineUsers() {
        onlineUserCount.incrementAndGet();
    }

    @Override
    public void decrementOnlineUsers() {
        if (onlineUserCount.get() > 1) {
            onlineUserCount.decrementAndGet();
        }
    }
}
