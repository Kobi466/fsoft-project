package com.fivetuat.fsoftprojectserver.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;

import java.util.ArrayList;
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
        List<Message> instructions = prompt.getInstructions();
        boolean changed = false;
        List<Message> sanitized = new ArrayList<>(instructions.size());
        for (Message message : instructions) {
            Message stripped = stripReasoningMetadata(message);
            if (stripped != message) {
                changed = true;
            }
            sanitized.add(stripped);
        }
        if (!changed) {
            return delegate.call(prompt);
        }
        log.info("[ADMIN_AI_DEBUG] stripped reasoningContent, resending {} sanitized message(s)", sanitized.size());
        return delegate.call(new Prompt(sanitized, prompt.getOptions()));
    }

    private Message stripReasoningMetadata(Message message) {
        if (message instanceof AssistantMessage assistant
                && assistant.getMetadata() != null
                && assistant.getMetadata().containsKey(REASONING_METADATA_KEY)) {
            var metadata = new java.util.HashMap<>(assistant.getMetadata());
            metadata.remove(REASONING_METADATA_KEY);
            return AssistantMessage.builder()
                    .content(assistant.getText())
                    .properties(metadata)
                    .toolCalls(assistant.getToolCalls())
                    .media(assistant.getMedia())
                    .build();
        }
        return message;
    }
}