package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckVisibilityRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Deck Controller", description = "Quản lý bộ thẻ (Decks)")
public class DeckController {
    DeckService deckService;

    @Operation(summary = "Tạo mới bộ thẻ", description = "Tạo mới một bộ thẻ")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(
            encoding = @Encoding(
                name = "request",
                contentType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> createDeck(
            @RequestPart(value = "request") @Valid DeckCreateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.createDeck(request, coverImage), SuccessCode.CREATE_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy bộ thẻ theo ID", description = "Lấy một bộ thẻ theo ID")
    @GetMapping("/{id}")
    public ApiResponse<DeckResponse> getDeck(@PathVariable Long id) {
        return ApiResponse.ok(deckService.getDeck(id), SuccessCode.GET_DECK_SUCCESS);
    }

    @Operation(summary = "Cập nhật bộ thẻ theo ID", description = "Cập nhật một bộ thẻ theo ID")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(
                name = "request",
                contentType = MediaType.APPLICATION_JSON_VALUE
            )
        )
    )

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> updateDeck(
            @PathVariable Long id,
            @RequestPart("request") @Valid DeckUpdateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.updateDeck(id, request, coverImage), SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @Operation(summary = "Xóa bộ thẻ theo ID", description = "Xóa một bộ thẻ theo ID")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách bộ thẻ của tôi", description = "Lấy danh sách các bộ thẻ do tôi tạo")
    @GetMapping("/my")
    public ApiResponse<PageResponse<DeckResponse>> getMyDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(deckService.getMyDecks(page, size), SuccessCode.GET_DECKS_SUCCESS);
    }

    @GetMapping("/public")
    public ApiResponse<PageResponse<DeckResponse>> getPublicDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String sourceLang,
            @RequestParam(required = false) String targetLang
    ) {
        return ApiResponse.ok(
                deckService.getPublicDecks(page, size, keyword, tagId, sourceLang, targetLang),
                SuccessCode.GET_DECKS_SUCCESS
        );
    }

    @GetMapping("/status")
    public ApiResponse<PageResponse<DeckResponse>> getDecksByStatus(
            @RequestParam(defaultValue = "PUBLIC") DeckVisibility status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(
                deckService.getDecksByStatus(status, page, size),
                SuccessCode.GET_DECKS_SUCCESS
        );
    }
    @PutMapping("/{id}/visibility")
    public ApiResponse<Void> convertDeckVisibility(
            @PathVariable Long id,
            @RequestBody DeckVisibilityRequest request) {
        deckService.convertDeckVisibility(id, request.getVisibility());
        return ApiResponse.ok(null, SuccessCode.DECK_VISIBILITY_UPDATED);
    }

    @PostMapping("/{id}/fork")
    public ApiResponse<DeckResponse> forkDeck(@PathVariable Long id) {
        DeckResponse response = deckService.fork(id);
        return ApiResponse.ok(response, SuccessCode.DECK_FORKED);
    }
    @PostMapping("/{id}/share")
    public ApiResponse<Void> shareDeck(
            @PathVariable Long id,
            @RequestBody @Valid ShareDeckRequest request) {
        deckService.share(id, request);
        return ApiResponse.ok(null, SuccessCode.DECK_SHARED);
    }

    @Operation(summary = "Lấy danh sách người được share", description = "Chỉ chủ sở hữu Deck mới có quyền xem")
    @GetMapping("/{id}/shares")
    public ApiResponse<PageResponse<DeckShareResponse>> getSharedUsers(@PathVariable Long id, Pageable pageable) {
        return ApiResponse.ok(deckService.getSharedUsers(id, pageable), SuccessCode.GET_SHARED_USERS_SUCCESS);
    }

    @Operation(summary = "Cập nhật quyền truy cập", description = "Đổi quyền (VIEWER/EDITOR) cho một user cụ thể")
    @PutMapping("/{id}/shares/{profileId}")
    public ApiResponse<Void> updateSharePermission(
            @PathVariable Long id,
            @PathVariable UUID profileId,
            @RequestParam SharePermission permission
    ) {
        deckService.updateSharePermission(id, profileId, permission);
        return ApiResponse.ok(null, SuccessCode.UPDATE_ROLE_DECK_SUCCESS);
    }

    @Operation(summary = "Thu hồi quyền truy cập", description = "Xóa user khỏi danh sách chia sẻ")
    @DeleteMapping("/{id}/shares/{profileId}")
    public ApiResponse<Void> revokeShare(
            @PathVariable Long id,
            @PathVariable UUID profileId
    ) {
        deckService.revokeShare(id, profileId);
        return ApiResponse.ok(null, SuccessCode.UPDATE_ROLE_DECK_SUCCESS);
    }

    @Operation(summary = "Bật/Tắt chia sẻ qua link", description = "Chỉ chủ sở hữu Deck mới có quyền")
    @PostMapping("/{id}/share-link/toggle")
    public ApiResponse<DeckResponse> toggleShareLink(@PathVariable Long id) {
        DeckResponse response = deckService.toggleShareLink(id);
        return ApiResponse.ok(response, SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @Operation(summary = "Truy cập Deck qua Link Share", description = "Dành cho người lạ truy cập qua link bí mật")
    @GetMapping("/shared/{shareToken}")
    public ApiResponse<DeckResponse> getDeckByShareToken(@PathVariable String shareToken) {
        DeckResponse response = deckService.getDeckByShareToken(shareToken);
        return ApiResponse.ok(response, SuccessCode.GET_DECK_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách bộ thẻ được chia sẻ với tôi", description = "Lấy danh sách các bộ thẻ do người khác chia sẻ cho tôi")
    @GetMapping("/shared-with-me")
    public ApiResponse<PageResponse<DeckResponse>> getSharedWithMeDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(deckService.getSharedWithMeDecks(page, size), SuccessCode.GET_DECKS_SUCCESS);
    }
}
