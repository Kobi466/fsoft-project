package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardAiResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.InternalCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.time.Instant;

@RestController
@RequestMapping("/internal/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@SecurityRequirement(name = "internalToken")
@Tag(name = "Internal Card Controller", description = "Các API dùng riêng cho hệ thống phân tán nội bộ để đồng bộ và phân tích dữ liệu")
public class InternalCardController {

    InternalCardService internalCardService;

    @Operation(
        summary = "Trích xuất danh sách thẻ bài đã có thay đổi", 
        description = "Lấy ra các thẻ bài (Flashcard) được tạo, cập nhật hoặc bị sửa đổi kể từ thời điểm (since) được cung cấp. Phục vụ cho Data Pipeline đồng bộ dữ liệu vào Vector Database hoặc AI Indexing."
    )
    @GetMapping("/changed-since")
    public ApiResponse<PageResponse<CardAiResponse>> getChangedCards(
            @Parameter(description = "Mốc thời gian (Chuẩn ISO-8601). Nếu bỏ trống sẽ lấy từ đầu (1970).") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @Parameter(description = "Trang cần lấy dữ liệu") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Kích thước mỗi trang (Mặc định 200)") @RequestParam(defaultValue = "200") int size
    ) {
        if (since == null) {
            since = Instant.EPOCH; // 1970-01-01T00:00:00Z
        }
        return ApiResponse.ok(internalCardService.getChangedCards(since, page, size), SuccessCode.CARD_CHANGED_SINCE_SUCCESS);
    }

    @Operation(
        summary = "Lấy toàn bộ ID của các thẻ bài trong hệ thống", 
        description = "Trả về một mảng chứa ID của tất cả các thẻ bài hiện có. Thường được các Worker chạy định kỳ gọi để kiểm tra và xóa các bản ghi bị lệch pha."
    )
    @GetMapping("/ids")
    public ApiResponse<PageResponse<Long>> getCardIds(
            @Parameter(description = "Trang dữ liệu (Page)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Số lượng ID trả về trong một lượt (Max 5000)") @RequestParam(defaultValue = "5000") int size
    ) {
        return ApiResponse.ok(internalCardService.getCardIds(page, size), SuccessCode.CARD_IDS_SUCCESS);
    }
}
