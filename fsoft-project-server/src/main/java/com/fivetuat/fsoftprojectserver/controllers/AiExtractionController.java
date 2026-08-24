package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.TextExtractionRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.UrlExtractionRequest;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai/extract")
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AiExtractionController {

    AiExtractionService aiExtractionService;

    @PostMapping("/text")
    public ApiResponse<List<CardCreationRequest>> extractFromText(@RequestBody TextExtractionRequest request) {
        List<CardCreationRequest> result = aiExtractionService.extractedText(request.text(), request.category(), request.deckId());
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS); 
    }

    @PostMapping("/url")
    public ApiResponse<List<CardCreationRequest>> extractFromUrl(@RequestBody UrlExtractionRequest request) {
        List<CardCreationRequest> result = aiExtractionService.extractedUrl(request.url(), request.category(), request.deckId());
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS); 
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<CardCreationRequest>> extractFromFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("category") String category,
            @RequestParam("deckId") Long deckId) {
        List<CardCreationRequest> result = aiExtractionService.extractedFile(file, category, deckId);
        return ApiResponse.ok(result, SuccessCode.AI_EXTRACTION_SUCCESS); 
    }
}
