package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiAuditLogResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;

import java.util.List;

public interface AdminAiChatService {
    AdminAiChatResponse chat(AdminAiChatRequest request);
    AdminAiChatResponse confirm(String actionId);
    AdminAiChatResponse cancel(String actionId);
    List<AdminAiChatHistoryResponse> getHistory(int page, int size);
    void clearHistory();
    PageResponse<AdminAiAuditLogResponse> getAuditLogs(int page, int size);
}