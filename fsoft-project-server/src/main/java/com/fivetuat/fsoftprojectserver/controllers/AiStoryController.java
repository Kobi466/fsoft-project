package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AiStoryRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AiStoryResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiStoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/story")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Story Controller", description = "Tạo đoạn văn/email ngữ cảnh thực tế từ các từ vựng đang học")
public class AiStoryController {

    AiStoryService aiStoryService;

    @PostMapping
    @Operation(summary = "Tự động lồng ghép các từ vựng cần ôn thành đoạn văn/email thực tế bằng AI")
    public ApiResponse<AiStoryResponse> generateStory(@Valid @RequestBody AiStoryRequest request) {
        return ApiResponse.ok(aiStoryService.generateStory(request), SuccessCode.AI_CHAT_SUCCESS);
    }
}
