package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.entities.*;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.*;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckServiceImpl implements DeckService {
    DeckRepository deckRepository;
    AuthService authService;
    TagRepository tagRepository;
    DeckMapper deckMapper;
    UploadService uploadService;
    CardRepository cardRepository;
    ProfileRepository profileRepository;
    DeckShareRepository deckShareRepository;

    @Transactional
    @Override
    public DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage) {
        Profile profile = authService.getCurrentProfile();

        Deck deck = deckMapper.toDeck(request);
        deck.setProfile(profile);

        if (request.getParentDeckId() != null) {
            Deck parent = deckRepository.findByIdAndIsDeletedFalse(request.getParentDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
            deck.setParentDeck(parent);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String imageUrl = uploadService.uploadFile(coverImage);
            deck.setCoverImageUrl(imageUrl);
        }

        deck.setTags(processTags(request.getTagIds()));

        deck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public DeckResponse getDeck(Long id) {
        Deck deck = deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        if (deck.getVisibility() == DeckVisibility.PRIVATE) {
            Profile currentProfile = authService.getCurrentProfile();
            if (!deck.getProfile().getId().equals(currentProfile.getId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return deckMapper.toDeckResponse(deck);
    }

    @Transactional
    @Override
    public DeckResponse updateDeck(Long id, DeckUpdateRequest request, MultipartFile coverImage) {
        Deck deck = deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        Profile currentProfile = authService.getCurrentProfile();
        if (!deck.getProfile().getId().equals(currentProfile.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        deckMapper.updateDeck(deck, request);

        if (request.getParentDeckId() != null) {
            Deck parent = deckRepository.findByIdAndIsDeletedFalse(request.getParentDeckId())
                    .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
            deck.setParentDeck(parent);
        } else {
            deck.setParentDeck(null);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String imageUrl = uploadService.uploadFile(coverImage);
            deck.setCoverImageUrl(imageUrl);
        }

        deck.setTags(processTags(request.getTagIds()));

        deck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(deck);
    }

    @Transactional
    @Override
    public void convertDeckVisibility(Long id, DeckVisibility deckVisibility) {
        Deck deck = deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
        Profile currentProfile = authService.getCurrentProfile();

        if (!deck.getProfile().getId().equals(currentProfile.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        deck.setVisibility(deckVisibility);
        deckRepository.save(deck);
    }

    @Override
    public void deleteDeck(Long id) {
        Deck deck = deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        Profile currentProfile = authService.getCurrentProfile();
        if (!deck.getProfile().getId().equals(currentProfile.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        deck.setDeleted(true);
        deckRepository.save(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getMyDecks(int page, int size) {
        Profile profile = authService.getCurrentProfile();
        int pageNo = Math.max(1, page);
        Pageable pageable = PageRequest.of(pageNo - 1, size);
        Page<Deck> deckPage=deckRepository.findByProfileAndIsDeletedFalse(profile, pageable);
        List<DeckResponse> content = deckPage.getContent().stream()
                .map(deckMapper::toDeckResponse)
                .toList();
        return PageResponse.<DeckResponse>builder()
                .pageNo(pageNo)
                .pageSize(size)
                .totalPages(deckPage.getTotalPages())
                .totalElements(deckPage.getTotalElements())
                .content(content)
                .last(deckPage.isLast())
                .build();
    }

    @Override
    public List<DeckResponse> getAllDecks() {
        List<Deck> decks = deckRepository.findByIsDeletedFalse();
        return decks.stream()
                .map(deckMapper::toDeckResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public DeckResponse fork(Long id) {
        Profile currentProfile = authService.getCurrentProfile();
        Deck deck = deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        if (deck.getVisibility() != DeckVisibility.PUBLIC) {
            throw new AppException(ErrorCode.DECK_NOT_PUBLIC);
        }

        if (deckRepository.existsByParentDeckIdAndProfileIdAndIsDeletedFalse(id, currentProfile.getId())) {
            throw new AppException(ErrorCode.DECK_ALREADY_FORKED);
        }

        deck.setForkCount(deck.getForkCount() + 1);
        deckRepository.save(deck);

        Deck newDeck = new Deck(deck);
        newDeck.setProfile(currentProfile);
        newDeck.setForkCount(0);
        newDeck.setParentDeck(deck);
        newDeck.setVisibility(DeckVisibility.PRIVATE);
        newDeck.setOfficial(false);

        if (deck.getTags() != null && !deck.getTags().isEmpty()) {
            newDeck.setTags(new java.util.HashSet<>(deck.getTags()));
        }

        Deck savedDeck = deckRepository.save(newDeck);

        List<Card> cards = cardRepository.findByDeckId(deck.getId());
        if (cards != null && !cards.isEmpty()) {
            List<Card> copiedCards = cards.stream()
                    .map(card -> {
                        Card newCard = new Card(card);
                        newCard.setDeckId(savedDeck.getId());
                        return newCard;
                    }).collect(Collectors.toList());
            cardRepository.saveAll(copiedCards);
        }

        return deckMapper.toDeckResponse(savedDeck);
    }

    @Transactional
    @Override
    public void share(Long deckId, ShareDeckRequest request) {
        Profile owner = authService.getCurrentProfile();

        Deck deck = deckRepository.findByIdAndIsDeletedFalse(deckId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        if (!deck.getProfile().getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (request.getProfileId() == null || request.getProfileId().equals(owner.getId())) {
            return;
        }

        if (deckShareRepository.existsByDeckIdAndProfileId(deck.getId(), request.getProfileId())) {
            throw new AppException(ErrorCode.DECK_ALREADY_SHARED);
        }

        deck.setVisibility(DeckVisibility.SHARED);
        deckRepository.save(deck);

        Profile targetProfile = profileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        DeckShare shareToSave = DeckShare.builder()
                .deck(deck)
                .profile(targetProfile)
                .permission(request.getPermission())
                .build();

        deckShareRepository.save(shareToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getPublicDecks(int page, int size, String keyword, Long tagId, String sourceLang, String targetLang) {
        int pageNo = Math.max(1, page);
        Pageable pageable = PageRequest.of(pageNo - 1, size);

        String sanitizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String sanitizedSourceLang = (sourceLang != null && !sourceLang.trim().isEmpty()) ? sourceLang.trim() : null;
        String sanitizedTargetLang = (targetLang != null && !targetLang.trim().isEmpty()) ? targetLang.trim() : null;

        Page<Deck> deckPage = deckRepository.searchPublicDecks(sanitizedKeyword, tagId, sanitizedSourceLang, sanitizedTargetLang, pageable);

        List<DeckResponse> content = deckPage.getContent().stream()
                .map(deckMapper::toDeckResponse)
                .toList();

        return PageResponse.<DeckResponse>builder()
                .pageNo(pageNo)
                .pageSize(size)
                .totalPages(deckPage.getTotalPages())
                .totalElements(deckPage.getTotalElements())
                .content(content)
                .last(deckPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getDecksByStatus(DeckVisibility status, int page, int size) {
        int pageNo = Math.max(1, page);
        Pageable pageable = PageRequest.of(pageNo - 1, size);

        DeckVisibility targetStatus = (status != null) ? status : DeckVisibility.PUBLIC;
        Page<Deck> deckPage;

        switch (targetStatus) {
            case PRIVATE -> {
                Profile profile = authService.getCurrentProfile();
                deckPage = deckRepository.findByProfileAndVisibilityAndIsDeletedFalse(profile, DeckVisibility.PRIVATE, pageable);
            }
            case SHARED -> {
                Profile profile = authService.getCurrentProfile();
                deckPage = deckRepository.findSharedDecksByProfile(profile, pageable);
            }
            default -> {
                deckPage = deckRepository.findByVisibilityAndIsDeletedFalse(DeckVisibility.PUBLIC, pageable);
            }
        }

        List<DeckResponse> content = deckPage.getContent().stream()
                .map(deckMapper::toDeckResponse)
                .toList();

        return PageResponse.<DeckResponse>builder()
                .pageNo(pageNo)
                .pageSize(size)
                .totalPages(deckPage.getTotalPages())
                .totalElements(deckPage.getTotalElements())
                .content(content)
                .last(deckPage.isLast())
                .build();
    }

    private Set<Tag> processTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        return tagIds.stream()
                .map(id -> tagRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)))
                .collect(Collectors.toSet());
    }
}
