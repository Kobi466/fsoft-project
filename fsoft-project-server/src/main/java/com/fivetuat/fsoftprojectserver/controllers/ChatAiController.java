package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.services.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Chat AI Controller", description = "Công cụ nội bộ để test thử khả năng của mô hình AI Chat")
public class ChatAiController {
	ChatService chatService;
    
    @Operation(
        summary = "Test gửi tin nhắn trực tiếp đến LLM", 
        description = "Gửi một tin nhắn văn bản đơn giản đến hệ thống Chat AI (LLM) và nhận về phản hồi thuần túy dạng Text. Chỉ dùng cho mục đích debug và kiểm tra kết nối."
    )
	@GetMapping("/test")
	public String test(
        @Parameter(description = "Nội dung lời nhắn để test (Ví dụ: 'Xin chào, bạn là ai?')") @RequestParam(value = "message") String message
    ) {
		return chatService.chat(message);
	}
}
