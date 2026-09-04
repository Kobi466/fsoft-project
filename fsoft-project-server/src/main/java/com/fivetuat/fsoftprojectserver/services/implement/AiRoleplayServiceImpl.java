package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivetuat.fsoftprojectserver.dtos.request.AiRoleplayRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CreateAiRoleplaySessionRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplaySessionResponse;
import com.fivetuat.fsoftprojectserver.entities.AiRoleplaySession;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.AiRoleplaySessionMapper;
import com.fivetuat.fsoftprojectserver.repositories.AiRoleplaySessionRepository;
import com.fivetuat.fsoftprojectserver.repositories.ProfileRepository;
import com.fivetuat.fsoftprojectserver.services.AiRoleplayService;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AiRoleplayServiceImpl implements AiRoleplayService {

    final ChatClient groqClient;
    final ChatMemory chatMemory;
    final Resource systemPromptResource;
    final Resource userPromptResource;
    final AiRoleplaySessionRepository sessionRepository;
    final ProfileRepository profileRepository;
    final AiRoleplaySessionMapper sessionMapper;
    final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired(required = false)
    JdbcTemplate jdbcTemplate;

    public AiRoleplayServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Value("classpath:/prompts/roleplay-tutor-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/roleplay-tutor-user.st") Resource userPromptResource,
            ChatMemory chatMemory,
            AiRoleplaySessionRepository sessionRepository,
            ProfileRepository profileRepository,
            AiRoleplaySessionMapper sessionMapper) {
        this.groqClient = groqClient;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
        this.chatMemory = chatMemory;
        this.sessionRepository = sessionRepository;
        this.profileRepository = profileRepository;
        this.sessionMapper = sessionMapper;
    }

    @Override
    public AiRoleplayResponse processRoleplay(AiRoleplayRequest request) {
        log.info("Bắt đầu AI Roleplay Tutor kịch bản: {}", request.getScenario());

        String rawConvId = request.getConversationId();
        if (rawConvId == null || rawConvId.isBlank()) {
            rawConvId = SecurityUtils.getCurrentUserLogin()
                    .map(UUID::toString)
                    .orElse("default-session-000000000000000000");
        }
        if (rawConvId.length() > 36) {
            rawConvId = rawConvId.substring(0, 36);
        }
        final String conversationId = rawConvId;

        BeanOutputConverter<AiRoleplayResponse> converter = new BeanOutputConverter<>(AiRoleplayResponse.class);
        String format = converter.getFormat();

        String targetWordsStr = request.getTargetWords() != null ? String.join(", ", request.getTargetWords()) : "Không yêu cầu";

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        String systemMessage = systemTemplate.render(Map.of(
                "scenario", request.getScenario() != null ? request.getScenario() : "JOB_INTERVIEW",
                "targetWords", targetWordsStr,
                "format", format
        ));

        PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
        String userMessage = userTemplate.render(Map.of(
                "userMessage", request.getUserMessage()
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                    .advisors(advisorSpec -> {
                        advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId);
                    })
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            AiRoleplayResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
            }

            saveOrUpdateSession(conversationId, request);

            return result;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Roleplay Tutor: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }

    private void saveOrUpdateSession(String conversationId, AiRoleplayRequest request) {
        try {
            UUID profileId = SecurityUtils.getCurrentUserLogin().orElse(null);
            if (profileId == null) return;

            sessionRepository.findByIdAndProfileId(conversationId, profileId).ifPresentOrElse(session -> {
                session.setUpdatedAt(LocalDateTime.now());
                if (request.getScenario() != null && !request.getScenario().isBlank()) {
                    session.setScenario(request.getScenario());
                }
                if (request.getTargetWords() != null && !request.getTargetWords().isEmpty()) {
                    session.setTargetWords(String.join(", ", request.getTargetWords()));
                }
                sessionRepository.save(session);
            }, () -> {
                profileRepository.findById(profileId).ifPresent(profile -> {
                    String title = request.getScenario() != null && !request.getScenario().isBlank()
                            ? request.getScenario() : "Phiên hội thoại";
                    String twStr = request.getTargetWords() != null ? String.join(", ", request.getTargetWords()) : "";
                    AiRoleplaySession newSession = AiRoleplaySession.builder()
                            .id(conversationId)
                            .profile(profile)
                            .title(title)
                            .scenario(request.getScenario())
                            .targetWords(twStr)
                            .build();
                    sessionRepository.save(newSession);
                });
            });
        } catch (Exception e) {
            log.warn("Không thể tự động lưu phiên hội thoại vào sessionRepository: {}", e.getMessage());
        }
    }

    @Override
    public List<AiRoleplayHistoryResponse> getRoleplayHistory(String conversationId) {
        String targetConvId = conversationId;
        if (targetConvId == null || targetConvId.isBlank()) {
            targetConvId = SecurityUtils.getCurrentUserLogin()
                    .map(UUID::toString)
                    .orElse("default-session-000000000000000000");
        }
        if (targetConvId.length() > 36) {
            targetConvId = targetConvId.substring(0, 36);
        }

        List<Message> messages = chatMemory.get(targetConvId);
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }

        List<Message> filteredMessages = messages.stream()
                .filter(msg -> msg.getMessageType() != MessageType.SYSTEM)
                .toList();

        List<LocalDateTime> dbTimestamps = fetchDbTimestamps(targetConvId);

        List<AiRoleplayHistoryResponse> result = new ArrayList<>();
        for (int i = 0; i < filteredMessages.size(); i++) {
            Message msg = filteredMessages.get(i);
            String content = msg.getText();
            if (msg.getMessageType() == MessageType.ASSISTANT && content != null) {
                content = extractTutorReplyContent(content);
            } else if (msg.getMessageType() == MessageType.USER && content != null) {
                content = extractUserMessageContent(content);
            }

            LocalDateTime timestamp = (i < dbTimestamps.size() && dbTimestamps.get(i) != null)
                    ? dbTimestamps.get(i)
                    : extractMessageTimestamp(msg);

            result.add(AiRoleplayHistoryResponse.builder()
                    .role(msg.getMessageType().name())
                    .content(content)
                    .createdAt(timestamp)
                    .build());
        }

        return result;
    }

    @Override
    public List<AiRoleplaySessionResponse> getUserSessions() {
        UUID profileId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        List<AiRoleplaySession> sessions = sessionRepository.findByProfileIdOrderByUpdatedAtDesc(profileId);
        return sessionMapper.toResponseList(sessions);
    }

    @Override
    public AiRoleplaySessionResponse createSession(CreateAiRoleplaySessionRequest request) {
        UUID profileId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        AiRoleplaySession session = sessionMapper.toEntity(request);
        session.setId(UUID.randomUUID().toString());
        session.setProfile(profile);

        if (session.getTitle() == null || session.getTitle().isBlank()) {
            session.setTitle(request.getScenario() != null && !request.getScenario().isBlank()
                    ? request.getScenario() : "Phiên hội thoại mới");
        }

        AiRoleplaySession saved = sessionRepository.save(session);
        return sessionMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        UUID profileId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        sessionRepository.deleteByIdAndProfileId(sessionId, profileId);
        chatMemory.clear(sessionId);
        log.info("Đã xóa phiên hội thoại và chatMemory cho sessionId: {}", sessionId);
    }

    private List<LocalDateTime> fetchDbTimestamps(String conversationId) {
        if (jdbcTemplate == null) return Collections.emptyList();
        try {
            String sql = """
                    SELECT "timestamp" FROM spring_ai_chat_memory 
                    WHERE conversation_id = ? 
                    AND type != 'SYSTEM' 
                    ORDER BY sequence_id ASC
                    """;
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                java.sql.Timestamp ts = rs.getTimestamp("timestamp");
                return ts != null ? ts.toLocalDateTime() : null;
            }, conversationId);
        } catch (Exception e) {
            log.warn("Không thể truy vấn timestamp trực tiếp từ DB: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private LocalDateTime extractMessageTimestamp(Message msg) {
        if (msg != null && msg.getMetadata() != null) {
            Object ts = msg.getMetadata().get("timestamp");
            if (ts instanceof LocalDateTime ldt) {
                return ldt;
            } else if (ts instanceof java.sql.Timestamp sqlTs) {
                return sqlTs.toLocalDateTime();
            } else if (ts instanceof java.time.Instant instant) {
                return LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault());
            } else if (ts instanceof Long epochMilli) {
                return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli), java.time.ZoneId.systemDefault());
            } else if (ts instanceof String strTs) {
                try {
                    return LocalDateTime.parse(strTs);
                } catch (Exception ignored) {
                }
            }
        }
        return LocalDateTime.now();
    }

    @Override
    public void clearRoleplayHistory(String conversationId) {
        String targetConvId = conversationId;
        if (targetConvId == null || targetConvId.isBlank()) {
            targetConvId = SecurityUtils.getCurrentUserLogin()
                    .map(UUID::toString)
                    .orElse("default-session-000000000000000000");
        }
        if (targetConvId.length() > 36) {
            targetConvId = targetConvId.substring(0, 36);
        }

        chatMemory.clear(targetConvId);
        log.info("Đã xóa lịch sử hội thoại AI Roleplay cho conversationId: {}", targetConvId);
    }

    private String extractTutorReplyContent(String rawText) {
        try {
            String cleaned = AiResponseUtils.sanitizeJsonResponse(rawText);
            JsonNode node = objectMapper.readTree(cleaned);
            if (node.has("tutorReply")) {
                return node.get("tutorReply").asText();
            }
        } catch (Exception ignored) {
        }
        return rawText;
    }

    private String extractUserMessageContent(String rawText) {
        if (rawText == null) return "";
        int index = rawText.indexOf("Câu trả lời/tin nhắn của học viên:");
        if (index != -1) {
            String sub = rawText.substring(index + "Câu trả lời/tin nhắn của học viên:".length()).trim();
            if (sub.startsWith("\"") && sub.endsWith("\"") && sub.length() >= 2) {
                return sub.substring(1, sub.length() - 1).trim();
            }
            return sub;
        }
        return rawText.trim();
    }
}
