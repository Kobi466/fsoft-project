package com.fivetuat.fsoftprojectserver.httpsClients;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiSearchRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface AiClient {
    
    @PostExchange(value = "/internal/v1/search", contentType = "application/json")
    AiSearchResponse search(
            @RequestHeader("X-Internal-Token") String token,
            @RequestBody AiSearchRequest request
    );

    @PostExchange(value = "/internal/v1/chat", contentType = "application/json")
    com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse chat(
            @RequestHeader("X-Internal-Token") String token,
            @RequestBody com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest request
    );

    @PostExchange(value = "/internal/v1/quiz/generate", contentType = "application/json")
    com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse generateQuiz(
            @RequestHeader("X-Internal-Token") String token,
            @RequestBody com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest request
    );
    
}
