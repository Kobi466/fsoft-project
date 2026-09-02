package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminGameRecordResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AdminGameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/games")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Game Controller", description = "Quản lý Lịch sử Game & Điểm số (Admin)")
public class AdminGameController {

    AdminGameService adminGameService;

    @GetMapping("/records")
    @Operation(summary = "Lấy danh sách tất cả các ván game đã diễn ra (Space Striker & Audio Reflex)")
    public ApiResponse<PageResponse<AdminGameRecordResponse>> getAllGameRecords(
            @RequestParam(required = false) String gameType,
            @RequestParam(required = false) Long deckId,
            Pageable pageable
    ) {
        return ApiResponse.ok(adminGameService.getAllGameRecords(gameType, deckId, pageable), SuccessCode.GET_LEADERBOARD_SUCCESS);
    }

    @DeleteMapping("/records/{recordId}")
    @Operation(summary = "Xóa kỷ lục điểm số gian lận khỏi Bảng Xếp Hạng")
    public ApiResponse<Void> deleteGameRecord(@PathVariable String recordId) {
        adminGameService.deleteGameRecord(recordId);
        return ApiResponse.ok(null, SuccessCode.DELETE_POST_SUCCESS);
    }
}
