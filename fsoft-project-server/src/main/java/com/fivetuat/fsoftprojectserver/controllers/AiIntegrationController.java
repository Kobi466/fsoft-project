package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AiIntegrationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AiIntegrationController {

    AiIntegrationService aiIntegrationService;

    @GetMapping("/search")
    public ApiResponse<AiSearchResponse> search(@RequestParam String query) {
        
        AiSearchResponse response = aiIntegrationService.searchCards(query);
        
        return ApiResponse.ok(response, SuccessCode.AI_SEARCH_SUCCESS);
    }

    @PostMapping("/chat")
    public ApiResponse<AiChatResponse> chat(
            @RequestBody AiChatRequest request) {
        
        AiChatResponse response = aiIntegrationService.chat(request);
        
        return ApiResponse.ok(response, SuccessCode.AI_CHAT_SUCCESS);
    }

    @PostMapping("/quiz")
    public ApiResponse<AiQuizResponse> generateQuiz(
            @RequestBody AiQuizRequest request) {
        
        AiQuizResponse response = aiIntegrationService.generateQuiz(request);
        
        return ApiResponse.ok(response, SuccessCode.GENERATED_QUIZ_SUCCESS);
    }
}
