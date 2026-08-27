package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.SituationalLearningRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.SituationalLearningResponse;
import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.SituationalLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/situational-learning")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Situational Learning", description = "Các API tạo bài học tình huống bằng AI")
public class SituationalLearningController {

    SituationalLearningService situationalLearningService;

    @Operation(summary = "Tạo bài học qua Text", description = "Dựa trên mô tả tình huống của người dùng (bằng văn bản), AI sẽ tạo ra một bộ câu giao tiếp song ngữ Anh-Việt và danh sách từ vựng liên quan theo văn phong thực tế.")
    @PostMapping("/text")
    public ApiResponse<SituationalLearningResponse> learnFromText(@RequestBody @Valid SituationalLearningRequest request) {
        SituationalLearningResponse response = situationalLearningService.generateLesson(request);
        return ApiResponse.ok(response, SuccessCode.AI_SITUATIONAL_LEARNING_SUCCESS);
    }
}
