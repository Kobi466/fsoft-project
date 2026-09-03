package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.Instant;
import java.time.LocalDate;
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
import com.fivetuat.fsoftprojectserver.enums.ActivityType;
import com.fivetuat.fsoftprojectserver.services.AudioReflexGameService;
import com.fivetuat.fsoftprojectserver.services.ProgressService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AudioReflexGameServiceImpl implements AudioReflexGameService {

    DeckRepository deckRepository;
    CardRepository cardRepository;
    GameRecordRepository gameRecordRepository;
    AuthRepository authRepository;
    ProgressService progressService;

    private static final String GAME_TYPE_AUDIO_REFLEX = "AUDIO_REFLEX";

    @Override
    @Transactional(readOnly = true)
    public AudioReflexGameResponse generateGame(Long deckId, int questionCount) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        List<Card> allCards = cardRepository.findByDeckIdOrderByPositionAsc(deckId);

        if (allCards.size() < 4) {
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }

        List<Card> pool = new ArrayList<>(allCards);
        Collections.shuffle(pool);

        int count = Math.min(questionCount, pool.size());
        List<Card> selectedTargetCards = pool.subList(0, count);

        List<AudioReflexGameResponse.AudioQuestionItem> questions = new ArrayList<>();

        for (Card targetCard : selectedTargetCards) {
            List<AudioReflexGameResponse.OptionItem> options = new ArrayList<>();
            options.add(new AudioReflexGameResponse.OptionItem(targetCard.getId(), targetCard.getMeaning()));

            List<Card> distractors = new ArrayList<>(allCards);
            distractors.removeIf(c -> c.getId().equals(targetCard.getId()));
            Collections.shuffle(distractors);

            for (int i = 0; i < Math.min(3, distractors.size()); i++) {
                Card distractorCard = distractors.get(i);
                options.add(new AudioReflexGameResponse.OptionItem(distractorCard.getId(), distractorCard.getMeaning()));
            }

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
    public AudioReflexLeaderboardResponse submitResult(AudioReflexSubmitRequest request, String currentUserId) {
        UUID userUuid;
        try {
            userUuid = UUID.fromString(currentUserId);
        } catch (Exception e) {
            throw new AppException(ErrorCode.AUTHENTICATION_FAILED);
        }

        Auth user = authRepository.findById(userUuid)
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));

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

        if (user.getProfile() != null) {
            progressService.updateUserActivityAndStreak(user.getProfile(), LocalDate.now(), ActivityType.GAME_PLAYED);
        }

        String name = (user.getProfile() != null && user.getProfile().getFullName() != null)
                ? user.getProfile().getFullName()
                : user.getEmail();

        String avatar = user.getProfile() != null ? user.getProfile().getAvatar() : null;

        double accuracy = request.getTotalQuestions() != null && request.getTotalQuestions() > 0
                ? ((double) request.getCorrectAnswers() / request.getTotalQuestions()) * 100
                : 0.0;

        return AudioReflexLeaderboardResponse.builder()
                .rank(1)
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
