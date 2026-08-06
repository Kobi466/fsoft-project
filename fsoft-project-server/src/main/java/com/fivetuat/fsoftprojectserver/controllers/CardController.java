package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card Controller", description = "Quản lý Thẻ ghi nhớ (Flashcards)")
public class CardController {

    CardService cardService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Tạo mới Thẻ ghi nhớ thuộc một Bộ thẻ (Deck)")
    public ApiResponse<CardResponse> createCard(@Valid @RequestBody CardCreationRequest request) {
        return ApiResponse.<CardResponse>builder()
                .status(200)
                .message("Tạo thẻ ghi nhớ thành công")
                .data(cardService.createCard(request))
                .build();
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Lấy thông tin chi tiết Thẻ ghi nhớ theo ID")
    public ApiResponse<CardResponse> getCardById(@PathVariable Long cardId) {
        return ApiResponse.<CardResponse>builder()
                .status(200)
                .data(cardService.getCardById(cardId))
                .build();
    }

    @GetMapping("/deck/{deckId}")
    @Operation(summary = "Lấy danh sách Thẻ ghi nhớ thuộc Bộ thẻ (Phân trang & Tìm kiếm)")
    public ApiResponse<PageResponse<CardResponse>> getCardsByDeckId(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "position") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        return ApiResponse.<PageResponse<CardResponse>>builder()
                .status(200)
                .data(cardService.getCardsByDeckId(deckId, page, size, keyword, sortBy, sortDirection))
                .build();
    }

    @GetMapping("/deck/{deckId}/all")
    @Operation(summary = "Lấy tất cả Thẻ ghi nhớ trong Bộ thẻ sắp xếp theo vị trí position")
    public ApiResponse<List<CardResponse>> getAllCardsInDeck(@PathVariable Long deckId) {
        return ApiResponse.<List<CardResponse>>builder()
                .status(200)
                .data(cardService.getAllCardsInDeck(deckId))
                .build();
    }

    @PutMapping("/{cardId}")
    @Operation(summary = "Cập nhật Thẻ ghi nhớ")
    public ApiResponse<CardResponse> updateCard(
            @PathVariable Long cardId,
            @Valid @RequestBody CardUpdateRequest request
    ) {
        return ApiResponse.<CardResponse>builder()
                .status(200)
                .message("Cập nhật thẻ ghi nhớ thành công")
                .data(cardService.updateCard(cardId, request))
                .build();
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Xóa Thẻ ghi nhớ theo ID")
    public ApiResponse<String> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ApiResponse.<String>builder()
                .status(200)
                .message("Xóa thẻ ghi nhớ thành công")
                .data("Card deleted successfully")
                .build();
    }

    @PatchMapping("/{cardId}/position")
    @Operation(summary = "Cập nhật vị trí hiển thị (position) của Thẻ ghi nhớ")
    public ApiResponse<CardResponse> updateCardPosition(
            @PathVariable Long cardId,
            @RequestParam Integer newPosition
    ) {
        return ApiResponse.<CardResponse>builder()
                .status(200)
                .message("Cập nhật vị trí hiển thị thành công")
                .data(cardService.updateCardPosition(cardId, newPosition))
                .build();
    }
}