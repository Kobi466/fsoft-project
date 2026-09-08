package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.VocabLookupRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.VocabLookupResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiIntegrationService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import com.fivetuat.fsoftprojectserver.annotations.RateLimit;
import com.fivetuat.fsoftprojectserver.enums.RateLimitKeyType;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Integration Controller", description = "Các API ứng dụng Trí tuệ nhân tạo (AI) giúp tối ưu hóa việc học tập của người dùng")
@RateLimit(limit = 30, period = 60, keyType = RateLimitKeyType.USER_OR_IP, name = "ai_integration")
public class AiIntegrationController {

    AiIntegrationService aiIntegrationService;

    @Operation(
        summary = "Tìm kiếm thẻ bài thông minh bằng AI", 
        description = "Sử dụng mô hình ngôn ngữ (LLM) để hiểu ngữ nghĩa của truy vấn người dùng, từ đó tìm và trả về các thẻ bài có liên quan nhất trong hệ thống, kể cả khi không khớp từ khóa hoàn toàn."
    )
    @GetMapping("/search")
    public ApiResponse<AiSearchResponse> search(
        @Parameter(description = "Câu hỏi hoặc từ khóa muốn tìm kiếm (ví dụ: 'Các thì trong tiếng anh')") @RequestParam String query
    ) {
        AiSearchResponse response = aiIntegrationService.searchCards(query);
        return ApiResponse.ok(response, SuccessCode.AI_SEARCH_SUCCESS);
    }

    @Operation(
        summary = "Chat với AI gia sư", 
        description = "Mở một phiên trò chuyện với AI gia sư. Người dùng có thể hỏi về các khái niệm khó hiểu, yêu cầu giải thích ngữ pháp, hoặc nhờ AI lấy ví dụ thực tế liên quan đến thẻ bài đang học."
    )
    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(
        @Parameter(description = "Nội dung tin nhắn và ngữ cảnh (tuỳ chọn) gửi cho AI") @RequestBody AiChatRequest request
    ) {
        AiChatResponse response = aiIntegrationService.chat(request);
        return ApiResponse.ok(response, SuccessCode.AI_CHAT_SUCCESS);
    }

    @Operation(
        summary = "Tự động tạo bộ câu hỏi Quiz (Kiểm tra)", 
        description = "AI sẽ phân tích nội dung của một hoặc nhiều thẻ bài (từ khóa, định nghĩa) sau đó tự động sinh ra các câu hỏi trắc nghiệm (bao gồm đáp án đúng, sai) để kiểm tra kiến thức của người dùng."
    )
    @PostMapping("/quiz")
    public ApiResponse<AiQuizResponse> generateQuiz(
        @Parameter(description = "Yêu cầu tạo quiz (bao gồm số lượng câu hỏi, danh sách thẻ bài cần tạo quiz...)") @RequestBody AiQuizRequest request
    ) {
        AiQuizResponse response = aiIntegrationService.generateQuiz(request);
        return ApiResponse.ok(response, SuccessCode.GENERATED_QUIZ_SUCCESS);
    }

    @Operation(
        summary = "Tra từ vựng bằng AI (Nội bộ / Dùng thử)",
        description = "Gửi từ vựng (và ngữ cảnh nếu có) tới AI Service để tra nghĩa, phát âm, ví dụ, cờ đã có trong bộ thẻ hay chưa, hoặc gợi ý sửa lỗi chính tả."
    )
    @PostMapping("/vocab/lookup")
    public ApiResponse<VocabLookupResponse> lookupVocab(
        @Parameter(description = "Thông tin từ vựng cần tra") @Valid @RequestBody VocabLookupRequest request
    ) {
        VocabLookupResponse response = aiIntegrationService.lookupVocab(request);
        return ApiResponse.ok(response, SuccessCode.AI_LOOKUP_SUCCESS);
    }
}
