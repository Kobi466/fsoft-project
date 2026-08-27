package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.AiRoleplayRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AiRoleplayService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiRoleplayServiceImpl implements AiRoleplayService {

    ChatClient groqClient;
    ChatMemory chatMemory;
    Resource systemPromptResource;
    Resource userPromptResource;

    public AiRoleplayServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Value("classpath:/prompts/roleplay-tutor-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/roleplay-tutor-user.st") Resource userPromptResource,
            ChatMemory chatMemory) {
        this.groqClient = groqClient;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
        this.chatMemory = chatMemory;
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

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        String systemMessage = systemTemplate.render(Map.of(
                "scenario", request.getScenario() != null ? request.getScenario() : "JOB_INTERVIEW",
                "format", format
        ));

        PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
        String targetWordsStr = request.getTargetWords() != null ? String.join(", ", request.getTargetWords()) : "Không yêu cầu";
        String userMessage = userTemplate.render(Map.of(
                "scenario", request.getScenario() != null ? request.getScenario() : "JOB_INTERVIEW",
                "targetWords", targetWordsStr,
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

            String cleanedResponse = sanitizeJsonResponse(rawResponse);
            AiRoleplayResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
            }

            return result;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Roleplay Tutor: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
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
        if (messages == null) {
            return Collections.emptyList();
        }

        return messages.stream()
                .map(msg -> AiRoleplayHistoryResponse.builder()
                        .role(msg.getMessageType().name())
                        .content(msg.getText())
                        .build())
                .collect(Collectors.toList());
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

    private String sanitizeJsonResponse(String rawResponse) {
        if (rawResponse == null) return "";
        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}
