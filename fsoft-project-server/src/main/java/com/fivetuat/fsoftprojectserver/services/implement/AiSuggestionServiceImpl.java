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

import com.fivetuat.fsoftprojectserver.dtos.response.CardSuggestionResponse;
import com.fivetuat.fsoftprojectserver.services.AiSuggestionService;
import com.fivetuat.fsoftprojectserver.utils.AiFallbackExecutor;
import com.fivetuat.fsoftprojectserver.utils.AiResponseUtils;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiSuggestionServiceImpl implements AiSuggestionService {

    ChatClient groqClient;
    AiFallbackExecutor aiFallbackExecutor;
    Resource suggestionSystemPromptResource;
    Resource suggestionUserPromptResource;

    public AiSuggestionServiceImpl(
            @Qualifier("groqClient") ChatClient groqClient,
            AiFallbackExecutor aiFallbackExecutor,
            @Value("classpath:/prompts/card-suggestion-system.st") Resource suggestionSystemPromptResource,
            @Value("classpath:/prompts/card-suggestion-user.st") Resource suggestionUserPromptResource) {
        this.groqClient = groqClient;
        this.aiFallbackExecutor = aiFallbackExecutor;
        this.suggestionSystemPromptResource = suggestionSystemPromptResource;
        this.suggestionUserPromptResource = suggestionUserPromptResource;
    }

    @Override
    @Retry(name = "aiRetry", fallbackMethod = "handleGetSuggestionsRetryFallback")
    public CardSuggestionResponse getSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return CardSuggestionResponse.builder()
                    .query("")
                    .termSuggestions(List.of())
                    .definitionSuggestions(List.of())
                    .build();
        }

        log.info("Bắt đầu sinh gợi ý từ vựng Autocomplete cho từ: '{}'", query);
        BeanOutputConverter<CardSuggestionResponse> converter = new BeanOutputConverter<>(CardSuggestionResponse.class);
        String format = converter.getFormat();

        SystemPromptTemplate systemTemplate = new SystemPromptTemplate(suggestionSystemPromptResource);
        String systemMessage = systemTemplate.render(Map.of("format", format));

        PromptTemplate userTemplate = new PromptTemplate(suggestionUserPromptResource);
        String userMessage = userTemplate.render(Map.of("query", query.trim()));

        try {
            String rawResponse = groqClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            CardSuggestionResponse result = converter.convert(cleanedResponse);

            if (result != null) {
                return result;
            }
            return CardSuggestionResponse.builder()
                    .query(query)
                    .termSuggestions(List.of(query))
                    .definitionSuggestions(List.of())
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Autocomplete Suggestion: {}", e.getMessage(), e);
            return CardSuggestionResponse.builder()
                    .query(query)
                    .termSuggestions(List.of(query))
                    .definitionSuggestions(List.of())
                    .build();
        }
    }

    public CardSuggestionResponse handleGetSuggestionsRetryFallback(String query, Throwable t) {
        CardSuggestionResponse result = aiFallbackExecutor.executeGeminiFallback(
                suggestionSystemPromptResource,
                null,
                suggestionUserPromptResource,
                Map.of("query", query != null ? query.trim() : ""),
                CardSuggestionResponse.class,
                t
        );

        if (result != null) {
            return result;
        }

        return CardSuggestionResponse.builder()
                .query(query != null ? query : "")
                .termSuggestions(query != null && !query.isBlank() ? List.of(query) : List.of())
                .definitionSuggestions(List.of())
                .build();
    }
}
