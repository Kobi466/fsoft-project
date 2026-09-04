package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AiRoleplayRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CreateAiRoleplaySessionRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplayResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AiRoleplaySessionResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiRoleplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/roleplay")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Roleplay Controller", description = "Gia sư AI đóng vai đối thoại tương tác ép dùng từ vựng vừa học")
public class AiRoleplayController {

    AiRoleplayService aiRoleplayService;

    @PostMapping
    @Operation(summary = "Đóng vai đối thoại theo kịch bản (Phỏng vấn, Khám bệnh, Khách sạn) và chấm điểm từ vựng")
    public ApiResponse<AiRoleplayResponse> processRoleplay(@Valid @RequestBody AiRoleplayRequest request) {
        return ApiResponse.ok(aiRoleplayService.processRoleplay(request), SuccessCode.AI_CHAT_SUCCESS);
    }

    @GetMapping("/history")
    @Operation(summary = "Lấy toàn bộ lịch sử tin nhắn của phiên nhập vai đối thoại")
    public ApiResponse<List<AiRoleplayHistoryResponse>> getRoleplayHistory(
            @Parameter(description = "ID phiên hội thoại (Nếu bỏ trống sẽ lấy theo User ID hiện tại)")
            @RequestParam(value = "conversationId", required = false) String conversationId
    ) {
        return ApiResponse.ok(aiRoleplayService.getRoleplayHistory(conversationId), SuccessCode.AI_CHAT_SUCCESS);
    }

    @DeleteMapping("/{conversationId}")
    @Operation(summary = "Xóa/Reset bộ nhớ của phiên đối thoại nhập vai để bắt đầu lại từ đầu")
    public ApiResponse<Void> clearRoleplayHistory(
            @Parameter(description = "ID phiên hội thoại cần xóa") @PathVariable String conversationId
    ) {
        aiRoleplayService.clearRoleplayHistory(conversationId);
        return ApiResponse.ok(null, SuccessCode.AI_CHAT_SUCCESS);
    }

    @GetMapping("/sessions")
    @Operation(summary = "Lấy danh sách tất cả các phiên hội thoại AI Roleplay của người dùng")
    public ApiResponse<List<AiRoleplaySessionResponse>> getUserSessions() {
        return ApiResponse.ok(aiRoleplayService.getUserSessions(), SuccessCode.AI_CHAT_SUCCESS);
    }

    @PostMapping("/sessions")
    @Operation(summary = "Tạo một phiên hội thoại AI Roleplay mới")
    public ApiResponse<AiRoleplaySessionResponse> createSession(@Valid @RequestBody CreateAiRoleplaySessionRequest request) {
        return ApiResponse.ok(aiRoleplayService.createSession(request), SuccessCode.AI_CHAT_SUCCESS);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "Xóa một phiên hội thoại AI Roleplay")
    public ApiResponse<Void> deleteSession(@PathVariable String sessionId) {
        aiRoleplayService.deleteSession(sessionId);
        return ApiResponse.ok(null, SuccessCode.AI_CHAT_SUCCESS);
    }
}
