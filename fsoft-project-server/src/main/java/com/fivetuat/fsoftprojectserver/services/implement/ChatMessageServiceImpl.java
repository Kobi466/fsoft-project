package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.fivetuat.fsoftprojectserver.listeners.WebSocketEventListener;
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

    @Override
    @Transactional
    public ChatMessageResponse saveAndBroadcastMessage(ChatMessageRequest request, String senderId) {
        UUID senderUuid;
        try {
            senderUuid = UUID.fromString(senderId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Auth sender = authRepository.findById(senderUuid)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));

        ChatMessage replyTo = null;
        if (request.getReplyToId() != null && !request.getReplyToId().trim().isEmpty()) {
            replyTo = chatMessageRepository.findById(request.getReplyToId().trim()).orElse(null);
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .content(request.getContent().trim())
                .replyTo(replyTo)
                .createdAt(Instant.now())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);
        ChatMessageResponse response = chatMessageMapper.toChatMessageResponse(saved);

        // Broadcast tin nhắn mới tới toàn bộ người dùng đang lắng nghe topic cộng đồng
        messagingTemplate.convertAndSend("/topic/community", response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatMessageResponse> getMessageHistory(Pageable pageable) {
        Page<ChatMessage> page = chatMessageRepository.findRecentMessages(pageable);

        List<ChatMessageResponse> responses = page.getContent().stream()
                .map(chatMessageMapper::toChatMessageResponse)
                .toList();

        return PageResponse.<ChatMessageResponse>builder()
                .content(responses)
                .pageNo(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public int getOnlineUserCount() {
        return WebSocketEventListener.getOnlineUserCount();
    }
}