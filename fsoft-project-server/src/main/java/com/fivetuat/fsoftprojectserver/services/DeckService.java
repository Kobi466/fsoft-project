package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


public interface DeckService {
    DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage);
    DeckResponse getDeck(Long id);
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, MultipartFile coverImage);
    void deleteDeck(Long id);
    PageResponse<DeckResponse> getMyDecks(Pageable pageable);
    PageResponse<DeckResponse> getAllDecks(String tag, Pageable pageable);

    Deck getMyDeck(Long id);
    void save(Deck deck);
}
