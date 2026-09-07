package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.SentenceOption;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.SituationalLearningRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.SituationalLearningResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.SituationalLearningService;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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

    @Retry(name = "aiRetry", fallbackMethod = "handleAiRetryFallback")
    @Override
    @Cacheable(value = "ai_situational_lessons", key = "#request.context.toLowerCase().trim()", unless = "#result == null")
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

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
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

    public SituationalLearningResponse handleAiRetryFallback(SituationalLearningRequest request, Throwable t) {
        log.error("AI API gặp sự cố sau 3 lần thử lại cho ngữ cảnh: {}. Lỗi: {}", request.getContext(), t.getMessage());
        SentenceOption fallbackNotice = SentenceOption.builder()
                .english("AI service is currently unavailable. Please try again later.")
                .vietnamese("Dịch vụ AI hiện tại chưa phản hồi. Vui lòng thử lại sau ít phút.")
                .build();
        return SituationalLearningResponse.builder()
                .mainActions(List.of(fallbackNotice))
                .interactions(List.of(fallbackNotice))
                .emotions(List.of(fallbackNotice))
                .shortCaptions(List.of(SentenceOption.builder()
                        .english("System Maintenance")
                        .vietnamese("Bảo trì hệ thống")
                        .build()))
                .vocabularies(Collections.emptyList())
                .build();
    }
}
