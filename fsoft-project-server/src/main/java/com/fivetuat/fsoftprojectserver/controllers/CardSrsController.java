package com.fivetuat.fsoftprojectserver.controllers;


import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.SrsCardResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.SrsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/srs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card SRS Controller", description = "Quản lý hệ thống SRS cho thẻ học")
public class CardSrsController {

    SrsService srsService;

	@Operation(summary = "Lấy danh sách thẻ học trong hàng đợi SRS", description = "Lấy danh sách thẻ học trong hàng đợi SRS, có thể lọc theo deckId và giới hạn số lượng thẻ trả về")
    @GetMapping("/study-queue")
    public ApiResponse<List<SrsCardResponse>> getStudyQueue(
		    @RequestParam(value = "deckId", required = false) Long deckId,
			@RequestParam(value = "limit", required = false, defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(srsService.getStudyQueue(deckId, limit), SuccessCode.GET_CARDS_SUCCESS);
    }

	@Operation(summary = "Đánh giá thẻ học", description = "Đánh giá thẻ học sau khi học xong, cập nhật trạng thái SRS của thẻ học")
	@PostMapping("/review")
	public ApiResponse<SrsCardResponse> reviewCard(@RequestBody @Valid SrsReviewRequest request) {
		return ApiResponse.ok(srsService.reviewCard(request), SuccessCode.REVIEW_CARD_SUCCESS);
	}

	@Operation(summary = "Làm mới tiến độ học", description = "Xóa toàn bộ lịch sử ôn tập của một bộ thẻ để học lại từ đầu")
	@DeleteMapping("/deck/{deckId}/reset")
	public ApiResponse<Void> resetDeckProgress(@PathVariable Long deckId) {
		srsService.resetDeckProgress(deckId);
		return ApiResponse.ok(null, SuccessCode.DELETE_CARD_SUCCESS);
	}
}
