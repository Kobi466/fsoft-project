package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminAiConfirmRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiAuditLogResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatHistoryResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminAiChatResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AdminAiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin AI Chat Controller", description = "Trợ lý AI cho quản trị viên (tra cứu + hành động)")
public class AdminAiChatController {

    AdminAiChatService adminAiChatService;

    @PostMapping("/chat")
    @Operation(summary = "Hỏi trợ lý AI quản trị",
            description = "Admin hỏi bằng ngôn ngữ tự nhiên. Bot trả lời dựa trên dữ liệu thật của hệ thống. "
                    + "Khi admin yêu cầu hành động nguy hiểm (duyệt/từ chối/xóa deck, đặt official, đổi roles), "
                    + "response trả về pendingAction chứa actionId — admin phải gọi /confirm để thực thi thật hoặc /confirm/cancel để hủy.")
    public ApiResponse<AdminAiChatResponse> chat(@Valid @RequestBody AdminAiChatRequest request) {
        return ApiResponse.ok(adminAiChatService.chat(request), SuccessCode.ADMIN_AI_CHAT_SUCCESS);
    }

    @PostMapping("/chat/confirm")
    @Operation(summary = "Xác nhận hành động AI đã soạn (thực thi thật)",
            description = "Thực thi hành động đã soạn. Lỗi 462 nếu actionId không tồn tại hoặc đã xử lý, "
                    + "lỗi 463 nếu đã hết hạn 5 phút. Chỉ admin tạo hành động mới xác nhận được. "
                    + "Mỗi actionId chỉ dùng được một lần. Mọi lần thực thi đều được ghi vào audit log.")
    public ApiResponse<AdminAiChatResponse> confirm(@Valid @RequestBody AdminAiConfirmRequest request) {
        return ApiResponse.ok(adminAiChatService.confirm(request.getActionId()), SuccessCode.ADMIN_AI_ACTION_CONFIRMED);
    }

    @PostMapping("/chat/confirm/cancel")
    @Operation(summary = "Hủy hành động AI đã soạn",
            description = "Hủy hành động chờ xác nhận và ghi audit log với trạng thái CANCELLED.")
    public ApiResponse<AdminAiChatResponse> cancel(@Valid @RequestBody AdminAiConfirmRequest request) {
        return ApiResponse.ok(adminAiChatService.cancel(request.getActionId()), SuccessCode.ADMIN_AI_ACTION_CANCELLED);
    }

    @GetMapping("/chat/history")
    @Operation(summary = "Lấy lịch sử hội thoại AI của admin hiện tại",
            description = "Trả về các tin nhắn (role: user/assistant/system) trong thread riêng của admin, bền vững qua JDBC chat memory.")
    public ApiResponse<List<AdminAiChatHistoryResponse>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminAiChatService.getHistory(page, size), SuccessCode.ADMIN_AI_CHAT_SUCCESS);
    }

    @DeleteMapping("/chat/history")
    @Operation(summary = "Xóa lịch sử hội thoại AI của admin hiện tại",
            description = "Reset thread hội thoại của admin hiện tại (chatMemory.clear).")
    public ApiResponse<Void> clearHistory() {
        adminAiChatService.clearHistory();
        return ApiResponse.ok(null, SuccessCode.ADMIN_AI_CHAT_SUCCESS);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Lấy nhật ký các hành động AI đã thực hiện",
            description = "Danh sách hành động AI (EXECUTED/CANCELLED/FAILED) của admin hiện tại, mới nhất trước.")
    public ApiResponse<PageResponse<AdminAiAuditLogResponse>> getAuditLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminAiChatService.getAuditLogs(page, size), SuccessCode.GET_AUDIT_LOGS_SUCCESS);
    }
}