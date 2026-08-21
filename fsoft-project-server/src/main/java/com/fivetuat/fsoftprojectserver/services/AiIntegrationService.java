package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiSearchRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.httpsClients.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiClient aiClient;
    private final DeckRepository deckRepository;

    @Value("${fsoft-ai.internal-token}")
    private String internalTokenA;

    private List<Long> getAccessibleDeckIds() {
//        UUID currentUserId = SecurityUtils.getCurrentUserLogin()
//                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
//        return deckRepository.findAllAccessibleDeckIdsByProfileId(currentUserId);
        return new ArrayList<>(){
            {
                add(1L);
                add(2L);
                add(3L);
                add(4L);
            }
        };
    }

    public AiSearchResponse searchCards(String query) {
        List<Long> allowedDeckIds = this.getAccessibleDeckIds();
        
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
        } catch (Exception e) {
            log.error("Lỗi khi gọi fsoft-ai search: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_SEARCH_ERROR);
        }
    }

    public AiChatResponse chat(AiChatRequest request) {
        List<Long> allowedDeckIds = getAccessibleDeckIds();

        if (allowedDeckIds == null || allowedDeckIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY);
        }

        request.setAllowedDeckIds(allowedDeckIds);

        log.info("Gửi request chat tới fsoft-ai cho các deck {}", allowedDeckIds);

        try {
            AiChatResponse response = aiClient.chat(internalTokenA, request);
            log.info("Nhận được kết quả chat từ fsoft-ai. Nguồn: {}", response.getAnswerSource());
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi gọi fsoft-ai chat: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_CHAT_ERROR);
        }
    }

    public AiQuizResponse generateQuiz(AiQuizRequest request) {
        List<Long> allowedDeckIds = getAccessibleDeckIds();

        if (allowedDeckIds == null || allowedDeckIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_KEY); 
        }
        
        if (request.getCardIds() != null && request.getCardIds().size() < 4) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }

        request.setAllowedDeckIds(allowedDeckIds);

        log.info("Gửi request sinh quiz tới fsoft-ai cho các deck {}", allowedDeckIds);

        try {
            AiQuizResponse response = aiClient.generateQuiz(internalTokenA, request);
            log.info("Nhận được kết quả sinh quiz từ fsoft-ai.");
            return response;
        } catch (Exception e) {
            log.error("Lỗi khi gọi fsoft-ai generateQuiz: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_QUIZ_ERROR);
        }
    }
}
