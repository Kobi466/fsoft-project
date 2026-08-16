package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.CardAiResponse;

import java.time.Instant;

public interface InternalCardService {
    PageResponse<CardAiResponse> getChangedCards(Instant since, int page, int size);
    PageResponse<Long> getCardIds(int page, int size);
}
