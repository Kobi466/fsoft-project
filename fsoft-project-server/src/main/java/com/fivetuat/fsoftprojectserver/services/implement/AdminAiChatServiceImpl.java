    package com.fivetuat.fsoftprojectserver.services.implement;
    
    import com.fasterxml.jackson.core.JsonProcessingException;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
    import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
    import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateDeckOfficialRequest;
    import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateUserRolesRequest;
    import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiAuditLogResponse;
    import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse;
    import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;
    import com.fivetuat.fsoftprojectserver.dtos.response.PendingActionResponse;
    import com.fivetuat.fsoftprojectserver.entities.AdminAiAuditLog;
    import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
    import com.fivetuat.fsoftprojectserver.enums.PendingActionStatus;
    import com.fivetuat.fsoftprojectserver.exceptions.AppException;
    import com.fivetuat.fsoftprojectserver.repositories.AdminAiAuditLogRepository;
    import com.fivetuat.fsoftprojectserver.services.AdminAiChatService;
    import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
    import com.fivetuat.fsoftprojectserver.services.AdminUserService;
    import com.fivetuat.fsoftprojectserver.services.support.AdminAiTools;
    import com.fivetuat.fsoftprojectserver.services.support.PendingActionStore;
    import com.fivetuat.fsoftprojectserver.utils.PageUtils;
    import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
    import lombok.AccessLevel;
    import lombok.experimental.FieldDefaults;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.ai.chat.client.ChatClient;
    import org.springframework.ai.chat.memory.ChatMemory;
    import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
    import org.springframework.ai.chat.messages.Message;
    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.stereotype.Service;
    
    import java.time.LocalDateTime;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.UUID;
    import java.util.stream.Collectors;
    
    @Slf4j
    @Service
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class AdminAiChatServiceImpl implements AdminAiChatService {

        final ChatClient adminAiClient;
        final ChatMemory chatMemory;
        final AdminAiTools adminAiTools;
        final PendingActionStore pendingActionStore;
        final AdminAiAuditLogRepository auditLogRepository;
        final AdminDeckService adminDeckService;
        final AdminUserService adminUserService;
        final ObjectMapper objectMapper;

        public AdminAiChatServiceImpl(
                @Qualifier("adminAiClient") ChatClient adminAiClient,
                ChatMemory chatMemory,
                AdminAiTools adminAiTools,
                PendingActionStore pendingActionStore,
                AdminAiAuditLogRepository auditLogRepository,
                AdminDeckService adminDeckService,
                AdminUserService adminUserService,
                ObjectMapper objectMapper) {
            this.adminAiClient = adminAiClient;
            this.chatMemory = chatMemory;
            this.adminAiTools = adminAiTools;
            this.pendingActionStore = pendingActionStore;
            this.auditLogRepository = auditLogRepository;
            this.adminDeckService = adminDeckService;
            this.adminUserService = adminUserService;
            this.objectMapper = objectMapper;
        }
    
        @Override
        public AdminAiChatResponse chat(AdminAiChatRequest request) {
            UUID adminId = currentAdminId();
            String conversationId = adminId.toString();
    
            String reply;
            try {
                reply = adminAiClient.prompt()
                        .tools(adminAiTools)
                        .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                        .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .user(request.getMessage())
                        .call()
                        .content();
            } catch (AppException e) {
                throw e;
            } catch (Exception e) {
                log.error("Admin AI chat call failed: {}", e.getMessage(), e);
                throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
    
            PendingActionResponse pending = pendingActionStore.findLatestByAdmin(adminId)
                    .map(this::toPendingResponse)
                    .orElse(null);
    
            return AdminAiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .pendingAction(pending)
                    .build();
        }
    
        @Override
        public AdminAiChatResponse confirm(String actionId) {
            UUID adminId = currentAdminId();
            PendingActionStore.PendingAction action = pendingActionStore.find(actionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PENDING_ACTION_NOT_FOUND));
    
            if (!action.adminId().equals(adminId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
    
            if (action.expiresAt().isBefore(LocalDateTime.now())) {
                pendingActionStore.delete(actionId);
                pendingActionStore.clearLatestIndex(adminId);
                throw new AppException(ErrorCode.PENDING_ACTION_EXPIRED);
            }
    
            ExecutedAction executed;
            try {
                executed = executeAction(action);
            } catch (AppException e) {
                String result = e.getMessage() != null ? e.getMessage() : ("FAILED: " + e.getErrorCode());
                saveAudit(adminId, action, result, PendingActionStatus.FAILED, referenceIdFor(action));
                throw e;
            } catch (Exception e) {
                log.error("Admin AI confirm action failed unexpectedly: {}", e.getMessage(), e);
                saveAudit(adminId, action, "FAILED: " + e.getClass().getSimpleName(),
                        PendingActionStatus.FAILED, referenceIdFor(action));
                throw e;
            }
    
            saveAudit(adminId, action, executed.resultSummary(), PendingActionStatus.EXECUTED, executed.referenceId());
            pendingActionStore.delete(actionId);
            pendingActionStore.clearLatestIndex(adminId);
    
            return AdminAiChatResponse.builder()
                    .conversationId(adminId.toString())
                    .reply("Đã thực hiện: " + action.summary())
                    .pendingAction(null)
                    .result(executed.resultSummary())
                    .build();
        }
    
        @Override
        public AdminAiChatResponse cancel(String actionId) {
            UUID adminId = currentAdminId();
            PendingActionStore.PendingAction action = pendingActionStore.find(actionId)
                    .orElseThrow(() -> new AppException(ErrorCode.PENDING_ACTION_NOT_FOUND));
    
            if (!action.adminId().equals(adminId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
    
            if (action.expiresAt().isBefore(LocalDateTime.now())) {
                pendingActionStore.delete(actionId);
                pendingActionStore.clearLatestIndex(adminId);
                throw new AppException(ErrorCode.PENDING_ACTION_EXPIRED);
            }
    
            saveAudit(adminId, action, "Cancelled by admin", PendingActionStatus.CANCELLED, referenceIdFor(action));
            pendingActionStore.delete(actionId);
            pendingActionStore.clearLatestIndex(adminId);
    
            return AdminAiChatResponse.builder()
                    .conversationId(adminId.toString())
                    .reply("Đã hủy hành động: " + action.summary())
                    .pendingAction(null)
                    .build();
        }
    
        @Override
        public List<AdminAiChatHistoryResponse> getHistory(int page, int size) {
            String conversationId = currentAdminId().toString();
            List<Message> messages = chatMemory.get(conversationId);
            int to = Math.min(messages.size(), Math.max(0, messages.size() - (page - 1) * size));
            int from = Math.max(0, to - size);
            if (to <= from) {
                return List.of();
            }
            return messages.subList(from, to).stream()
                    .map(m -> AdminAiChatHistoryResponse.builder()
                            .role(m.getMessageType().getValue())
                            .content(m.getText())
                            .build())
                    .toList();
        }
    
        @Override
        public void clearHistory() {
            chatMemory.clear(currentAdminId().toString());
        }
    
        @Override
        public PageResponse<AdminAiAuditLogResponse> getAuditLogs(int page, int size) {
            UUID adminId = currentAdminId();
            Pageable pageable = PageUtils.createPageable(page, size);
            Page<AdminAiAuditLog> logPage = auditLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable);
            return PageUtils.toPageResponse(logPage, this::toAuditLogResponse);
        }
    
        private ExecutedAction executeAction(PendingActionStore.PendingAction action) {
            return switch (action.tool()) {
                case "requestApproveDeck" -> {
                    Long deckId = toLong(action.params().get("deckId"));
                    yield new ExecutedAction(
                            adminDeckService.approvePublicDeck(deckId, true, null).toString(),
                            "APPROVE_DECK", String.valueOf(deckId));
                }
                case "requestRejectDeck" -> {
                    Long deckId = toLong(action.params().get("deckId"));
                    String reason = String.valueOf(action.params().get("reason"));
                    yield new ExecutedAction(
                            adminDeckService.approvePublicDeck(deckId, false, reason).toString(),
                            "REJECT_DECK", String.valueOf(deckId));
                }
                case "requestDeleteDeck" -> {
                    Long deckId = toLong(action.params().get("deckId"));
                    adminDeckService.deleteDeck(deckId);
                    yield new ExecutedAction("Deck deleted", "DELETE_DECK", String.valueOf(deckId));
                }
                case "requestSetOfficial" -> {
                    Long deckId = toLong(action.params().get("deckId"));
                    boolean official = Boolean.parseBoolean(String.valueOf(action.params().get("isOfficial")));
                    AdminUpdateDeckOfficialRequest req = new AdminUpdateDeckOfficialRequest();
                    req.setIsOfficial(official);
                    yield new ExecutedAction(
                            adminDeckService.updateDeckOfficial(deckId, req).toString(),
                            "SET_OFFICIAL", String.valueOf(deckId));
                }
                case "requestUpdateUserRoles" -> {
                    String userId = String.valueOf(action.params().get("userId"));
                    Set<String> roles = Arrays.stream(
                                    String.valueOf(action.params().get("rolesCsv")).split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet());
                    AdminUpdateUserRolesRequest req = AdminUpdateUserRolesRequest.builder()
                            .roleNames(roles)
                            .build();
                    yield new ExecutedAction(
                            adminUserService.updateUserRoles(UUID.fromString(userId), req).toString(),
                            "UPDATE_USER_ROLES", userId);
                }
                default -> throw new AppException(ErrorCode.PENDING_ACTION_NOT_FOUND);
            };
        }
    
        private record ExecutedAction(String resultSummary, String auditAction, String referenceId) {
        }
    
        private Long toLong(Object o) {
            return o instanceof Number n ? n.longValue() : Long.parseLong(String.valueOf(o));
        }
    
        private UUID currentAdminId() {
            return SecurityUtils.getCurrentUserLogin()
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        }
    
        private PendingActionResponse toPendingResponse(PendingActionStore.PendingAction a) {
            return PendingActionResponse.builder()
                    .actionId(a.actionId().toString())
                    .tool(a.tool())
                    .summary(a.summary())
                    .expiresAt(a.expiresAt())
                    .build();
        }
    
        private AdminAiAuditLogResponse toAuditLogResponse(AdminAiAuditLog l) {
            return AdminAiAuditLogResponse.builder()
                    .id(l.getId())
                    .action(l.getAction())
                    .params(l.getParams())
                    .status(l.getStatus())
                    .resultSummary(l.getResultSummary())
                    .referenceId(l.getReferenceId())
                    .createdAt(l.getCreatedAt())
                    .build();
        }
    
        private void saveAudit(UUID adminId, PendingActionStore.PendingAction action,
                               String result, PendingActionStatus status, String referenceId) {
            auditLogRepository.save(AdminAiAuditLog.builder()
                    .adminId(adminId)
                    .action(auditActionName(action.tool()))
                    .params(toJson(action.params()))
                    .status(status.name())
                    .resultSummary(result != null && result.length() > 500 ? result.substring(0, 500) : result)
                    .referenceId(referenceId)
                    .build());
        }
    
        private String auditActionName(String tool) {
            return switch (tool) {
                case "requestApproveDeck" -> "APPROVE_DECK";
                case "requestRejectDeck" -> "REJECT_DECK";
                case "requestDeleteDeck" -> "DELETE_DECK";
                case "requestSetOfficial" -> "SET_OFFICIAL";
                case "requestUpdateUserRoles" -> "UPDATE_USER_ROLES";
                default -> tool.toUpperCase();
            };
        }
    
        private String referenceIdFor(PendingActionStore.PendingAction action) {
            Object deckId = action.params().get("deckId");
            if (deckId != null) return String.valueOf(deckId);
            Object userId = action.params().get("userId");
            return userId != null ? String.valueOf(userId) : null;
        }
    
        private String toJson(Map<String, Object> params) {
            try {
                return objectMapper.writeValueAsString(params);
            } catch (JsonProcessingException e) {
                return String.valueOf(params);
            }
        }
    }
