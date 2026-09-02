package com.fivetuat.fsoftprojectserver.controllers;


import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.SrsReviewRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ReviewCardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.SrsMasteryResponse;
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
			summary = "Lấy danh sách thẻ cần học/ôn tập theo cụm thời gian FSRS",
			description = "Truy xuất danh sách toàn bộ thẻ bài trong cụm thời gian FSRS gần nhất của người dùng. Có thể lọc theo 1 bộ thẻ cụ thể hoặc tất cả bộ thẻ."
	)
	@GetMapping("/study-queue")
	public ApiResponse<List<ReviewCardResponse>> getStudyQueue(
			@Parameter(description = "Lọc thẻ theo bộ thẻ (bỏ trống nếu muốn lấy tất cả bộ bài)") @RequestParam(value = "deckId", required = false) Long deckId,
			@Parameter(description = "Khoảng thời gian tối đa cho phép xem trước cụm tương lai (phút)") @RequestParam(value = "earlyBufferMinutes", required = false, defaultValue = "120") Integer earlyBufferMinutes,
			@Parameter(description = "Phạm vi cửa sổ thời gian để gom cụm thẻ bài (phút)") @RequestParam(value = "clusterMinutes", required = false, defaultValue = "30") Integer clusterMinutes
	) {
		return ApiResponse.ok(srsService.getFsrsStudyQueue(deckId, earlyBufferMinutes, clusterMinutes), SuccessCode.GET_CARDS_SUCCESS);
	}

	@Operation(
        summary = "Lấy danh sách thẻ đến hạn ôn tập của một Bộ thẻ theo cụm FSRS", 
        description = "Truy xuất danh sách các thẻ bài đến hạn hoặc sắp đến hạn ôn tập của một Bộ thẻ cụ thể theo cụm thời gian FSRS."
    )
    @GetMapping("/deck/{deckId}/due")
    public ApiResponse<List<ReviewCardResponse>> getDueCardsByDeckId(
            @Parameter(description = "ID của bộ thẻ cần lấy thẻ đến hạn") @PathVariable Long deckId,
            @Parameter(description = "Khoảng thời gian tối đa cho phép xem trước cụm tương lai (phút)") @RequestParam(value = "earlyBufferMinutes", required = false, defaultValue = "120") Integer earlyBufferMinutes,
            @Parameter(description = "Phạm vi cửa sổ thời gian để gom cụm thẻ bài (phút)") @RequestParam(value = "clusterMinutes", required = false, defaultValue = "30") Integer clusterMinutes
    ) {
        return ApiResponse.ok(srsService.getFsrsStudyQueue(deckId, earlyBufferMinutes, clusterMinutes), SuccessCode.GET_CARDS_SUCCESS);
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

	@Operation(
			summary = "Biểu đồ thống kê tiến độ thuộc từ (SRS Mastery Dashboard)",
			description = """
						Trả về thống kê tổng quan tiến độ học SRS của học viên:
						- Tổng thẻ đã học, thẻ thuộc lòng (interval >= 30), đang học, hay quên.
						- Tỷ lệ % thuộc lòng (masteryRate).
						- Số thẻ cần ôn hôm nay (bao gồm thẻ quá hạn từ hôm trước).
						- Số thẻ chưa bắt đầu học (chỉ khi truyền deckId).
						- Dự báo số thẻ scheduled trong 7 ngày tới.
					"""
	)
	@GetMapping("/mastery")
	public ApiResponse<SrsMasteryResponse> getMasteryDashboard(
			@Parameter(description = "ID bộ thẻ (bỏ trống để xem thống kê toàn bộ)")
			@RequestParam(value = "deckId", required = false) Long deckId
	) {
		return ApiResponse.ok(srsService.getMasteryDashboard(deckId), SuccessCode.GET_SRS_MASTERY_SUCCESS);
	}
}


