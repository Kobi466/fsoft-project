package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardStarToggleResponse;
import org.springframework.data.domain.Pageable;

public interface CardStarService {
    CardStarToggleResponse toggleStar(Long cardId);
    PageResponse<CardResponse> getStarredCards(Long deckId, Pageable pageable);
}
