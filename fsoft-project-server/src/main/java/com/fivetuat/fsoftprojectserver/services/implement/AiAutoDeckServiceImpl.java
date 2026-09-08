package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.dtos.request.AiAutoDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiAutoDeckResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AiAutoDeckService;
import com.fivetuat.fsoftprojectserver.utils.AiFallbackExecutor;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiAutoDeckServiceImpl implements AiAutoDeckService {

    ChatClient groqClient;
    AiFallbackExecutor aiFallbackExecutor;
    Resource systemPromptResource;
    Resource userPromptResource;

    public AiAutoDeckServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            AiFallbackExecutor aiFallbackExecutor,
            @Value("classpath:/prompts/auto-deck-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/auto-deck-user.st") Resource userPromptResource) {
        this.groqClient = groqClient;
        this.aiFallbackExecutor = aiFallbackExecutor;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
    }

    @Override
    @Retry(name = "aiRetry", fallbackMethod = "handleGenerateAutoDeckRetryFallback")
    public AiAutoDeckResponse generateAutoDeck(AiAutoDeckRequest request) {
        log.info("Bắt đầu tự động tạo Bộ bài AI chủ đề: '{}', số lượng: {}", request.getTopic(), request.getCardCount());

        BeanOutputConverter<AiAutoDeckResponse> converter = new BeanOutputConverter<>(AiAutoDeckResponse.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
        String userMessage = userTemplate.render(Map.of(
                "topic", request.getTopic(),
                "cardCount", request.getCardCount() > 0 ? request.getCardCount() : 15,
                "sourceLanguage", request.getSourceLanguage() != null ? request.getSourceLanguage() : "en",
                "targetLanguage", request.getTargetLanguage() != null ? request.getTargetLanguage() : "vi"
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            AiAutoDeckResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
            }

            result.setSourceLanguage(request.getSourceLanguage());
            result.setTargetLanguage(request.getTargetLanguage());
            return result;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Auto Deck Generator (Groq): {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }

    public AiAutoDeckResponse handleGenerateAutoDeckRetryFallback(AiAutoDeckRequest request, Throwable t) {
        AiAutoDeckResponse geminiResult = aiFallbackExecutor.executeGeminiFallback(
                systemPromptResource,
                null,
                userPromptResource,
                Map.of(
                        "topic", request.getTopic(),
                        "cardCount", request.getCardCount() > 0 ? request.getCardCount() : 15,
                        "sourceLanguage", request.getSourceLanguage() != null ? request.getSourceLanguage() : "en",
                        "targetLanguage", request.getTargetLanguage() != null ? request.getTargetLanguage() : "vi"
                ),
                AiAutoDeckResponse.class,
                t
        );

        if (geminiResult != null) {
            geminiResult.setSourceLanguage(request.getSourceLanguage());
            geminiResult.setTargetLanguage(request.getTargetLanguage());
            return geminiResult;
        }

        return AiAutoDeckResponse.builder()
                .title(request.getTopic() != null ? "Bộ bài: " + request.getTopic() + " (Tạm thời không khả dụng)" : "Bộ bài mẫu")
                .description("Hệ thống AI tạo bộ bài đang quá tải hoặc gián đoạn kết nối. Vui lòng thử lại sau ít phút.")
                .sourceLanguage(request.getSourceLanguage() != null ? request.getSourceLanguage() : "en")
                .targetLanguage(request.getTargetLanguage() != null ? request.getTargetLanguage() : "vi")
                .cards(List.of())
                .build();
    }
}
