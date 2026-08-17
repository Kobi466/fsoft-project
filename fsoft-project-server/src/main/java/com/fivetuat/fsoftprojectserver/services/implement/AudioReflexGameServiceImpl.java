package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.Instant;
import java.util.*;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.request.AudioReflexSubmitRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexLeaderboardResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.GameRecord;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.GameRecordRepository;
import com.fivetuat.fsoftprojectserver.services.AudioReflexGameService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AudioReflexGameServiceImpl implements AudioReflexGameService {

    DeckRepository deckRepository;
    CardRepository cardRepository;
    GameRecordRepository gameRecordRepository;
    AuthRepository authRepository;

    private static final String GAME_TYPE_AUDIO_REFLEX = "AUDIO_REFLEX";

    @Override
    @Transactional(readOnly = true)
    public AudioReflexGameResponse generateGame(Long deckId, int questionCount) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        // Dùng đúng method findByDeckId trong CardRepository của bạn
        List<Card> allCards = cardRepository.findByDeckId(deckId);
        if (allCards.size() < 4) {
            // Bộ thẻ cần ít nhất 4 từ để tạo 4 lựa chọn trắc nghiệm
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }

        Collections.shuffle(allCards);
        List<Card> questionCards = allCards.subList(0, Math.min(questionCount, allCards.size()));

        List<AudioReflexGameResponse.AudioQuestionItem> questions = new ArrayList<>();

        for (Card targetCard : questionCards) {
            // Đáp án đúng (lấy meaning)
            List<AudioReflexGameResponse.OptionItem> options = new ArrayList<>();
            options.add(new AudioReflexGameResponse.OptionItem(targetCard.getId(), targetCard.getMeaning()));

            // 3 Đáp án gây nhiễu từ các thẻ khác
            List<Card> distractors = new ArrayList<>(allCards);
            distractors.remove(targetCard);
            Collections.shuffle(distractors);

            for (int i = 0; i < Math.min(3, distractors.size()); i++) {
                Card distractorCard = distractors.get(i);
                options.add(new AudioReflexGameResponse.OptionItem(distractorCard.getId(), distractorCard.getMeaning()));
            }

            // Xáo trộn thứ tự 4 đáp án A, B, C, D
            Collections.shuffle(options);

            questions.add(AudioReflexGameResponse.AudioQuestionItem.builder()
                    .cardId(targetCard.getId())
                    .word(targetCard.getWord())
                    .phonetic(targetCard.getPhonetic())
                    .imageUrl(targetCard.getImageUrl())
                    .exampleSentence(targetCard.getExampleSentence())
                    .exampleMeaning(targetCard.getExampleMeaning())
                    .correctOptionId(targetCard.getId())
                    .options(options)
                    .build());
        }

        return AudioReflexGameResponse.builder()
                .deckId(deck.getId())
                .deckTitle(deck.getTitle())
                .totalQuestions(questions.size())
                .questions(questions)
                .build();
    }

    @Override
    @Transactional
    public AudioReflexLeaderboardResponse submitResult(AudioReflexSubmitRequest request, String userId) {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Auth user = authRepository.findById(userUuid)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Deck deck = deckRepository.findById(request.getDeckId())
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        GameRecord record = GameRecord.builder()
                .user(user)
                .deck(deck)
                .gameType(GAME_TYPE_AUDIO_REFLEX)
                .score(request.getScore())
                .maxCombo(request.getMaxCombo())
                .correctAnswers(request.getCorrectAnswers())
                .totalQuestions(request.getTotalQuestions())
                .completionTime(request.getCompletionTime())
                .createdAt(Instant.now())
                .build();

        GameRecord saved = gameRecordRepository.save(record);

        String name = (user.getProfile() != null && user.getProfile().getFullName() != null)
                ? user.getProfile().getFullName()
                : user.getEmail();

        String avatar = user.getProfile() != null ? user.getProfile().getAvatar() : null;
        double accuracy = request.getTotalQuestions() != null && request.getTotalQuestions() > 0
                ? ((double) request.getCorrectAnswers() / request.getTotalQuestions()) * 100
                : 0.0;

        return AudioReflexLeaderboardResponse.builder()
                .userId(String.valueOf(user.getId()))
                .userName(name)
                .userAvatar(avatar)
                .score(saved.getScore())
                .maxCombo(saved.getMaxCombo())
                .accuracy(Math.round(accuracy * 10.0) / 10.0)
                .playedAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AudioReflexLeaderboardResponse> getLeaderboard(Long deckId, int limit) {
        List<GameRecord> topRecords = gameRecordRepository.findTopLeaderboard(
                deckId, GAME_TYPE_AUDIO_REFLEX, PageRequest.of(0, limit)
        );

        List<AudioReflexLeaderboardResponse> result = new ArrayList<>();
        int rank = 1;
        for (GameRecord r : topRecords) {
            String name = (r.getUser().getProfile() != null && r.getUser().getProfile().getFullName() != null)
                    ? r.getUser().getProfile().getFullName()
                    : r.getUser().getEmail();

            String avatar = r.getUser().getProfile() != null ? r.getUser().getProfile().getAvatar() : null;
            double accuracy = r.getTotalQuestions() != null && r.getTotalQuestions() > 0
                    ? ((double) r.getCorrectAnswers() / r.getTotalQuestions()) * 100
                    : 0.0;

            result.add(AudioReflexLeaderboardResponse.builder()
                    .rank(rank++)
                    .userId(String.valueOf(r.getUser().getId()))
                    .userName(name)
                    .userAvatar(avatar)
                    .score(r.getScore())
                    .maxCombo(r.getMaxCombo())
                    .accuracy(Math.round(accuracy * 10.0) / 10.0)
                    .playedAt(r.getCreatedAt())
                    .build());
        }
        return result;
    }
}