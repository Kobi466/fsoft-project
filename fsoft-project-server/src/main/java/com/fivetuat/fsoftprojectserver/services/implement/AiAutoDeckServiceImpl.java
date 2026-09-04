package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.AiAutoDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiAutoDeckResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.AiAutoDeckService;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiAutoDeckServiceImpl implements AiAutoDeckService {

    ChatClient groqClient;
    Resource systemPromptResource;
    Resource userPromptResource;

    public AiAutoDeckServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            @Value("classpath:/prompts/auto-deck-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/auto-deck-user.st") Resource userPromptResource) {
        this.groqClient = groqClient;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
    }

    @Override
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
            log.error("Lỗi khi gọi AI Auto Deck Generator: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }
}
