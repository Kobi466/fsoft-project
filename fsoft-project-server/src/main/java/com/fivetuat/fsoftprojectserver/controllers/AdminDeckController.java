package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateDeckOfficialRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;

@RestController
@RequestMapping("/admin/decks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Deck Controller", description = "Quản lý & Kiểm duyệt Bộ bài (Admin)")
public class AdminDeckController {

    AdminDeckService adminDeckService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo mới Bộ bài chuẩn Hệ thống (Official System Deck)")
    public ApiResponse<DeckResponse> createOfficialDeck(
            @RequestPart("request") @Valid DeckCreateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage
    ) {
        return ApiResponse.ok(adminDeckService.createOfficialDeck(request, coverImage), SuccessCode.CREATE_DECK_SUCCESS);
    }


    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả bộ bài trong CSDL (Có phân trang & lọc)")
    public ApiResponse<PageResponse<DeckResponse>> getAllDecks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOfficial,
            @RequestParam(required = false) DeckVisibility visibility,
            Pageable pageable
    ) {
        return ApiResponse.ok(adminDeckService.getAllDecks(keyword, isOfficial, visibility, pageable), SuccessCode.GET_DECKS_SUCCESS);
    }

    @PatchMapping("/{deckId}/official")
    @Operation(summary = "Đánh dấu hoặc Hủy đánh dấu Bộ bài chuẩn Hệ Thống (isOfficial)")
    public ApiResponse<DeckResponse> updateDeckOfficial(
            @PathVariable Long deckId,
            @Valid @RequestBody AdminUpdateDeckOfficialRequest request
    ) {
        return ApiResponse.ok(adminDeckService.updateDeckOfficial(deckId, request), SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @DeleteMapping("/{deckId}")
    @Operation(summary = "Xóa hoặc gỡ bỏ bộ bài vi phạm tiêu chuẩn cộng đồng")
    public ApiResponse<Void> deleteDeck(@PathVariable Long deckId) {
        adminDeckService.deleteDeck(deckId);
        return ApiResponse.ok(null, SuccessCode.DELETE_DECK_SUCCESS);
    }
}
