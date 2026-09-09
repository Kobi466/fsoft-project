package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.advisors.AvailableToolsLoggingAdvisor;
import com.fivetuat.fsoftprojectserver.advisors.TokenCounterAdvisor;
import com.fivetuat.fsoftprojectserver.dtos.request.UserAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.services.UserAiChatService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserAiChatServiceImpl implements UserAiChatService {

    ChatClient userAiChatClient;
    ChatMemory chatMemory;

    public UserAiChatServiceImpl(
            @Qualifier("userAiChatClient") ChatClient userAiChatClient,
            ChatMemory chatMemory) {
        this.userAiChatClient = userAiChatClient;
        this.chatMemory = chatMemory;
    }

    @Override
    public UserAiChatResponse chat(UserAiChatRequest request) {
        UUID userId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));
        String conversationId = userId.toString();

        try {
            String reply = userAiChatClient.prompt()
                    .advisors(
                            MessageChatMemoryAdvisor.builder(chatMemory).build(),
                            new AvailableToolsLoggingAdvisor(),
                            new TokenCounterAdvisor()
                    )
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(request.getMessage())
                    .call()
                    .content();

            return UserAiChatResponse.builder()
                    .conversationId(conversationId)
                    .reply(reply)
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Lỗi khi xử lý User AI Chat: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    @Override
    public List<UserAiChatHistoryResponse> getHistory(int page, int size) {
        UUID userId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));
        String conversationId = userId.toString();

        List<Message> messages = chatMemory.get(conversationId);
        int to = Math.min(messages.size(), Math.max(0, messages.size() - (page - 1) * size));
        int from = Math.max(0, to - size);
        if (to <= from) {
            return List.of();
        }
        return messages.subList(from, to).stream()
                .map(m -> UserAiChatHistoryResponse.builder()
                        .role(m.getMessageType().getValue())
                        .content(m.getText())
                        .build())
                .toList();
    }

    @Override
    public void clearHistory() {
        UUID userId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));
        chatMemory.clear(userId.toString());
    }
}
