package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.AiRoleplayRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayResponse;

import java.util.List;

public interface AiRoleplayService {
    AiRoleplayResponse processRoleplay(AiRoleplayRequest request);
    List<AiRoleplayHistoryResponse> getRoleplayHistory(String conversationId);
    void clearRoleplayHistory(String conversationId);
}
