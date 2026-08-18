package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.DeckShare;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DeckService {
    DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage);
    DeckResponse getDeck(Long id);
    DeckResponse updateDeck(Long id, DeckUpdateRequest request, MultipartFile coverImage);
    void convertDeckVisibility(Long id, DeckVisibility deckVisibility);
    void deleteDeck(Long id);
    PageResponse<DeckResponse> getMyDecks(int page, int size);
    DeckResponse fork(Long id);
    void share(Long deckId, ShareDeckRequest request);
    PageResponse<DeckResponse> getPublicDecks(int page, int size, String keyword, Long tagId, String sourceLang, String targetLang);
    PageResponse<DeckResponse> getDecksByStatus(DeckVisibility status, int page, int size);
    Deck getMyDeck(Long id);
    void save(Deck deck);

    PageResponse<DeckShareResponse> getSharedUsers(Long deckId, Pageable pageable);
    void updateSharePermission(Long deckId, UUID profileId, SharePermission permission);
    void revokeShare(Long deckId, UUID profileId);

    DeckResponse toggleShareLink(Long deckId);
    DeckResponse getDeckByShareToken(String shareToken);
    PageResponse<DeckResponse> getSharedWithMeDecks(int page, int size);
}
