package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardPositionRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card Controller", description = "Quản lý Thẻ ghi nhớ (Flashcards)")
public class CardController {

    CardService cardService;

    @PostMapping(value = "/deck/{deckId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tạo mới thẻ ghi nhớ trong bộ bài")
    public ApiResponse<CardResponse> createCard(
            @PathVariable Long deckId,
            @RequestPart("request") @Valid CardCreationRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile
    ) {
        return ApiResponse.ok(cardService.createCard(deckId, request, imageFile, audioFile), SuccessCode.CREATE_CARD_SUCCESS);
    }

    @GetMapping("/deck/{deckId}")
    @Operation(summary = "Lấy danh sách Thẻ ghi nhớ thuộc Bộ thẻ (Phân trang)")
    public ApiResponse<PageResponse<CardResponse>> getCardsByDeckId(
            @PathVariable Long deckId,
            Pageable pageable
    ) {
        return ApiResponse.ok(cardService.getCardsByDeckId(deckId, pageable), SuccessCode.GET_CARDS_SUCCESS);
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Lấy thông tin chi tiết Thẻ ghi nhớ theo ID")
    public ApiResponse<CardResponse> getCardById(@PathVariable Long cardId) {
        return ApiResponse.ok(cardService.getCardById(cardId), SuccessCode.GET_CARD_SUCCESS);
    }

    @PutMapping(value = "/{cardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật Thẻ ghi nhớ")
    public ApiResponse<CardResponse> updateCard(
            @PathVariable Long cardId,
            @RequestPart("request") @Valid CardUpdateRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestPart(value = "audioFile", required = false) MultipartFile audioFile
    ) {
        return ApiResponse.ok(cardService.updateCard(cardId, request, imageFile, audioFile), SuccessCode.UPDATE_CARD_SUCCESS);
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Xóa Thẻ ghi nhớ theo ID")
    public ApiResponse<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ApiResponse.ok(null, SuccessCode.DELETE_CARD_SUCCESS);
    }

    @PutMapping("/deck/{deckId}/positions")
    @Operation(summary = "Thay đổi thứ tự danh sách thẻ bài trong bộ")
    public ApiResponse<Void> updateCardPositions(
            @PathVariable Long deckId,
            @Valid @RequestBody List<CardPositionRequest> requests
    ) {
        cardService.updateCardPositions(deckId, requests);
        return ApiResponse.ok(null, SuccessCode.UPDATE_CARD_SUCCESS);
    }
}
