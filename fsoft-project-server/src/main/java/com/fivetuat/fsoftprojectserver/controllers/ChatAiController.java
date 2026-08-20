package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.services.ChatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatAiController {
	ChatService chatService;
	@GetMapping("/test")
	public String test(@RequestParam(value = "message") String message) {
		return chatService.chat(message);
	}
}
