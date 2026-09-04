package com.fivetuat.fsoftprojectserver.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiChatRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiQuizRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.AiSearchRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.ChatOptions;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.request.VocabLookupRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiChatResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiQuizResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.AiSearchResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.ai.response.VocabLookupResponse;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.httpsClients.AiClient;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardSrsRepository;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiIntegrationService {

    private final AiClient aiClient;
    private final DeckRepository deckRepository;
    private final UserCardSrsRepository userCardSrsRepository;

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
        log.info("Nhận request chat từ người dùng: {}", request);
        List<Long> accessibleDeckIds = getAccessibleDeckIds();

        if (accessibleDeckIds == null || accessibleDeckIds.isEmpty()) {
            return AiChatResponse.builder()
                    .answer("Bạn chưa có bộ thẻ nào để thực hiện hỏi đáp AI. Hãy tạo hoặc mở bộ thẻ mới để bắt đầu học nhé!")
                    .answerSource("NO_DECKS")
                    .citations(Collections.emptyList())
                    .build();
        }

        List<Long> finalAllowed;
        if (request.getAllowedDeckIds() != null && !request.getAllowedDeckIds().isEmpty()) {
            finalAllowed = new ArrayList<>(accessibleDeckIds);
            finalAllowed.retainAll(request.getAllowedDeckIds());
            if (finalAllowed.isEmpty()) {
                return AiChatResponse.builder()
                        .answer("Bạn không có quyền truy cập vào bộ thẻ đã chọn.")
                        .answerSource("ACCESS_DENIED")
                        .citations(Collections.emptyList())
                        .build();
            }
        } else {
            finalAllowed = accessibleDeckIds;
        }

        request.setAllowedDeckIds(finalAllowed);

        if (request.getOptions() == null) {
            request.setOptions(ChatOptions.builder()
                    .topK(5)
                    .maxOutputTokens(2048)
                    .build());
        } else if (request.getOptions().getMaxOutputTokens() == null || request.getOptions().getMaxOutputTokens() < 1500) {
            request.getOptions().setMaxOutputTokens(2048);
        }

        log.info("Gửi request chat tới fsoft-ai cho các deck {}", finalAllowed);

        try {
            AiChatResponse response = aiClient.chat(internalTokenA, request);
            log.info("Nhận được kết quả chat từ fsoft-ai. Nguồn: {}", response.getAnswerSource());
            log.info("Kết quả chat: {}", response);
            if (response != null && (response.getAnswer() == null || response.getAnswer().trim().isEmpty())) {
                log.warn("Nhận phản hồi rỗng từ fsoft-ai (Nguồn: {}), thay thế bằng thông báo dự phòng.", response.getAnswerSource());
                response.setAnswer("Rất tiếc, chưa thể khởi tạo câu trả lời cho yêu cầu này. Bạn vui lòng thử lại nhé!");
            } else if (response != null && response.getAnswer() != null) {
//                response.setAnswer(sanitizeAiAnswer(response.getAnswer()));
                response.setAnswer(response.getAnswer().trim());
            }
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 503) {
                return AiChatResponse.builder()
                        .answer("Hệ thống AI đang khởi động hoặc đang nạp dữ liệu index. Vui lòng đợi trong giây lát và thử lại nhé!")
                        .answerSource("SERVICE_UNAVAILABLE")
                        .citations(Collections.emptyList())
                        .build();
            }
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai chat: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_CHAT_ERROR);
        }
    }

    public AiQuizResponse generateQuiz(AiQuizRequest request) {
        log.info("Nhận request sinh quiz từ người dùng: {}", request);
        List<Long> accessibleDeckIds = getAccessibleDeckIds();

        if (accessibleDeckIds == null || accessibleDeckIds.isEmpty()) {
            return AiQuizResponse.builder()
                    .questions(Collections.emptyList())
                    .build();
        }

        // Nếu là ôn tập SRS (chỉ sinh quiz cho các thẻ SRS)
        if (Boolean.TRUE.equals(request.getIsSrsOnly())) {
            UUID currentUserId = SecurityUtils.getCurrentUserLogin()
                    .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
            LocalDateTime now = LocalDateTime.now();
            List<UserCardSrs> srsCards;
            if (request.getDeckId() != null) {
                // Ưu tiên các thẻ đã đến hạn review
                srsCards = userCardSrsRepository.findByProfileIdAndCardDeckIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(currentUserId, request.getDeckId(), now);
                // Nếu chưa có thẻ nào đến hạn trong deck này, lấy các thẻ đang thuộc tiến trình SRS của deck
                if (srsCards.isEmpty()) {
                    srsCards = userCardSrsRepository.findAllUserSrsCardsOrdered(currentUserId, request.getDeckId());
                }
            } else {
                srsCards = userCardSrsRepository.findByProfileIdAndNextReviewDateLessThanEqualOrderByNextReviewDateAsc(currentUserId, now);
                if (srsCards.isEmpty()) {
                    srsCards = userCardSrsRepository.findAllUserSrsCardsOrdered(currentUserId, null);
                }
            }

            List<Long> srsCardIds = srsCards.stream()
                    .map(srs -> srs.getCard().getId())
                    .distinct()
                    .collect(Collectors.toList());

            log.info("Xác định {} thẻ SRS cho user {} (deckId: {})", srsCardIds.size(), currentUserId, request.getDeckId());

            if (srsCardIds.isEmpty()) {
                log.info("Không có thẻ SRS nào được ghi nhận cho user.");
                return AiQuizResponse.builder()
                        .questions(Collections.emptyList())
                        .build();
            }
            request.setCardIds(srsCardIds);
            if (request.getQuestionCount() == null || request.getQuestionCount() <= 0) {
                request.setQuestionCount(Math.min(srsCardIds.size(), 20));
            }
        }

        List<Long> finalAllowed;
        if (request.getAllowedDeckIds() != null && !request.getAllowedDeckIds().isEmpty()) {
            finalAllowed = new ArrayList<>(accessibleDeckIds);
            finalAllowed.retainAll(request.getAllowedDeckIds());
            if (finalAllowed.isEmpty()) {
                return AiQuizResponse.builder()
                        .questions(Collections.emptyList())
                        .build();
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
        log.info("Request chi tiết: {}", request);
        try {
            AiQuizResponse response = aiClient.generateQuiz(internalTokenA, request);
            log.info("Nhận được kết quả sinh quiz từ fsoft-ai.");
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 503) {
                throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai generateQuiz: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_QUIZ_ERROR);
        }
    }

    public VocabLookupResponse lookupVocab(VocabLookupRequest request) {
        log.info("Nhận request tra từ vựng AI (Global lookup) cho từ: {}", request.getWord());

//        request.setAllowedDeckIds();

        log.info("Gửi request lookupVocab từ '{}' tới fsoft-ai (Global)", request);

        try {
            VocabLookupResponse response = aiClient.lookupVocab(internalTokenA, request);
            log.info("Nhận được kết quả lookupVocab từ fsoft-ai. Source: {}, Found: {}",
                    response != null ? response.getSource() : "null",
                    response != null ? response.getFound() : "null");
            return response;
        } catch (WebClientResponseException e) {
            log.error("fsoft-ai trả về lỗi HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 503) {
                throw new AppException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            throw e;
        } catch (Exception e) {
            log.error("Lỗi kết nối hoặc parse kết quả từ fsoft-ai lookupVocab: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.AI_LOOKUP_ERROR);
        }
    }

    private String sanitizeAiAnswer(String rawAnswer) {
        if (rawAnswer == null || rawAnswer.trim().isEmpty()) {
            return rawAnswer;
        }
        String text = rawAnswer.trim();
        String[] lines = text.split("\r?\n");
        if (lines.length > 1) {
            String lastLine = lines[lines.length - 1].trim();
            int boldCount = 0;
            int idx = 0;
            while ((idx = lastLine.indexOf("**", idx)) != -1) {
                boldCount++;
                idx += 2;
            }
            boolean isUnclosedBold = (boldCount % 2 != 0);
            boolean isUnfinishedBullet = (lastLine.startsWith("- ") || lastLine.startsWith("* "))
                    && !lastLine.endsWith(".") && !lastLine.endsWith("!") && !lastLine.endsWith("?") && !lastLine.endsWith(")") && !lastLine.endsWith(":");

            if (isUnclosedBold || isUnfinishedBullet) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < lines.length - 1; i++) {
                    sb.append(lines[i]).append("\n");
                }
                text = sb.toString().trim();
            }
        }
        return text;
    }
}
