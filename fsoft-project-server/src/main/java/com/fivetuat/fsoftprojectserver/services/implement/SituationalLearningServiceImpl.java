package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.SituationalLearningResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.SituationalLearningService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import org.springframework.ai.chat.prompt.SystemPromptTemplate;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.SituationalLearningRequest;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SituationalLearningServiceImpl implements SituationalLearningService {

    ChatClient groqClient;
    Resource systemPromptResource;
    Resource userPromptResource;

    public SituationalLearningServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Value("classpath:/prompts/situational-learning-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/situational-learning-user.st") Resource userPromptResource) {
        this.groqClient = groqClient;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
    }

    @Override
    public SituationalLearningResponse generateLesson(SituationalLearningRequest request) {
        log.info("Bắt đầu tạo bài học ngữ cảnh (Text-only) cho mô tả: {}", request.getContext());

        BeanOutputConverter<SituationalLearningResponse> converter = new BeanOutputConverter<>(SituationalLearningResponse.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
        String userMessage = userTemplate.render(Map.of(
                "context", request.getContext(),
                "currentTime", request.getCurrentTime() != null ? request.getCurrentTime() : "Không cung cấp",
                "location", request.getLocation() != null ? request.getLocation() : "Không cung cấp"
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = sanitizeJsonResponse(rawResponse);
            SituationalLearningResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
            }

            return result;
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI tạo bài học ngữ cảnh: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
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
