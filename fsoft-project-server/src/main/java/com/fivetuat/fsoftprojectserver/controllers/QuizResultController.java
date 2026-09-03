package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.QuizGenerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizAnalyticsResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizGenerateResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.QuizSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizResultResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.LocalQuizService;
import com.fivetuat.fsoftprojectserver.services.QuizResultService;
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
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Quiz Result Controller", description = "Các API nộp bài kiểm tra Quiz, tạo bài Quiz nội bộ và xem lịch sử thi")
public class QuizResultController {

    QuizResultService quizResultService;
    LocalQuizService localQuizService;

    @PostMapping("/generate-local")
    @Operation(
        summary = "Khởi tạo bài thi Quiz nội bộ (Local Quiz Generator)",
        description = "Tự động lấy thẻ bài từ Database và sinh các câu hỏi trắc nghiệm A, B, C, D nội bộ. Hệ thống sẽ kiểm tra đảm bảo số lượng câu hỏi không vượt quá tổng số thẻ có sẵn."
    )
    public ApiResponse<QuizGenerateResponse> generateLocalQuiz(@RequestBody QuizGenerateRequest request) {
        QuizGenerateResponse response = localQuizService.generateLocalQuiz(request);
        return ApiResponse.ok(response, SuccessCode.GENERATED_QUIZ_SUCCESS);
    }

    @PostMapping("/submit")
    @Operation(
        summary = "Nộp bài kiểm tra Quiz",
        description = "Gửi kết quả bài Quiz từ Frontend. Hệ thống sẽ lưu điểm số và tự động cập nhật Streak/Activity cho học viên."
    )
    public ApiResponse<QuizResultResponse> submitQuiz(@RequestBody @Valid QuizSubmitRequest request) {
        QuizResultResponse response = quizResultService.submitQuiz(request);
        return ApiResponse.ok(response, SuccessCode.SUBMIT_QUIZ_SUCCESS);
    }

    @GetMapping("/history")
    @Operation(
        summary = "Lấy lịch sử thi Quiz của tôi",
        description = "Trả về danh sách tất cả các lần làm bài Quiz của người dùng hiện tại, sắp xếp từ mới nhất đến cũ nhất."
    )
    public ApiResponse<List<QuizResultResponse>> getMyQuizHistory() {
        List<QuizResultResponse> response = quizResultService.getMyQuizHistory();
        return ApiResponse.ok(response, SuccessCode.GET_QUIZ_HISTORY_SUCCESS);
    }

    @GetMapping("/analytics")
    @Operation(
        summary = "Báo cáo thống kê phong độ thi Quiz của tôi",
        description = "Trả về tổng quan phong độ: Tổng lượt thi, Độ chính xác trung bình, Tổng XP, Tổng thời gian thi, và 7 lượt làm gần nhất."
    )
    public ApiResponse<QuizAnalyticsResponse> getMyQuizAnalytics() {
        QuizAnalyticsResponse response = quizResultService.getMyQuizAnalytics();
        return ApiResponse.ok(response, SuccessCode.GET_QUIZ_HISTORY_SUCCESS);
    }

    @GetMapping("/result/{id}")
    @Operation(
        summary = "Xem chi tiết kết quả 1 bài thi Quiz",
        description = "Lấy thông tin kết quả 1 bài thi Quiz theo ID."
    )
    public ApiResponse<QuizResultResponse> getQuizResultById(
            @Parameter(description = "ID của kết quả bài Quiz") @PathVariable Long id
    ) {
        QuizResultResponse response = quizResultService.getQuizResultById(id);
        return ApiResponse.ok(response, SuccessCode.GET_QUIZ_RESULT_SUCCESS);
    }
}
