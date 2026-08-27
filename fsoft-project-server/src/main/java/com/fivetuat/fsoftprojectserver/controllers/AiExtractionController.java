package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.MagicSortResult;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.MagicSortRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.TextExtractionRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.UrlExtractionRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardSuggestionResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ai/extract")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "AI Extraction", description = "Các API dùng AI để bóc tách từ vựng từ đoạn văn, URL, file và Gợi ý Autocomplete thời gian thực")
public class AiExtractionController {

    AiExtractionService aiExtractionService;

    @Operation(summary = "Bóc tách từ vựng từ Text", description = "AI sẽ đọc một đoạn văn bản và tự động lấy ra danh sách các từ vựng/thuật ngữ quan trọng. Trả về JSON (Không tự động lưu vào Deck).")
    @PostMapping("/text")
    public ApiResponse<List<CardCreationRequest>> extractFromText(@RequestBody TextExtractionRequest request) {
        List<CardCreationRequest> result = aiExtractionService.extractedText(request.text(), request.category());
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS);
    }

    @Operation(summary = "Bóc tách từ vựng từ URL", description = "Truyền vào một đường link bài báo/blog. Hệ thống sẽ cào nội dung text và nhờ AI bóc tách ra các từ vựng quan trọng.")
    @PostMapping("/url")
    public ApiResponse<List<CardCreationRequest>> extractFromUrl(@RequestBody UrlExtractionRequest request) {
        List<CardCreationRequest> result = aiExtractionService.extractedUrl(request.url(), request.category());
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS);
    }

    @Operation(summary = "Bóc tách từ vựng từ File (PDF/Ảnh)", description = "Upload file PDF hoặc Hình ảnh (Gemini Vision). AI sẽ phân tích nội dung file và trích xuất từ vựng.")
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<CardCreationRequest>> extractFromFile(
            @Parameter(description = "File PDF hoặc Hình ảnh", required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "Chủ đề gợi ý (vd: Science, Daily Life)")
            @RequestParam("category") String category) {
        List<CardCreationRequest> result = aiExtractionService.extractedFile(file, category);
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS);
    }

    @Operation(summary = "Magic Sort (Phân loại thông minh)", description = "Gửi danh sách từ vựng và danh sách Decks của user. AI sẽ tự động phân từng từ vào Deck phù hợp, hoặc gợi ý tên Deck mới nếu từ đó không thuộc Deck nào.")
    @PostMapping("/magic-sort")
    public ApiResponse<List<MagicSortResult>> magicSortCards(@RequestBody MagicSortRequest request) {
        List<MagicSortResult> result = aiExtractionService.magicSort(request);
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS);
    }

    @Operation(
        summary = "⚡ Real-time Card Autocomplete & Definition Suggestions (Giống Quizlet)", 
        description = "Khi người dùng vừa gõ từ vựng vào ô TERM (ví dụ: 'meet'), AI sẽ tự động sinh danh sách gợi ý hoàn thiện từ vựng (meet, meeting, meet, met, met) và các gợi ý nghĩa Tiếng Việt (gặp, đáp ứng, gặp gỡ) cùng thông tin auto-fill đầy đủ."
    )
    @GetMapping("/suggestions")
    public ApiResponse<CardSuggestionResponse> getSuggestions(
            @Parameter(description = "Từ vựng hoặc chuỗi ký tự người dùng đang gõ", example = "meet")
            @RequestParam("query") String query
    ) {
        CardSuggestionResponse result = aiExtractionService.getSuggestions(query);
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS);
    }
}
