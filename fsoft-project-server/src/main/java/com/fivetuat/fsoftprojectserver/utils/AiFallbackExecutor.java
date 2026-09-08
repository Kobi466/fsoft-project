package com.fivetuat.fsoftprojectserver.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AiFallbackExecutor {

    private final ChatClient geminiClient;

    public AiFallbackExecutor(@Qualifier("geminiClient") ChatClient geminiClient) {
        this.geminiClient = geminiClient;
    }

   
    public <T> T executeGeminiFallback(String systemMessage, String userMessage, Class<T> targetClass, Throwable t) {
        log.warn("Groq AI gặp sự cố ({}), tự động chuyển sang Gemini làm Secondary Provider...", t != null ? t.getMessage() : "N/A");
        try {
            BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
            String rawResponse = geminiClient.prompt()
                    .system(systemMessage)
                    .user(userMessage)
                    .call()
                    .content();

            String cleanedResponse = AiResponseUtils.sanitizeJsonResponse(rawResponse);
            T result = converter.convert(cleanedResponse);
            if (result != null) {
                log.info("Chuyển đổi thành công sang Gemini({})!", targetClass.getSimpleName());
                return result;
            }
        } catch (Exception e) {
            log.error("Cả Groq và Gemini đều gặp sự cố khi xử lý {}: {}", targetClass.getSimpleName(), e.getMessage());
        }
        return null;
    }

    
    public <T> T executeGeminiFallback(
            Resource systemPromptResource,
            Map<String, Object> systemParams,
            Resource userPromptResource,
            Map<String, Object> userParams,
            Class<T> targetClass,
            Throwable t
    ) {
        try {
            BeanOutputConverter<T> converter = new BeanOutputConverter<>(targetClass);
            String format = converter.getFormat();

            Map<String, Object> sysMap = new HashMap<>(systemParams != null ? systemParams : Map.of());
            sysMap.put("format", format);

            String systemMessage = new SystemPromptTemplate(systemPromptResource).render(sysMap);
            String userMessage = new PromptTemplate(userPromptResource).render(userParams != null ? userParams : Map.of());

            return executeGeminiFallback(systemMessage, userMessage, targetClass, t);
        } catch (Exception e) {
            log.error("Lỗi khi render prompt cho Gemini Fallback ({}): {}", targetClass.getSimpleName(), e.getMessage());
            return null;
        }
    }
}
