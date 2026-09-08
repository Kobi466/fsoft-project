package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AiAutoDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiAutoDeckResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiAutoDeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.annotations.RateLimit;
import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;

@RestController
@RequestMapping("/api/ai/auto-deck")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Auto Deck Controller", description = "Tự động sinh bộ bài Flashcard hoàn chỉnh từ chủ đề bằng AI")
@RateLimit(limit = 10, period = 60, keyType = RateLimitKeyType.USER_OR_IP, name = "ai_auto_deck")
public class AiAutoDeckController {

    AiAutoDeckService aiAutoDeckService;

    @PostMapping
    @Operation(summary = "Tự động sinh nguyên bộ bài Flashcard chuẩn trong 3 giây từ 1 chủ đề bất kỳ")
    public ApiResponse<AiAutoDeckResponse> generateAutoDeck(@Valid @RequestBody AiAutoDeckRequest request) {
        return ApiResponse.ok(aiAutoDeckService.generateAutoDeck(request), SuccessCode.CREATE_DECK_SUCCESS);
    }
}
