package com.fivetuat.fsoftprojectserver.services.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.response.CrammingCardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CrammingQueueResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardSrsRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckCrammingHelper {

    CardRepository cardRepository;
    UserCardSrsRepository userCardSrsRepository;
    AuthService authService;
    CardMapper cardMapper;

    @Transactional(readOnly = true)
    public CrammingQueueResponse getCramCards(Deck deck, Boolean shuffle, String filter, Integer limit) {
        List<Card> allCards = cardRepository.findByDeckIdOrderByPositionAsc(deck.getId());
        Map<Long, UserCardSrs> srsMap = fetchUserSrsMap(deck.getId());

        List<CrammingCardResponse> crammingCards = filterAndMapCrammingCards(allCards, srsMap, filter);

        if (Boolean.TRUE.equals(shuffle)) {
            Collections.shuffle(crammingCards);
        }

        if (limit != null && limit > 0 && crammingCards.size() > limit) {
            crammingCards = new ArrayList<>(crammingCards.subList(0, limit));
        }

        return CrammingQueueResponse.builder()
                .deckId(deck.getId())
                .deckTitle(deck.getTitle())
                .totalCards(crammingCards.size())
                .cards(crammingCards)
                .build();
    }

    private Map<Long, UserCardSrs> fetchUserSrsMap(Long deckId) {
        Map<Long, UserCardSrs> srsMap = new HashMap<>();
        try {
            Profile currentProfile = authService.getCurrentProfile();
            if (currentProfile != null) {
                List<UserCardSrs> srsList = userCardSrsRepository.findByProfileIdAndCardDeckId(currentProfile.getId(), deckId);
                for (UserCardSrs srs : srsList) {
                    srsMap.put(srs.getCard().getId(), srs);
                }
            }
        } catch (Exception e) {
            log.debug("No authenticated profile found for cramming mode");
        }
        return srsMap;
    }

    private List<CrammingCardResponse> filterAndMapCrammingCards(List<Card> allCards, Map<Long, UserCardSrs> srsMap, String filter) {
        List<CrammingCardResponse> crammingCards = new ArrayList<>();
        for (Card card : allCards) {
            UserCardSrs srs = srsMap.get(card.getId());
            boolean isLearned = (srs != null && srs.getStatus() != SrsStatus.NEW);
            SrsStatus status = (srs != null) ? srs.getStatus() : SrsStatus.NEW;

            if (filter != null) {
                if ("LEARNED".equalsIgnoreCase(filter) && !isLearned) continue;
                if ("UNLEARNED".equalsIgnoreCase(filter) && isLearned) continue;
            }

            crammingCards.add(CrammingCardResponse.builder()
                    .card(cardMapper.toCardResponse(card))
                    .srsStatus(status)
                    .build());
        }
        return crammingCards;
    }
}
