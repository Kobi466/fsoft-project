package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ChatMessageRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;
import org.springframework.data.domain.Pageable;

public interface ChatMessageService {
    ChatMessageResponse saveAndBroadcastMessage(ChatMessageRequest request, String senderId);
    PageResponse<ChatMessageResponse> getMessageHistory(Pageable pageable);
    int getOnlineUserCount();
}