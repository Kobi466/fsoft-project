package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateDeckOfficialRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.services.AdminDeckService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import org.springframework.cache.annotation.CacheEvict;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import com.fivetuat.fsoftprojectserver.utils.PageUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDeckServiceImpl implements AdminDeckService {

    DeckRepository deckRepository;
    DeckMapper deckMapper;
    DeckService deckService;

    @Override
    @Transactional
    @CacheEvict(value = {"public_decks", "admin_dashboard_stats"}, allEntries = true)
    public DeckResponse createOfficialDeck(DeckCreateRequest request, MultipartFile coverImage) {
//        request.setOfficial(true);
//        request.setVisibility(DeckVisibility.PUBLIC);
        return deckService.createDeck(request, coverImage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getAllDecks(String keyword, Boolean isOfficial, DeckVisibility visibility, Pageable pageable) {
        Page<Deck> deckPage = deckRepository.findAll(pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public DeckResponse updateDeckOfficial(Long deckId, AdminUpdateDeckOfficialRequest request) {
        Deck deck = this.getExistingDeck(deckId);
        deck.setOfficial(Boolean.TRUE.equals(request.getIsOfficial()));
        Deck savedDeck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(savedDeck);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks", "deck_cards", "card_detail", "admin_dashboard_stats"}, allEntries = true)
    public void deleteDeck(Long deckId) {
        Deck deck = this.getExistingDeck(deckId);
        deck.setDeleted(true);
        deckRepository.save(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getPendingPublicDecks(Pageable pageable) {
        Page<Deck> deckPage = deckRepository.findByPendingPublicTrueAndIsDeletedFalse(pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks", "admin_dashboard_stats"}, allEntries = true)
    public DeckResponse approvePublicDeck(Long deckId, boolean approved) {
        Deck deck = this.getExistingDeck(deckId);
        if (approved) {
            deck.setVisibility(DeckVisibility.PUBLIC);
        } else {
            deck.setVisibility(DeckVisibility.PRIVATE);
        }
        deck.setPendingPublic(false);
        Deck savedDeck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(savedDeck);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public DeckResponse convertDeckVisibility(Long deckId, DeckVisibility visibility) {
        Deck deck = this.getExistingDeck(deckId);
        deck.setVisibility(visibility);
        Deck savedDeck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(savedDeck);
    }

    private Deck getExistingDeck(Long deckId) {
        return deckRepository.findByIdAndIsDeletedFalse(deckId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }
}
