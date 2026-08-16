package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardAiResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.InternalCardService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/internal/cards")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InternalCardController {

    InternalCardService internalCardService;

    @GetMapping("/changed-since")
    public ApiResponse<PageResponse<CardAiResponse>> getChangedCards(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "200") int size) {
        if (since == null) {
            since = Instant.EPOCH; // 1970-01-01T00:00:00Z
        }
        return ApiResponse.ok(internalCardService.getChangedCards(since, page, size), SuccessCode.CARD_CHANGED_SINCE_SUCCESS);
    }

    @GetMapping("/ids")
    public ApiResponse<PageResponse<Long>> getCardIds(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5000") int size) {
        
        return ApiResponse.ok(internalCardService.getCardIds(page, size), SuccessCode.CARD_IDS_SUCCESS);
    }
}
