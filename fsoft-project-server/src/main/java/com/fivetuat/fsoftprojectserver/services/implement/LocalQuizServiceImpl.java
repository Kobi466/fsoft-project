package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.request.QuizGenerateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizGenerateResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.QuizQuestionResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.services.LocalQuizService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocalQuizServiceImpl implements LocalQuizService {

    CardRepository cardRepository;
    DeckRepository deckRepository;

    private List<Long> getAccessibleDeckIds() {
        UUID currentUserId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        return deckRepository.findAllAccessibleDeckIdsByProfileId(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizGenerateResponse generateLocalQuiz(QuizGenerateRequest request) {
        log.info("Bắt đầu sinh Quiz nội bộ từ Database...");

        List<Long> accessibleDeckIds = getAccessibleDeckIds();
        if (accessibleDeckIds == null || accessibleDeckIds.isEmpty()) {
            return QuizGenerateResponse.builder()
                    .questions(Collections.emptyList())
                    .totalQuestions(0)
                    .engine("LOCAL")
                    .build();
        }

        List<Card> availableCards = new ArrayList<>();
        if (request.getCardIds() != null && !request.getCardIds().isEmpty()) {
            availableCards = cardRepository.findAllById(request.getCardIds());
        } else if (request.getDeckId() != null) {
            if (!accessibleDeckIds.contains(request.getDeckId())) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
            availableCards = cardRepository.findByDeckId(request.getDeckId());
        } else if (request.getAllowedDeckIds() != null && !request.getAllowedDeckIds().isEmpty()) {
            List<Long> finalAllowed = new ArrayList<>(accessibleDeckIds);
            finalAllowed.retainAll(request.getAllowedDeckIds());
            if (finalAllowed.isEmpty()) {
                throw new AppException(ErrorCode.ACCESS_DENIED);
            }
            availableCards = cardRepository.findByDeckIdIn(finalAllowed);
        } else {
            availableCards = cardRepository.findByDeckIdIn(accessibleDeckIds);
        }

        if (availableCards.isEmpty()) {
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }

        int totalAvailableCards = availableCards.size();

        // KIỂM TRA RÀNG BUỘC: Số lượng câu hỏi người dùng nhập KHÔNG ĐƯỢC vượt quá số thẻ có sẵn trong bộ thẻ!
        if (request.getQuestionCount() != null) {
            if (request.getQuestionCount() <= 0 || request.getQuestionCount() > totalAvailableCards) {
                log.warn("Số lượng câu hỏi yêu cầu ({}) vượt quá tổng số thẻ bài hiện có ({}) trong bộ thẻ.",
                        request.getQuestionCount(), totalAvailableCards);
                throw new AppException(ErrorCode.INVALID_QUESTION_COUNT);
            }
        }

        int count = (request.getQuestionCount() != null && request.getQuestionCount() > 0)
                ? Math.min(request.getQuestionCount(), totalAvailableCards)
                : Math.min(10, totalAvailableCards);

        List<Card> shuffledCards = new ArrayList<>(availableCards);
        Collections.shuffle(shuffledCards);
        List<Card> selectedTargetCards = shuffledCards.subList(0, count);

        List<QuizQuestionResponse> questions = new ArrayList<>();
        int index = 0;

        for (Card targetCard : selectedTargetCards) {
            index++;
            String word = targetCard.getWord();
            String correctMeaning = targetCard.getMeaning();

            // Lựa chọn các đáp án nhiễu
            List<String> distractors = availableCards.stream()
                    .map(Card::getMeaning)
                    .filter(meaning -> meaning != null && !meaning.equalsIgnoreCase(correctMeaning))
                    .distinct()
                    .collect(Collectors.toList());

            Collections.shuffle(distractors);

            List<String> choices = new ArrayList<>();
            choices.add(correctMeaning);

            for (int i = 0; i < Math.min(3, distractors.size()); i++) {
                choices.add(distractors.get(i));
            }

            while (choices.size() < 4) {
                choices.add("Lựa chọn khác " + (choices.size() + 1));
            }

            Collections.shuffle(choices);
            int correctIndex = choices.indexOf(correctMeaning);

            QuizQuestionResponse question = QuizQuestionResponse.builder()
                    .index(index)
                    .cardId(targetCard.getId())
                    .word(word)
                    .meaning(correctMeaning)
                    .questionText(word)
                    .questionType("MULTIPLE_CHOICE")
                    .choices(choices)
                    .correctIndex(correctIndex)
                    .correctAnswer(correctMeaning)
                    .explanation("Đây là ý nghĩa của từ: " + correctMeaning)
                    .engine("LOCAL")
                    .build();

            questions.add(question);
        }

        log.info("Khởi tạo thành công {} câu hỏi Quiz nội bộ (Local).", questions.size());

        return QuizGenerateResponse.builder()
                .questions(questions)
                .totalQuestions(questions.size())
                .engine("LOCAL")
                .build();
    }
}
