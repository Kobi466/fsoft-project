package com.fivetuat.fsoftprojectserver.services.support;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckShareRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckValidator {

    DeckShareRepository deckShareRepository;
    DeckRepository deckRepository;

    public boolean isDeckOwner(Deck deck, UUID profileId) {
        return deck.getProfile().getId().equals(profileId);
    }

    public void verifyOwner(Deck deck, UUID profileId) {
        if (!isDeckOwner(deck, profileId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void verifyDeckReadPermission(Deck deck, UUID profileId) {
        boolean isOwner = isDeckOwner(deck, profileId);
        boolean isShared = deckShareRepository.existsByDeckIdAndProfileId(deck.getId(), profileId);
        if (!isOwner && !isShared) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void validateForkEligibility(Deck deck, UUID profileId) {
        if (isDeckOwner(deck, profileId)) {
            throw new AppException(ErrorCode.CANNOT_FORK_OWN_DECK);
        }

        if (deck.getVisibility() != DeckVisibility.PUBLIC) {
            throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
        }

        if (deckRepository.existsByParentDeckIdAndProfileIdAndIsDeletedFalse(deck.getId(), profileId)) {
            throw new AppException(ErrorCode.DECK_ALREADY_FORKED);
        }
    }
}
