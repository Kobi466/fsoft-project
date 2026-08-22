package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardStarToggleResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.CardStarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card Star Controller", description = "Quản lý đánh dấu từ khó / từ yêu thích (Favorite / Starred Cards)")
public class CardStarController {

    CardStarService cardStarService;

    @Operation(
        summary = "Bật/Tắt đánh dấu từ khó (Toggle Star)",
        description = "Bấm nút ⭐ để lưu thẻ vào danh sách từ khó ôn riêng, hoặc bấm lại lần nữa để bỏ lưu. Trả về trạng thái isStarred mới (true/false)."
    )
    @PostMapping("/{cardId}/toggle-star")
    public ApiResponse<CardStarToggleResponse> toggleStar(
            @Parameter(description = "ID của thẻ ghi nhớ cần bật/tắt gắn sao") @PathVariable Long cardId
    ) {
        return ApiResponse.ok(cardStarService.toggleStar(cardId), SuccessCode.UPDATE_CARD_SUCCESS);
    }

    @Operation(
        summary = "Lấy danh sách các từ khó đã gắn sao",
        description = "Lấy danh sách toàn bộ các thẻ bài người dùng đã bấm ⭐ để phục vụ việc ôn tập riêng. Hỗ trợ lọc theo bộ thẻ (deckId) và phân trang."
    )
    @GetMapping("/starred")
    public ApiResponse<PageResponse<CardResponse>> getStarredCards(
            @Parameter(description = "Lọc danh sách từ khó theo bộ thẻ cụ thể (tuỳ chọn)") @RequestParam(value = "deckId", required = false) Long deckId,
            Pageable pageable
    ) {
        return ApiResponse.ok(cardStarService.getStarredCards(deckId, pageable), SuccessCode.GET_CARDS_SUCCESS);
    }
}
