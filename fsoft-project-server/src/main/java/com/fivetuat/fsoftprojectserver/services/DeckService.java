package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DeckService {
    DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage);
    DeckResponse getDeck(Long id);
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, MultipartFile coverImage);
    void deleteDeck(Long id);
    List<DeckResponse> getMyDecks();
    List<DeckResponse> getAllDecks(String tag);
}
