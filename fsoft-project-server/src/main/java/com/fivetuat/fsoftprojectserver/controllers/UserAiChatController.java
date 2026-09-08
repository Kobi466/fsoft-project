package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.annotations.RateLimit;
import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.UserAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserAiChatResponse;
import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.UserAiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "User AI Chatbot Controller", description = "Trợ lý AI Gia sư cho học viên (hỏi đáp + tự động tạo/thêm bộ bài học tập)")
@RateLimit(limit = 30, period = 60, keyType = RateLimitKeyType.USER_OR_IP, name = "user_ai_chat")
public class UserAiChatController {

    UserAiChatService userAiChatService;

    @PostMapping("/chat")
    @Operation(
            summary = "Trò chuyện với Trợ lý AI gia sư học viên",
            description = "Gửi câu hỏi bằng ngôn ngữ tự nhiên. AI có thể tự động gọi tool tạo bộ bài, thêm từ vựng, xem bộ bài của học viên hiện tại. Không cần truyền conversationId, server tự quản lý theo ID học viên."
    )
    public ApiResponse<UserAiChatResponse> chat(@Valid @RequestBody UserAiChatRequest request) {
        UserAiChatResponse response = userAiChatService.chat(request);
        return ApiResponse.ok(response, SuccessCode.AI_CHAT_SUCCESS);
    }

    @GetMapping("/chat/history")
    @Operation(
            summary = "Lấy lịch sử hội thoại AI của học viên hiện tại",
            description = "Lấy danh sách lịch sử tin nhắn hội thoại trước đó của học viên hiện tại."
    )
    public ApiResponse<List<UserAiChatHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<UserAiChatHistoryResponse> history = userAiChatService.getHistory(page, size);
        return ApiResponse.ok(history, SuccessCode.AI_CHAT_SUCCESS);
    }

    @DeleteMapping("/chat/history")
    @Operation(
            summary = "Reset lịch sử hội thoại AI của học viên hiện tại",
            description = "Clear bộ nhớ chat memory của phiên trò chuyện học viên hiện tại."
    )
    public ApiResponse<Void> clearHistory() {
        userAiChatService.clearHistory();
        return ApiResponse.ok(null, SuccessCode.AI_CHAT_SUCCESS);
    }
}
