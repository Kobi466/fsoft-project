package com.fivetuat.fsoftprojectserver.controllers;


import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ReviewCardResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.ReviewCardService;
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
@RequestMapping("/srs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card SRS Controller", description = "Quản lý hệ thống học lặp lại ngắt quãng (Spaced Repetition System) cho thẻ học")
public class ReviewCardController {

    ReviewCardService srsService;

	@Operation(
        summary = "Lấy danh sách thẻ cần học/ôn tập", 
        description = "Truy xuất danh sách các thẻ bài đang nằm trong hàng đợi SRS (đã đến hạn ôn tập hoặc thẻ bài mới học). Có thể lọc theo một bộ thẻ cụ thể."
    )
    @GetMapping("/study-queue")
    public ApiResponse<List<ReviewCardResponse>> getStudyQueue(
		    @Parameter(description = "Lọc thẻ theo bộ thẻ (bỏ trống nếu muốn lấy tất cả)") @RequestParam(value = "deckId", required = false) Long deckId,
			@Parameter(description = "Số lượng thẻ tối đa trả về") @RequestParam(value = "limit", required = false, defaultValue = "20") int limit
    ) {
        return ApiResponse.ok(srsService.getStudyQueue(deckId, limit), SuccessCode.GET_CARDS_SUCCESS);
    }

	@Operation(
        summary = "Chấm điểm (Đánh giá) thẻ bài đã học", 
        description = "Sau khi người dùng học/ôn một thẻ, API này nhận vào độ khó mà người dùng đánh giá (AGAIN, HARD, GOOD, EASY). Hệ thống sẽ tự động tính toán thời điểm ôn tập tiếp theo (next_review) cho thẻ đó."
    )
	@PostMapping("/review")
	public ApiResponse<ReviewCardResponse> reviewCard(
        @Parameter(description = "Dữ liệu đánh giá (ID thẻ bài và mức độ đánh giá)") @RequestBody @Valid SrsReviewRequest request
    ) {
		return ApiResponse.ok(srsService.reviewCard(request), SuccessCode.REVIEW_CARD_SUCCESS);
	}

	@Operation(
        summary = "Khôi phục lại tiến trình học của một Bộ thẻ", 
        description = "Xóa toàn bộ lịch sử ôn tập (SRS log) của mọi thẻ bài trong bộ thẻ được chỉ định, đưa tất cả thẻ bài về trạng thái CHƯA HỌC (New)."
    )
	@DeleteMapping("/deck/{deckId}/reset")
	public ApiResponse<Void> resetDeckProgress(
        @Parameter(description = "ID của bộ thẻ cần reset") @PathVariable Long deckId
    ) {
		srsService.resetDeckProgress(deckId);
		return ApiResponse.ok(null, SuccessCode.DELETE_CARD_SUCCESS);
	}
}
