package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;
import com.fivetuat.fsoftprojectserver.entities.AdminAiAuditLog;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
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

import java.time.LocalDateTime;
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

    private PendingActionStore.PendingAction pendingAction(String tool, Map<String, Object> params, String summary) {
        return new PendingActionStore.PendingAction(
                UUID.randomUUID(), tool, params, adminId, summary,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void chatDerivesConversationIdFromAdminProfile() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            mockAiReply("Hiện có 5 bộ bài chờ duyệt");
            when(pendingActionStore.findLatestByAdmin(adminId)).thenReturn(Optional.empty());

            AdminAiChatResponse response = impl().chat(
                    new AdminAiChatRequest("Có bao nhiêu bộ bài đang chờ duyệt?", null));

            assertEquals(adminId.toString(), response.getConversationId());
            assertEquals("Hiện có 5 bộ bài chờ duyệt", response.getReply());
            assertNull(response.getPendingAction());
            assertNull(response.getResult());
        }
    }

    @Test
    void chatSurfacesPendingActionFromLatestIndex() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            mockAiReply("Đã soạn hành động từ chối, xin xác nhận");
            PendingActionStore.PendingAction action = pendingAction("requestRejectDeck",
                    Map.of("deckId", 12, "reason", "spam"), "Từ chối bộ bài #12");
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
                    "Từ chối bộ bài #12", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(adminDeckService.approvePublicDeck(12L, false, "spam")).thenReturn(anyDeckResponse());
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AdminAiChatResponse response = impl().confirm(actionId.toString());

            assertTrue(response.getReply().contains("Đã thực hiện"));
            assertEquals(anyDeckResponse().toString(), response.getResult());
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
    void confirmWritesFailedAuditWhenServiceThrowsAppException() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestApproveDeck",
                    Map.of("deckId", 12), adminId,
                    "Duyệt bộ bài #12", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(adminDeckService.approvePublicDeck(12L, true, null))
                    .thenThrow(new AppException(ErrorCode.DECK_NOT_FOUND));
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThrows(AppException.class, () -> impl().confirm(actionId.toString()));

            verify(auditLogRepository).save(argThat(log ->
                    "APPROVE_DECK".equals(log.getAction())
                            && "FAILED".equals(log.getStatus())
                            && "12".equals(log.getReferenceId())));
            verify(pendingActionStore, never()).delete(anyString());
        }
    }

    @Test
    void confirmWritesFailedAuditWhenServiceThrowsUnexpectedException() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestRejectDeck",
                    Map.of("deckId", "not-a-number", "reason", "spam"), adminId,
                    "Từ chối bộ bài #not-a-number", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            assertThrows(NumberFormatException.class, () -> impl().confirm(actionId.toString()));

            verify(auditLogRepository).save(argThat(log ->
                    "REJECT_DECK".equals(log.getAction())
                            && "FAILED".equals(log.getStatus())));
            verify(pendingActionStore, never()).delete(anyString());
        }
    }

    @Test
    void confirmThrowsExpiredAndClearsStaleAction() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestApproveDeck",
                    Map.of("deckId", 12), adminId,
                    "Duyệt bộ bài #12", LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().minusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));

            AppException ex = assertThrows(AppException.class, () -> impl().confirm(actionId.toString()));

            assertEquals(ErrorCode.PENDING_ACTION_EXPIRED, ex.getErrorCode());
            verify(adminDeckService, never()).approvePublicDeck(any(), anyBoolean(), any());
            verify(pendingActionStore).delete(actionId.toString());
            verify(pendingActionStore).clearLatestIndex(adminId);
            verify(auditLogRepository, never()).save(any(AdminAiAuditLog.class));
        }
    }

    @Test
    void cancelThrowsExpiredAndClearsStaleAction() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            UUID actionId = UUID.randomUUID();
            PendingActionStore.PendingAction action = new PendingActionStore.PendingAction(
                    actionId, "requestApproveDeck",
                    Map.of("deckId", 12), adminId,
                    "Duyệt bộ bài #12", LocalDateTime.now().minusMinutes(10),
                    LocalDateTime.now().minusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));

            AppException ex = assertThrows(AppException.class, () -> impl().cancel(actionId.toString()));

            assertEquals(ErrorCode.PENDING_ACTION_EXPIRED, ex.getErrorCode());
            verify(pendingActionStore).delete(actionId.toString());
            verify(pendingActionStore).clearLatestIndex(adminId);
            verify(auditLogRepository, never()).save(any(AdminAiAuditLog.class));
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
                    "Xóa bộ bài #12", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));

            assertThrows(AppException.class,
                    () -> impl().confirm(actionId.toString()));

            verify(adminDeckService, never()).deleteDeck(any());
        }
    }

    @Test
    void confirmMissingActionThrowsNotFound() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            when(pendingActionStore.find(anyString())).thenReturn(Optional.empty());

            assertThrows(AppException.class,
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
                    "Duyệt bộ bài #7", LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
            when(pendingActionStore.find(actionId.toString())).thenReturn(Optional.of(action));
            when(auditLogRepository.save(any(AdminAiAuditLog.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            AdminAiChatResponse response = impl().cancel(actionId.toString());

            assertTrue(response.getReply().contains("Đã hủy"));
            assertNull(response.getResult());
            verify(auditLogRepository).save(argThat(log -> "CANCELLED".equals(log.getStatus())));
            verify(pendingActionStore).delete(actionId.toString());
        }
    }

    @Test
    void getHistoryReturnsLatestPageFirst() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            List<Message> messages = buildMessages(25);
            when(chatMemory.get(adminId.toString())).thenReturn(messages);

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

    @Test
    void getHistoryClampsPageZeroAndOversizedPage() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            List<Message> messages = buildMessages(5);
            when(chatMemory.get(adminId.toString())).thenReturn(messages);

            // page 0 and negative pages clamp to the first page (most recent messages)
            List<com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse> clamped =
                    impl().getHistory(0, 10);
            assertEquals(5, clamped.size());
            assertEquals("msg-1", clamped.get(0).getContent());
            assertEquals("msg-5", clamped.get(4).getContent());

            List<com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse> negative =
                    impl().getHistory(-3, 10);
            assertEquals(5, negative.size());

            assertEquals(List.of(), impl().getHistory(99, 10));

            List<com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse> bigSize =
                    impl().getHistory(1, 100);
            assertEquals(5, bigSize.size());
            assertEquals("msg-1", bigSize.get(0).getContent());
            assertEquals("msg-5", bigSize.get(4).getContent());
        }
    }

    @Test
    void getHistoryReturnsEmptyWhenMemoryEmpty() {
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(adminId));
            when(chatMemory.get(adminId.toString())).thenReturn(List.of());

            assertEquals(List.of(), impl().getHistory(1, 10));
        }
    }

    private List<Message> buildMessages(int count) {
        List<Message> messages = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            messages.add(i % 2 == 1
                    ? new UserMessage("msg-" + i)
                    : new AssistantMessage("msg-" + i));
        }
        return messages;
    }

    private com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse anyDeckResponse() {
        return com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse.builder()
                .id(12L).title("deck").build();
    }
}
