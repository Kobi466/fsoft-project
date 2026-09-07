package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;
import com.fivetuat.fsoftprojectserver.entities.AdminAiAuditLog;
import com.fivetuat.fsoftprojectserver.repositories.AdminAiAuditLogRepository;
import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import com.fivetuat.fsoftprojectserver.services.support.AdminAiTools;
import com.fivetuat.fsoftprojectserver.services.support.PendingActionStore;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAiChatServiceImplTest {

    @Mock ChatClient adminAiClient;
    @Mock ChatMemory chatMemory;
    @Mock AdminAiTools adminAiTools;
    @Mock PendingActionStore pendingActionStore;
    @Mock AdminAiAuditLogRepository auditLogRepository;
    @Mock AdminDeckService adminDeckService;
    @Mock AdminUserService adminUserService;

    UUID adminId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

    private AdminAiChatServiceImpl impl() {
        return new AdminAiChatServiceImpl(
                adminAiClient, chatMemory, adminAiTools, pendingActionStore,
                auditLogRepository, adminDeckService, adminUserService, new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @AfterEach
    void cleanup() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    private void mockAiReply(String reply) {
        ChatClient.ChatClientRequestSpec spec = mock(ChatClient.ChatClientRequestSpec.class, RETURNS_SELF);
        ChatClient.CallResponseSpec callSpec = mock(ChatClient.CallResponseSpec.class);
        when(adminAiClient.prompt()).thenReturn(spec);
        when(spec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(reply);
    }

    @Test
    void chatDerivesConversationIdFromAdminProfile() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            mockAiReply("Hiện có 5 bộ bài chờ duyệt");
            when(pendingActionStore.findLatestByAdmin(adminId)).thenReturn(Optional.empty());

            AdminAiChatResponse response = impl().chat(
                    new AdminAiChatRequest("Có bao nhiêu bộ bài đang chờ duyệt?", null));

            assertEquals("admin-ai-" + adminId, response.getConversationId());
            assertEquals("Hiện có 5 bộ bài chờ duyệt", response.getReply());
            assertNull(response.getPendingAction());
        }
    }

    @Test
    void chatSurfacesPendingActionFromLatestIndex() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            mockAiReply("Đã soạn hành động từ chối, xin xác nhận");
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    UUID.randomUUID(), "requestRejectDeck",
                    Map.of("deckId", 12, "reason", "spam"), adminId,
                    "Từ chối bộ bài #12", java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.findLatestByAdmin(adminId)).thenReturn(Optional.of(action));

            AdminAiChatResponse response = impl().chat(
                    new AdminAiChatRequest("Từ chối bộ bài 12 vì spam", null));

            assertNotNull(response.getPendingAction());
            assertEquals("requestRejectDeck", response.getPendingAction().getTool());
            assertEquals(action.actionId().toString(), response.getPendingAction().getActionId());
        }
    }

    @Test
    void confirmExecutesRejectDeckAndWritesAudit() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestRejectDeck",
                    Map.of("deckId", 12, "reason", "spam"), adminId,
                    "Từ chối bộ bài #12", java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(adminDeckService.approvePublicDeck(12L, false, "spam")).thenReturn(anyDeckResponse());
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AdminAiChatResponse response = impl().confirm(actionId.toString());

            assertTrue(response.getReply().contains("Đã thực hiện"));
            verify(adminDeckService).approvePublicDeck(12L, false, "spam");
            verify(pendingActionStore).delete(actionId.toString());
            verify(pendingActionStore).clearLatestIndex(adminId);
            verify(auditLogRepository).save(argThat(log ->
                    "REJECT_DECK".equals(log.getAction())
                            && "EXECUTED".equals(log.getStatus())
                            && "12".equals(log.getReferenceId())));
        }
    }

    @Test
    void confirmRejectsActionOwnedByAnotherAdmin() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestDeleteDeck",
                    Map.of("deckId", 12), UUID.randomUUID(),
                    "Xóa bộ bài #12", java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));

            assertThrows(com.fivetuat.fsoftprojectserver.exceptions.AppException.class,
                    () -> impl().confirm(actionId.toString()));

            verify(adminDeckService, never()).deleteDeck(any());
        }
    }

    @Test
    void confirmMissingActionThrowsNotFound() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            when(pendingActionStore.find(anyString())).thenReturn(Optional.empty());

            assertThrows(com.fivetuat.fsoftprojectserver.exceptions.AppException.class,
                    () -> impl().confirm(UUID.randomUUID().toString()));
        }
    }

    @Test
    void cancelWritesCancelledAudit() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestApproveDeck",
                    Map.of("deckId", 7), adminId,
                    "Duyệt bộ bài #7", java.time.LocalDateTime.now(), java.time.LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AdminAiChatResponse response = impl().cancel(actionId.toString());

            assertTrue(response.getReply().contains("Đã hủy"));
            verify(auditLogRepository).save(argThat(log -> "CANCELLED".equals(log.getStatus())));
            verify(pendingActionStore).delete(actionId.toString());
        }
    }

    @Test
    void getHistoryReturnsLatestPageFirst() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            List<Message> messages = new ArrayList<>();
            for (int i = 1; i <= 25; i++) {
                messages.add(i % 2 == 1
                        ? new UserMessage("msg-" + i)
                        : new AssistantMessage("msg-" + i));
            }
            when(chatMemory.get("admin-ai-" + adminId)).thenReturn(messages);

            List<com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse> page1 =
                    impl().getHistory(1, 10);

            assertEquals(10, page1.size());
            assertEquals("msg-16", page1.get(0).getContent());
            assertEquals("msg-25", page1.get(9).getContent());
            assertEquals("assistant", page1.get(0).getRole());
            assertEquals("user", page1.get(9).getRole());

            List<com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse> page3 =
                    impl().getHistory(3, 10);

            assertEquals(5, page3.size());
            assertEquals("msg-1", page3.get(0).getContent());
            assertEquals("msg-5", page3.get(4).getContent());
        }
    }

    private com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse anyDeckResponse() {
        return com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse.builder()
                .id(12L).title("deck").build();
    }
}
