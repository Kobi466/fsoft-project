package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateDeckOfficialRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface AdminDeckService {
    DeckResponse createOfficialDeck(DeckCreateRequest request, MultipartFile coverImage);
    PageResponse<DeckResponse> getAllDecks(String keyword, Boolean isOfficial, DeckVisibility visibility, Pageable pageable);
    DeckResponse updateDeckOfficial(Long deckId, AdminUpdateDeckOfficialRequest request);
    void deleteDeck(Long deckId);
    PageResponse<DeckResponse> getPendingPublicDecks(Pageable pageable);
    DeckResponse approvePublicDeck(Long deckId, boolean approved);
    DeckResponse convertDeckVisibility(Long deckId, DeckVisibility visibility);
}

