package com.fivetuat.fsoftprojectserver.services.support;

import java.util.*;

import org.springframework.stereotype.Component;

import com.fivetuat.fsoftprojectserver.dtos.game.PlayerState;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MultiplayerRoomHelper {

    CardRepository cardRepository;

    /**
     * Tính tổng số từ vựng không trùng lặp (Unique Words) trong tất cả các bộ thẻ của phòng chơi
     */
    public int countTotalAvailableUniqueCards(Long hostDeckId, Collection<PlayerState> players) {
        List<Long> deckIds = new ArrayList<>();
        if (hostDeckId != null) {
            deckIds.add(hostDeckId);
        }
        if (players != null) {
            for (PlayerState p : players) {
                if (p.getSelectedDeckId() != null && !deckIds.contains(p.getSelectedDeckId())) {
                    deckIds.add(p.getSelectedDeckId());
                }
            }
        }
        if (deckIds.isEmpty()) return 0;

        List<Card> allCards = cardRepository.findByDeckIdIn(deckIds);
        Set<String> uniqueWords = new HashSet<>();
        for (Card card : allCards) {
            if (card.getWord() != null && !card.getWord().trim().isEmpty()) {
                uniqueWords.add(card.getWord().trim().toLowerCase());
            }
        }
        return uniqueWords.size();
    }

    /**
     * Kiểm tra tính hợp lệ và trả về số lượng câu hỏi đã cập nhật
     */
    public int resolveQuestionCount(Long hostDeckId, Collection<PlayerState> players, int currentQuestionCount, Integer newQuestionCount) {
        int totalAvailable = countTotalAvailableUniqueCards(hostDeckId, players);
        if (newQuestionCount != null && newQuestionCount > 0) {
            return Math.min(newQuestionCount, totalAvailable);
        }
        return Math.min(currentQuestionCount, totalAvailable);
    }
}
