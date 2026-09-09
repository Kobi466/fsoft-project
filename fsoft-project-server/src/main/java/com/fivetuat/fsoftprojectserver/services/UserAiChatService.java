package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.UserAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatResponse;

import java.util.List;

public interface UserAiChatService {
    UserAiChatResponse chat(UserAiChatRequest request);
    List<UserAiChatHistoryResponse> getHistory(int page, int size);
    void clearHistory();
}
