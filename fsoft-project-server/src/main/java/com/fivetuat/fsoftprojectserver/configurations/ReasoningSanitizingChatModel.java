package com.fivetuat.fsoftprojectserver.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.HashMap;
import java.util.List;

@Slf4j
public class ReasoningSanitizingChatModel implements ChatModel {

    private static final String REASONING_METADATA_KEY = "reasoningContent";

    private final ChatModel delegate;

    public ReasoningSanitizingChatModel(ChatModel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return delegate.getDefaultOptions();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        ChatResponse response = delegate.call(prompt);
        return sanitizeResponse(response);
    }

    private ChatResponse sanitizeResponse(ChatResponse response) {
        if (response == null || response.getResults() == null) {
            return response;
        }
        boolean changed = false;
        List<Generation> generations = response.getResults();
        Generation[] sanitized = new Generation[generations.size()];
        for (int i = 0; i < generations.size(); i++) {
            sanitized[i] = generations.get(i);
        }
        for (int i = 0; i < sanitized.length; i++) {
            Generation generation = sanitized[i];
            if (generation == null || !(generation.getOutput() instanceof AssistantMessage assistant)) {
                continue;
            }
            if (assistant.getMetadata() != null && assistant.getMetadata().containsKey(REASONING_METADATA_KEY)) {
                var metadata = new HashMap<>(assistant.getMetadata());
                metadata.remove(REASONING_METADATA_KEY);
                AssistantMessage clean = AssistantMessage.builder()
                        .content(assistant.getText())
                        .properties(metadata)
                        .toolCalls(assistant.getToolCalls())
                        .media(assistant.getMedia())
                        .build();
                sanitized[i] = new Generation(clean, generation.getMetadata());
                changed = true;
            }
        }
        if (!changed) {
            return response;
        }
        log.info("[ADMIN_AI_DEBUG] stripped reasoningContent from response generation(s)");
        return new ChatResponse(List.of(sanitized), response.getMetadata());
    }
}