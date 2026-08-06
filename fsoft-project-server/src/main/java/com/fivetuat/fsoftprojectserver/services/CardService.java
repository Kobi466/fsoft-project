package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;

import java.util.List;

public interface CardService {

    CardResponse createCard(CardCreationRequest request);

    CardResponse getCardById(Long cardId);

    PageResponse<CardResponse> getCardsByDeckId(Long deckId, int page, int size, String keyword, String sortBy, String sortDirection);

    List<CardResponse> getAllCardsInDeck(Long deckId);

    CardResponse updateCard(Long cardId, CardUpdateRequest request);

    void deleteCard(Long cardId);

    CardResponse updateCardPosition(Long cardId, Integer newPosition);
}