package com.fivetuat.fsoftprojectserver.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiSearchRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.httpsClients.AiClient;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiClient aiClient;
    private final DeckRepository deckRepository;

    @Value("${fsoft-ai.internal-token}")
    private String internalTokenA;

    private List<Long> getAccessibleDeckIds() {
        UUID currentUserId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        return deckRepository.findAllAccessibleDeckIdsByProfileId(currentUserId);
    }

    public AiSearchResponse searchCards(String query) {
        List<Long> allowedDeckIds = getAccessibleDeckIds();
        if (allowedDeckIds == null || allowedDeckIds.isEmpty()) {
            return new AiSearchResponse(Collections.emptyList(), 0, 0);
        }

        log.info("Gửi request tìm kiếm '{}' tới fsoft-ai cho các deck {}", query, allowedDeckIds);

        AiSearchRequest request = AiSearchRequest.builder()
                .query(query)
                .allowedDeckIds(allowedDeckIds)
                .topK(5)
                .build();

        try {
            AiSearchResponse response = aiClient.search(internalTokenA, request);
            log.info("Nhận được kết quả tìm kiếm từ fsoft-ai: {} kết quả", response.getCandidateCount());
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai search: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SEARCH_ERROR);
        }
    }

    public AiChatResponse chat(AiChatRequest request) {
        List<Long> accessibleDeckIds = getAccessibleDeckIds();

        if (accessibleDeckIds == null || accessibleDeckIds.isEmpty()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        List<Long> finalAllowed;
        if (request.getAllowedDeckIds() != null && !request.getAllowedDeckIds().isEmpty()) {
            finalAllowed = new ArrayList<>(accessibleDeckIds);
            finalAllowed.retainAll(request.getAllowedDeckIds());
            if (finalAllowed.isEmpty()) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            finalAllowed = accessibleDeckIds;
        }

        request.setAllowedDeckIds(finalAllowed);

        log.info("Gửi request chat tới fsoft-ai cho các deck {}", finalAllowed);

        try {
            AiChatResponse response = aiClient.chat(internalTokenA, request);
            log.info("Nhận được kết quả chat từ fsoft-ai. Nguồn: {}", response.getAnswerSource());
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai chat: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_CHAT_ERROR);
        }
    }

    public AiQuizResponse generateQuiz(AiQuizRequest request) {
        List<Long> accessibleDeckIds = getAccessibleDeckIds();

        if (accessibleDeckIds == null || accessibleDeckIds.isEmpty()) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        List<Long> finalAllowed;
        if (request.getAllowedDeckIds() != null && !request.getAllowedDeckIds().isEmpty()) {
            finalAllowed = new ArrayList<>(accessibleDeckIds);
            finalAllowed.retainAll(request.getAllowedDeckIds());
            if (finalAllowed.isEmpty()) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
        } else {
            finalAllowed = accessibleDeckIds;
        }

        if (request.getCardIds() != null && !request.getCardIds().isEmpty() && request.getCardIds().size() < 4) {
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }

        if (request.getTypes() == null || request.getTypes().isEmpty()) {
            request.setTypes(List.of("MULTIPLE_CHOICE"));
        }

        request.setAllowedDeckIds(finalAllowed);

        log.info("Gửi request sinh quiz tới fsoft-ai cho các deck {}", finalAllowed);

        try {
            AiQuizResponse response = aiClient.generateQuiz(internalTokenA, request);
            log.info("Nhận được kết quả sinh quiz từ fsoft-ai.");
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai generateQuiz: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_QUIZ_ERROR);
        }
    }
}
