package com.fivetuat.fsoftprojectserver.controllers;

import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ChatMessageRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;
import com.fivetuat.fsoftprojectserver.services.ChatMessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Community Chat Controller")
public class ChatController {

    ChatMessageService chatMessageService;

    @GetMapping("/messages")
    @Operation(summary = "Lấy lịch sử tin nhắn cộng đồng phân trang")
    public ResponseEntity<ApiResponse<PageResponse<ChatMessageResponse>>> getMessages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ChatMessageResponse>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy lịch sử tin nhắn thành công")
                        .data(chatMessageService.getMessageHistory(pageable))
                        .build()
        );
    }

    @PostMapping("/messages")
    @Operation(summary = "Gửi tin nhắn qua REST API")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> postMessage(
            @RequestBody @Valid ChatMessageRequest request
    ) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<ChatMessageResponse>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Gửi tin nhắn thành công")
                        .data(chatMessageService.saveAndBroadcastMessage(request, currentUserId))
                        .build()
        );
    }

    @GetMapping("/online-count")
    @Operation(summary = "Lấy số lượng người dùng đang online")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getOnlineCount() {
        return ResponseEntity.ok(
                ApiResponse.<Map<String, Integer>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Lấy số người online thành công")
                        .data(Map.of("onlineUsers", chatMessageService.getOnlineUserCount()))
                        .build()
        );
    }
}
