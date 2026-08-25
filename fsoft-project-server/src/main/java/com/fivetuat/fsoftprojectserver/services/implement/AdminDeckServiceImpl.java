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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminDeckServiceImpl implements AdminDeckService {

    DeckRepository deckRepository;
    DeckMapper deckMapper;
    DeckService deckService;

    @Override
    @Transactional
    public DeckResponse createOfficialDeck(DeckCreateRequest request, MultipartFile coverImage) {
        request.setOfficial(true);
        request.setVisibility(DeckVisibility.PUBLIC);
        return deckService.createDeck(request, coverImage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getAllDecks(String keyword, Boolean isOfficial, DeckVisibility visibility, Pageable pageable) {
        Page<Deck> deckPage = deckRepository.findAll(pageable);

        List<DeckResponse> content = deckPage.getContent().stream()
                .map(deckMapper::toDeckResponse)
                .collect(Collectors.toList());

        return PageResponse.<DeckResponse>builder()
                .content(content)
                .totalElements(deckPage.getTotalElements())
                .totalPages(deckPage.getTotalPages())
                .pageNo(deckPage.getNumber())
                .pageSize(deckPage.getSize())
                .last(deckPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public DeckResponse updateDeckOfficial(Long deckId, AdminUpdateDeckOfficialRequest request) {
        Deck deck = this.getExistingDeck(deckId);
        deck.setOfficial(Boolean.TRUE.equals(request.getIsOfficial()));
        Deck savedDeck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(savedDeck);
    }

    @Override
    @Transactional
    public void deleteDeck(Long deckId) {
        Deck deck = this.getExistingDeck(deckId);
        deck.setDeleted(true);
        deckRepository.save(deck);
    }


    private Deck getExistingDeck(Long deckId) {
        return deckRepository.findByIdAndIsDeletedFalse(deckId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }
}
