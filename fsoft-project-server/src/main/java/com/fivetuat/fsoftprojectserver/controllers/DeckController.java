package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckVisibilityRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckController {
    DeckService deckService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> createDeck(
            @RequestPart("request") @Valid DeckCreateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.createDeck(request, coverImage), SuccessCode.CREATE_DECK_SUCCESS);
    }

    @GetMapping("/{id}")
    public ApiResponse<DeckResponse> getDeck(@PathVariable Long id) {
        return ApiResponse.ok(deckService.getDeck(id), SuccessCode.GET_DECK_SUCCESS);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DeckResponse> updateDeck(
            @PathVariable Long id,
            @RequestPart("request") @Valid DeckUpdateRequest request,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage) {
        return ApiResponse.ok(deckService.updateDeck(id, request, coverImage), SuccessCode.UPDATE_DECK_SUCCESS);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ApiResponse.ok(null, SuccessCode.DELETE_DECK_SUCCESS);
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<DeckResponse>> getMyDecks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(deckService.getMyDecks(page, size), SuccessCode.GET_DECKS_SUCCESS);
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
            @RequestBody ShareDeckRequest request) {
        deckService.share(id, request);
        return ApiResponse.ok(null, SuccessCode.DECK_SHARED);
    }
}
