package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardStarToggleResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.UserCardStar;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardStarRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.CardStarService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CardStarServiceImpl implements CardStarService {

    AuthService authService;
    CardRepository cardRepository;
    UserCardStarRepository userCardStarRepository;
    CardMapper cardMapper;

    @Override
    @Transactional
    public CardStarToggleResponse toggleStar(Long cardId) {
        Profile profile = authService.getCurrentProfile();

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_FOUND));

        Optional<UserCardStar> existingStar = userCardStarRepository.findByProfileIdAndCardId(profile.getId(), cardId);

        boolean isStarred;
        if (existingStar.isPresent()) {
            userCardStarRepository.delete(existingStar.get());
            isStarred = false;
            log.info("Profile {} unstarred card {}", profile.getId(), cardId);
        } else {
            UserCardStar star = UserCardStar.builder()
                    .profile(profile)
                    .card(card)
                    .build();
            userCardStarRepository.save(star);
            isStarred = true;
            log.info("Profile {} starred card {}", profile.getId(), cardId);
        }

        return CardStarToggleResponse.builder()
                .cardId(cardId)
                .isStarred(isStarred)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CardResponse> getStarredCards(Long deckId, Pageable pageable) {
        Profile profile = authService.getCurrentProfile();

        Page<Card> starredCardsPage = userCardStarRepository.findStarredCardsByProfileId(
                profile.getId(), deckId, pageable
        );

        return PageResponse.<CardResponse>builder()
                .content(starredCardsPage.getContent().stream().map(cardMapper::toCardResponse).toList())
                .pageNo(starredCardsPage.getNumber())
                .pageSize(starredCardsPage.getSize())
                .totalElements(starredCardsPage.getTotalElements())
                .totalPages(starredCardsPage.getTotalPages())
                .build();
    }
}
