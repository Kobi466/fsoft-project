package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.CardCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardPositionRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.CardUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CardResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CardService {

    CardResponse createCard(
            Long deckId,
            CardCreationRequest request,
            MultipartFile imageFile,
            MultipartFile audioFile
    );
    CardResponse getCardById(Long cardId);
    PageResponse<CardResponse> getCardsByDeckId(Long deckId, Pageable pageable);
    CardResponse updateCard(Long cardId, CardUpdateRequest request, MultipartFile imageFile, MultipartFile audioFile);
    void deleteCard(Long cardId);

    void updateCardPositions(Long deckId, List<CardPositionRequest> cardPositionRequests);

    byte[] exportCardsToExcel(Long deckId);
    byte[] exportCardsToPdf(Long deckId);
}