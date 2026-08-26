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
import io.swagger.v3.oas.annotations.Parameter;
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

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Card Controller", description = "Quản lý dữ liệu Thẻ ghi nhớ (Flashcards) trong các Bộ thẻ (Decks)")
public class CardController {

    CardService cardService;

    @Operation(
        summary = "Tạo mới thẻ ghi nhớ vào một bộ bài", 
        description = "Thêm một Flashcard mới vào bộ bài được chỉ định (deckId). Hỗ trợ upload ảnh (imageFile) minh hoạ và âm thanh (audioFile) phát âm đi kèm. JSON data của thẻ được parse từ Request part 'request'."
    )
    @PostMapping(value = "/deck/{deckId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CardResponse> createCard(
            @Parameter(description = "ID của bộ thẻ muốn thêm thẻ bài") @PathVariable Long deckId,
            @Parameter(description = "Thông tin chữ của thẻ bài (định dạng JSON)") @RequestPart("request") @Valid CardCreationRequest request,
            @Parameter(description = "File ảnh mặt trước/sau của thẻ (tuỳ chọn)") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @Parameter(description = "File âm thanh phát âm của thẻ (tuỳ chọn)") @RequestPart(value = "audioFile", required = false) MultipartFile audioFile
    ) {
        return ApiResponse.ok(cardService.createCard(deckId, request, imageFile, audioFile), SuccessCode.CREATE_CARD_SUCCESS);
    }

    @Operation(
        summary = "Lấy danh sách thẻ ghi nhớ theo Bộ thẻ", 
        description = "Lấy toàn bộ các thẻ bài có trong một bộ bài cụ thể, hỗ trợ tính năng phân trang (page, size) và sắp xếp tự động."
    )
    @GetMapping("/deck/{deckId}")
    public ApiResponse<PageResponse<CardResponse>> getCardsByDeckId(
            @Parameter(description = "ID của bộ thẻ") @PathVariable Long deckId,
            Pageable pageable
    ) {
        return ApiResponse.ok(cardService.getCardsByDeckId(deckId, pageable), SuccessCode.GET_CARDS_SUCCESS);
    }

    @Operation(
        summary = "Xem thông tin chi tiết một Thẻ ghi nhớ", 
        description = "Truy xuất thông tin chi tiết (nội dung chữ, link ảnh, link audio, v.v...) của một thẻ bài dựa vào cardId."
    )
    @GetMapping("/{cardId}")
    public ApiResponse<CardResponse> getCardById(
            @Parameter(description = "ID của thẻ bài cần xem") @PathVariable Long cardId) {
        return ApiResponse.ok(cardService.getCardById(cardId), SuccessCode.GET_CARD_SUCCESS);
    }

    @Operation(
        summary = "Cập nhật dữ liệu thẻ ghi nhớ", 
        description = "Sửa đổi nội dung (text), thay thế ảnh mới hoặc âm thanh mới cho thẻ bài đã tồn tại."
    )
    @PutMapping(value = "/{cardId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CardResponse> updateCard(
            @Parameter(description = "ID của thẻ bài cần cập nhật") @PathVariable Long cardId,
            @Parameter(description = "Thông tin mới của thẻ bài") @RequestPart("request") @Valid CardUpdateRequest request,
            @Parameter(description = "Ảnh thay thế (nếu có)") @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            @Parameter(description = "Âm thanh thay thế (nếu có)") @RequestPart(value = "audioFile", required = false) MultipartFile audioFile
    ) {
        return ApiResponse.ok(cardService.updateCard(cardId, request, imageFile, audioFile), SuccessCode.UPDATE_CARD_SUCCESS);
    }

    @Operation(
        summary = "Xóa thẻ ghi nhớ", 
        description = "Xóa hoàn toàn thẻ bài khỏi bộ thẻ và xóa cả các tài nguyên ảnh/âm thanh đi kèm."
    )
    @DeleteMapping("/{cardId}")
    public ApiResponse<Void> deleteCard(
            @Parameter(description = "ID của thẻ bài cần xóa") @PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ApiResponse.ok(null, SuccessCode.DELETE_CARD_SUCCESS);
    }

    @Operation(
        summary = "Cập nhật vị trí/thứ tự của các thẻ bài", 
        description = "Thay đổi thứ tự hiển thị của nhiều thẻ bài cùng lúc trong một bộ thẻ (hữu ích khi người dùng kéo thả để sắp xếp lại bài)."
    )
    @PutMapping("/deck/{deckId}/positions")
    public ApiResponse<Void> updateCardPositions(
            @Parameter(description = "ID của bộ thẻ") @PathVariable Long deckId,
            @Parameter(description = "Danh sách vị trí mới của từng thẻ") @Valid @RequestBody List<CardPositionRequest> requests
    ) {
        cardService.updateCardPositions(deckId, requests);
        return ApiResponse.ok(null, SuccessCode.UPDATE_CARD_SUCCESS);
    }

    @Operation(
        summary = "Xuất danh sách thẻ bài ra file Excel (.xlsx)",
        description = "Tải file Excel .xlsx chứa toàn bộ thẻ bài có trong bộ bài."
    )
    @GetMapping("/deck/{deckId}/export/excel")
    public ResponseEntity<byte[]> exportCardsToExcel(
            @Parameter(description = "ID của bộ thẻ") @PathVariable Long deckId
    ) {
        byte[] excelBytes = cardService.exportCardsToExcel(deckId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Deck_" + deckId + "_Flashcards.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @Operation(
        summary = "Xuất danh sách thẻ bài ra file PDF (.pdf)",
        description = "Tải file PDF chứa toàn bộ thẻ bài có trong bộ bài để in ấn hoặc học tập offline."
    )
    @GetMapping("/deck/{deckId}/export/pdf")
    public ResponseEntity<byte[]> exportCardsToPdf(
            @Parameter(description = "ID của bộ thẻ") @PathVariable Long deckId
    ) {
        byte[] pdfBytes = cardService.exportCardsToPdf(deckId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Deck_" + deckId + "_Flashcards.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
