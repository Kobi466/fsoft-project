package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PendingActionResponse;
import com.fivetuat.fsoftprojectserver.services.AdminAiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminAiChatControllerTest {

    @Test
    void chatEndpointReturnsReplyAndPendingAction() throws Exception {
        AdminAiChatService service = mock(AdminAiChatService.class);
        when(service.chat(any(AdminAiChatRequest.class))).thenReturn(
                AdminAiChatResponse.builder()
                        .conversationId("admin-ai-x")
                        .reply("5 decks pending")
                        .pendingAction(PendingActionResponse.builder()
                                .actionId("abc")
                                .tool("requestRejectDeck")
                                .summary("Từ chối bộ bài #12")
                                .build())
                        .build());

        var mvc = MockMvcBuilders.standaloneSetup(new AdminAiChatController(service)).build();

        mvc.perform(post("/admin/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Từ chối deck 12 vì spam\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("5 decks pending"))
                .andExpect(jsonPath("$.data.pendingAction.actionId").value("abc"));
    }

    @Test
    void confirmEndpointDelegatesToService() throws Exception {
        AdminAiChatService service = mock(AdminAiChatService.class);
        when(service.confirm("abc")).thenReturn(
                AdminAiChatResponse.builder()
                        .conversationId("admin-ai-x")
                        .reply("Đã thực hiện: Từ chối bộ bài #12")
                        .build());

        var mvc = MockMvcBuilders.standaloneSetup(new AdminAiChatController(service)).build();

        mvc.perform(post("/admin/ai/chat/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"actionId\":\"abc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("Đã thực hiện: Từ chối bộ bài #12"));
    }
}