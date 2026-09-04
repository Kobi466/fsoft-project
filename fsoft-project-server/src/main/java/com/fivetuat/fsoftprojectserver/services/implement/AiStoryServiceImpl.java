package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.AiStoryRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiStoryResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.services.AiStoryService;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiStoryServiceImpl implements AiStoryService {

    ChatClient groqClient;
    CardRepository cardRepository;
    Resource systemPromptResource;
    Resource userPromptResource;

    public AiStoryServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            CardRepository cardRepository,
            @Value("classpath:/prompts/story-generator-system.st") Resource systemPromptResource,
            @Value("classpath:/prompts/story-generator-user.st") Resource userPromptResource) {
        this.groqClient = groqClient;
        this.cardRepository = cardRepository;
        this.systemPromptResource = systemPromptResource;
        this.userPromptResource = userPromptResource;
    }

    @Override
    @Transactional(readOnly = true)
    public AiStoryResponse generateStory(AiStoryRequest request) {
        List<String> words = new ArrayList<>();
        if (request.getWords() != null && !request.getWords().isEmpty()) {
            words.addAll(request.getWords());
        }

        if (request.getCardIds() != null && !request.getCardIds().isEmpty()) {
            List<Card> cards = cardRepository.findAllById(request.getCardIds());
            List<String> cardWords = cards.stream().map(Card::getWord).collect(Collectors.toList());
            words.addAll(cardWords);
        }

        if (words.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        log.info("Bắt đầu sinh đoạn văn AI từ {} từ vựng theo ngữ cảnh {}", words.size(), request.getContextType());

        BeanOutputConverter<AiStoryResponse> converter = new BeanOutputConverter<>(AiStoryResponse.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(systemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(userPromptResource);
        String userMessage = userTemplate.render(Map.of(
                "contextType", request.getContextType() != null ? request.getContextType() : "BUSINESS_EMAIL",
                "words", String.join(", ", words)
        ));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            AiStoryResponse result = converter.convert(cleanedResponse);

            if (result == null) {
                throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
            }

            return result;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Story Generator: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_EXTRACTION_ERROR);
        }
    }
}
