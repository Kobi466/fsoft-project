package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import com.fivetuat.fsoftprojectserver.services.UploadService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckServiceImpl implements DeckService {
    DeckRepository deckRepository;
    AuthService authService;
    TagRepository tagRepository;
    DeckMapper deckMapper;
    UploadService uploadService;

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
    public List<DeckResponse> getMyDecks() {
        Profile profile = authService.getCurrentProfile();
        List<Deck> decks = deckRepository.findByProfileIdAndIsDeletedFalse(profile.getId());
        return decks.stream()
                .map(deckMapper::toDeckResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeckResponse> getAllDecks(String tag) {
        List<Deck> decks;
        if (tag != null && !tag.trim().isEmpty()) {
            decks = deckRepository.findByTagsSlugAndIsDeletedFalseAndVisibility(tag.trim(), DeckVisibility.PUBLIC);
        } else {
            decks = deckRepository.findByIsDeletedFalse();
        }
        return decks.stream()
                .map(deckMapper::toDeckResponse)
                .collect(Collectors.toList());
    }

    private Set<Tag> processTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new java.util.HashSet<>();
        }
        return tagIds.stream()
                .map(id -> tagRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)))
                .collect(Collectors.toSet());
    }
}