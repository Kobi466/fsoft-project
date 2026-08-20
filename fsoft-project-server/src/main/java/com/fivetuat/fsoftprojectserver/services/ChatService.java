package com.fivetuat.fsoftprojectserver.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class ChatService {
	private final ChatClient chatClient;
	public ChatService(ChatClient.Builder chatClient) {
		this.chatClient = chatClient.build();
	}
public String chat(String message) {
		return chatClient.prompt(message).call().content();
	}
}
