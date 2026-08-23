package com.fivetuat.fsoftprojectserver.services.implement;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.dtos.game.PlayerState;
import com.fivetuat.fsoftprojectserver.dtos.game.SpaceStrikerRoomState;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse.AudioQuestionItem;
import com.fivetuat.fsoftprojectserver.dtos.response.AudioReflexGameResponse.OptionItem;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.GameRecord;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.RoomStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.*;
import com.fivetuat.fsoftprojectserver.services.SpaceStrikerRoomService;
import com.fivetuat.fsoftprojectserver.services.support.SpaceStrikerQuestionTimer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SpaceStrikerRoomServiceImpl implements SpaceStrikerRoomService {

    @Qualifier("spaceStrikerRoomRedisTemplate")
    RedisTemplate<String, SpaceStrikerRoomState> redis;

    SimpMessagingTemplate messagingTemplate;
    DeckRepository deckRepository;
    CardRepository cardRepository;
    AuthRepository authRepository;
    GameRecordRepository gameRecordRepository;
    SpaceStrikerQuestionTimer questionTimer;

    private static final String KEY_PREFIX = "room:space-striker:";
    private static final String GAME_TYPE_MULTI = "SPACE_STRIKER_MULTI";
    private static final String CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int MAX_PLAYERS = 20;
    private static final int QUESTION_TIME_LIMIT_SECONDS = 15;

    private final Map<String, Object> roomLocks = new ConcurrentHashMap<>();

    private String key(String code) { return KEY_PREFIX + code; }
    private Object lockFor(String roomCode) { return roomLocks.computeIfAbsent(roomCode, k -> new Object()); }

    @Override
    public SpaceStrikerRoomState createRoom(Long deckId, int questionCount, String hostUserId) {
        Deck deck = deckRepository.findById(deckId).orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
        List<Card> deckCards = cardRepository.findByDeckIdOrderByPositionAsc(deckId);
        if (deckCards.size() < 12) {
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }
        Auth host = authRepository.findById(UUID.fromString(hostUserId))
                .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));

        String code = generateUniqueCode();
        PlayerState hostPlayer = toPlayerState(host, true);
        hostPlayer.setSelectedDeckId(deck.getId());
        hostPlayer.setSelectedDeckTitle(deck.getTitle());
        hostPlayer.setSelectedDeckCardCount(deckCards.size());

        SpaceStrikerRoomState state = SpaceStrikerRoomState.builder()
                .roomCode(code)
                .deckId(deckId)
                .hostUserId(hostUserId)
                .status(RoomStatus.WAITING)
                .questionCount(questionCount)
                .questions(new ArrayList<>())
                .currentQuestionIndex(-1)
                .players(new LinkedHashMap<>(Map.of(hostUserId, hostPlayer)))
                .questionResolved(false)
                .build();

        save(state);
        return state;
    }

    @Override
    public SpaceStrikerRoomState selectPlayerDeck(String roomCode, String userId, Long selectedDeckId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);
            if (state.getStatus() != RoomStatus.WAITING) {
                throw new AppException(ErrorCode.ROOM_ALREADY_STARTED);
            }
            PlayerState player = state.getPlayers().get(userId);
            if (player == null) {
                throw new AppException(ErrorCode.PLAYER_NOT_IN_ROOM);
            }

            if (selectedDeckId != null) {
                Deck deck = deckRepository.findById(selectedDeckId)
                        .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
                int cardCount = cardRepository.findByDeckIdOrderByPositionAsc(selectedDeckId).size();
                player.setSelectedDeckId(deck.getId());
                player.setSelectedDeckTitle(deck.getTitle());
                player.setSelectedDeckCardCount(cardCount);
            } else {
                player.setSelectedDeckId(null);
                player.setSelectedDeckTitle(null);
                player.setSelectedDeckCardCount(0);
            }

            save(state);
            broadcast(state, "PLAYER_DECK_SELECTED");
            return state;
        }
    }

    @Override
    public SpaceStrikerRoomState joinRoom(String roomCode, String userId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);

            PlayerState existing = state.getPlayers().get(userId);
            if (existing != null) {
                existing.setConnected(true);
                save(state);
                broadcast(state, "PLAYER_RECONNECTED");
                return state;
            }

            if (state.getStatus() != RoomStatus.WAITING) throw new AppException(ErrorCode.ROOM_ALREADY_STARTED);
            if (state.getPlayers().size() >= MAX_PLAYERS) throw new AppException(ErrorCode.ROOM_FULL);

            Auth user = authRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHENTICATION_FAILED));
            state.getPlayers().put(userId, toPlayerState(user, false));
            save(state);
            broadcast(state, "PLAYER_JOINED");
            return state;
        }
    }

    @Override
    public SpaceStrikerRoomState startGame(String roomCode, String requesterId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);
            if (!state.getHostUserId().equals(requesterId)) throw new AppException(ErrorCode.NOT_ROOM_HOST);
            if (state.getStatus() != RoomStatus.WAITING) throw new AppException(ErrorCode.ROOM_ALREADY_STARTED);
            if (state.getPlayers().size() < 2) throw new AppException(ErrorCode.NOT_ENOUGH_PLAYERS);

            state.setQuestions(generateQuestions(state));
            state.setStatus(RoomStatus.PLAYING);
            state.setCurrentQuestionIndex(0);
            state.setStartedAt(Instant.now().toEpochMilli());
            state.setCurrentQuestionStartedAt(Instant.now().toEpochMilli());
            state.setQuestionResolved(false);
            save(state);
            broadcast(state, "ROOM_STARTED");
            scheduleNextTimerIfNeeded(state);
            return state;
        }
    }

    /**
     * Luật riêng Space Striker: chỉ người BẮN TRÚNG ĐẦU TIÊN mới được điểm và làm chuyển câu ngay.
     * Bắn trượt: không có gì thay đổi. Bắn trúng nhưng câu đã bị "hạ gục" trước đó: bị bỏ qua (chậm chân).
     */
    @Override
    public SpaceStrikerRoomState submitAnswer(String roomCode, String userId, Long cardId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);
            if (state.getStatus() != RoomStatus.PLAYING) throw new AppException(ErrorCode.ROOM_ALREADY_STARTED);
            PlayerState player = state.getPlayers().get(userId);
            if (player == null) throw new AppException(ErrorCode.PLAYER_NOT_IN_ROOM);
            if (player.getLives() <= 0) return state; // đã hết máu, không còn được bắn
            if (state.isQuestionResolved()) return state;

            AudioQuestionItem question = state.getQuestions().get(state.getCurrentQuestionIndex());
            boolean correct = cardId != null && question.getCorrectOptionId() != null
                    && question.getCorrectOptionId().longValue() == cardId.longValue();

            if (!correct) {
                player.setLives(Math.max(0, player.getLives() - 1));
                boolean eliminated = player.getLives() <= 0;
                save(state);
                broadcast(state, eliminated ? "PLAYER_ELIMINATED" : "PLAYER_MISSED");
                if (eliminated && allPlayersEliminated(state)) {
                    finishRoomEarly(state);
                }
                return state;
            }

            state.setQuestionResolved(true);
            long elapsedMs = Instant.now().toEpochMilli() - state.getCurrentQuestionStartedAt();
            long speedBonus = Math.max(0, 500 - elapsedMs / 20);
            int points = (int) (500 + speedBonus);
            player.setScore(player.getScore() + points);
            player.setCorrectCount(player.getCorrectCount() + 1);
            player.setLastAnswerAt(Instant.now().toEpochMilli());

            boolean finished = advanceQuestionOrFinish(state);
            String eventType = finished ? "ROOM_FINISHED" : "NEXT_QUESTION";
            save(state);
            broadcast(state, eventType);
            if (!finished) scheduleNextTimerIfNeeded(state);
            return state;
        }
    }
    private boolean advanceQuestionOrFinish(SpaceStrikerRoomState state) {
        int next = state.getCurrentQuestionIndex() + 1;
        if (next >= state.getQuestions().size()) {
            state.setStatus(RoomStatus.FINISHED);
            state.setCurrentQuestionIndex(next);
            persistGameRecords(state);
            return true;
        }
        state.setCurrentQuestionIndex(next);
        state.setCurrentQuestionStartedAt(Instant.now().toEpochMilli());
        state.setQuestionResolved(false);
        return false;
    }
    private boolean allPlayersEliminated(SpaceStrikerRoomState state) {
        return state.getPlayers().values().stream().allMatch(p -> p.getLives() <= 0);
    }

    private void finishRoomEarly(SpaceStrikerRoomState state) {
        state.setStatus(RoomStatus.FINISHED);
        persistGameRecords(state);
        save(state);
        broadcast(state, "ROOM_FINISHED");
        questionTimer.cancel(state.getRoomCode());
    }


    private void handleQuestionTimeout(String roomCode, int questionIndexAtSchedule) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state;
            try {
                state = getRoomOrThrow(roomCode);
            } catch (AppException e) {
                return;
            }
            if (state.getStatus() != RoomStatus.PLAYING) return;
            if (state.getCurrentQuestionIndex() != questionIndexAtSchedule) return;
            if (state.isQuestionResolved()) return;

            boolean finished = advanceQuestionOrFinish(state);
            save(state);
            broadcast(state, finished ? "ROOM_FINISHED" : "QUESTION_TIMEOUT_NEXT");
            scheduleNextTimerIfNeeded(state);
        }
    }

    private void scheduleNextTimerIfNeeded(SpaceStrikerRoomState state) {
        if (state.getStatus() == RoomStatus.PLAYING) {
            int idx = state.getCurrentQuestionIndex();
            questionTimer.schedule(state.getRoomCode(),
                    () -> handleQuestionTimeout(state.getRoomCode(), idx),
                    QUESTION_TIME_LIMIT_SECONDS);
        } else {
            questionTimer.cancel(state.getRoomCode());
        }
    }

    private void persistGameRecords(SpaceStrikerRoomState state) {
        Deck deck = deckRepository.findById(state.getDeckId()).orElse(null);
        if (deck == null) return;
        for (PlayerState p : state.getPlayers().values()) {
            Auth user = authRepository.findById(UUID.fromString(p.getUserId())).orElse(null);
            if (user == null) continue;
            gameRecordRepository.save(GameRecord.builder()
                    .user(user)
                    .deck(deck)
                    .gameType(GAME_TYPE_MULTI)
                    .score(p.getScore())
                    .correctAnswers(p.getCorrectCount())
                    .totalQuestions(state.getQuestions().size())
                    .build());
        }
    }

    @Override
    public SpaceStrikerRoomState leaveRoom(String roomCode, String userId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);
            state.getPlayers().remove(userId);
            if (state.getPlayers().isEmpty()) {
                redis.delete(key(roomCode));
                questionTimer.cancel(roomCode);
                roomLocks.remove(roomCode);
                return state;
            }
            if (state.getHostUserId().equals(userId)) {
                String newHostId = state.getPlayers().keySet().iterator().next();
                state.setHostUserId(newHostId);
                state.getPlayers().get(newHostId).setHost(true);
            }
            save(state);
            broadcast(state, "PLAYER_LEFT");
            return state;
        }
    }

    @Override
    public SpaceStrikerRoomState getRoom(String roomCode) {
        return getRoomOrThrow(roomCode);
    }

    @Override
    public SpaceStrikerRoomState markDisconnected(String roomCode, String userId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state;
            try {
                state = getRoomOrThrow(roomCode);
            } catch (AppException e) {
                return null;
            }
            PlayerState player = state.getPlayers().get(userId);
            if (player == null) return state;

            player.setConnected(false);
            save(state);
            broadcast(state, "PLAYER_DISCONNECTED");
            return state;
        }
    }

    @Override
    public SpaceStrikerRoomState markReconnected(String roomCode, String userId) {
        synchronized (lockFor(roomCode)) {
            SpaceStrikerRoomState state = getRoomOrThrow(roomCode);
            PlayerState player = state.getPlayers().get(userId);
            if (player == null) throw new AppException(ErrorCode.PLAYER_NOT_IN_ROOM);

            player.setConnected(true);
            save(state);
            broadcast(state, "PLAYER_RECONNECTED");
            return state;
        }
    }

    // ---- helpers ----

    private SpaceStrikerRoomState getRoomOrThrow(String code) {
        SpaceStrikerRoomState state = redis.opsForValue().get(key(code));
        if (state == null) throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        return state;
    }

    private void save(SpaceStrikerRoomState state) {
        redis.opsForValue().set(key(state.getRoomCode()), state, Duration.ofHours(2));
    }

    private void broadcast(SpaceStrikerRoomState state, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", eventType);
        payload.put("timeLimitSeconds", QUESTION_TIME_LIMIT_SECONDS);
        payload.put("room", state);
        messagingTemplate.convertAndSend("/topic/space-room/" + state.getRoomCode(), (Object) payload);
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 6; i++) sb.append(CHARS.charAt(ThreadLocalRandom.current().nextInt(CHARS.length())));
            code = sb.toString();
        } while (Boolean.TRUE.equals(redis.hasKey(key(code))));
        return code;
    }

    private PlayerState toPlayerState(Auth user, boolean host) {
        return PlayerState.builder()
                .userId(String.valueOf(user.getId()))
                .displayName(user.getProfile() != null ? user.getProfile().getFullName() : user.getEmail())
                .avatar(user.getProfile() != null ? user.getProfile().getAvatar() : null)
                .host(host)
                .score(0)
                .correctCount(0)
                .build();
    }

    /** Sinh câu hỏi bằng cách gộp bộ thẻ gốc của Host và bộ thẻ cá nhân đóng góp của các người chơi. */
    private List<AudioQuestionItem> generateQuestions(SpaceStrikerRoomState state) {
        List<Long> deckIds = new ArrayList<>();
        deckIds.add(state.getDeckId());

        for (PlayerState p : state.getPlayers().values()) {
            if (p.getSelectedDeckId() != null && !deckIds.contains(p.getSelectedDeckId())) {
                deckIds.add(p.getSelectedDeckId());
            }
        }

        List<Card> allCards = cardRepository.findByDeckIdIn(deckIds);
        if (allCards.isEmpty()) {
            throw new AppException(ErrorCode.INSUFFICIENT_CARDS);
        }

        Map<String, Card> uniqueCardsMap = new LinkedHashMap<>();
        for (Card card : allCards) {
            if (card.getWord() != null && !card.getWord().trim().isEmpty()) {
                uniqueCardsMap.putIfAbsent(card.getWord().trim().toLowerCase(), card);
            }
        }
        List<Card> pool = new ArrayList<>(uniqueCardsMap.values());
        Collections.shuffle(pool);

        int count = Math.min(state.getQuestionCount(), pool.size());

        List<AudioQuestionItem> questions = new ArrayList<>();
        for (Card target : pool.subList(0, count)) {
            List<OptionItem> options = new ArrayList<>();
            options.add(OptionItem.builder().optionId(target.getId()).text(target.getMeaning()).build());

            List<Card> distractors = new ArrayList<>(pool);
            distractors.removeIf(c -> c.getId().equals(target.getId()));
            Collections.shuffle(distractors);
            for (int i = 0; i < Math.min(3, distractors.size()); i++) {
                Card d = distractors.get(i);
                options.add(OptionItem.builder().optionId(d.getId()).text(d.getMeaning()).build());
            }
            Collections.shuffle(options);

            questions.add(AudioQuestionItem.builder()
                    .cardId(target.getId())
                    .word(target.getWord())
                    .phonetic(target.getPhonetic())
                    .imageUrl(target.getImageUrl())
                    .exampleSentence(target.getExampleSentence())
                    .exampleMeaning(target.getExampleMeaning())
                    .correctOptionId(target.getId())
                    .options(options)
                    .build());
        }
        return questions;
    }
}