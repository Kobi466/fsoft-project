package com.fivetuat.fsoftprojectserver.services;

import org.springframework.data.domain.Pageable;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ChatMessageRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;

public interface ChatMessageService {
    PageResponse<ChatMessageResponse> getMessageHistory(Pageable pageable);
    ChatMessageResponse saveAndBroadcastMessage(ChatMessageRequest request, String senderUserId);
    int getOnlineUserCount();
    void incrementOnlineUsers();
    void decrementOnlineUsers();
}
