package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.DeckService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Deck Controller", description = "Quản lý bộ thẻ (Decks)")
public class DeckController {
    DeckService deckService;

    @Operation(summary = "Tạo mới bộ thẻ", description = "Tạo mới một bộ thẻ")
    @RequestBody(content = @Content(
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
    @RequestBody(content = @Content(encoding = @Encoding(
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
    public ApiResponse<PageResponse<DeckResponse>> getMyDecks(Pageable pageable) {
        return ApiResponse.ok(deckService.getMyDecks(pageable), SuccessCode.GET_DECKS_SUCCESS);
    }

    @Operation(summary = "Lấy danh sách tất cả bộ thẻ", description = "Lấy danh sách tất cả các bộ thẻ")
    @GetMapping
    public ApiResponse<PageResponse<DeckResponse>> getAllDecks(
            @RequestParam(required = false) String tag,
            Pageable pageable
    ) {
        return ApiResponse.ok(deckService.getAllDecks(tag, pageable), SuccessCode.GET_DECKS_SUCCESS);
    }
}
