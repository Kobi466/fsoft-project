package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CrammingQueueResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.entities.Card;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.Tag;
import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import com.fivetuat.fsoftprojectserver.enums.SrsStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.CardMapper;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.CardRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckShareRepository;
import com.fivetuat.fsoftprojectserver.repositories.TagRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserCardSrsRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import com.fivetuat.fsoftprojectserver.services.support.DeckCrammingHelper;
import com.fivetuat.fsoftprojectserver.services.support.DeckShareHelper;
import com.fivetuat.fsoftprojectserver.services.support.DeckValidator;
import com.fivetuat.fsoftprojectserver.utils.PageUtils;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckServiceImpl implements DeckService {

    DeckRepository deckRepository;
    AuthService authService;
    TagRepository tagRepository;
    DeckMapper deckMapper;
    UploadService uploadService;
    CardRepository cardRepository;
    DeckShareRepository deckShareRepository;

    DeckValidator deckValidator;
    DeckShareHelper deckShareHelper;
    DeckCrammingHelper deckCrammingHelper;

    @Transactional
    @Override
    @CacheEvict(value = "public_decks", allEntries = true)
    public DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage) {
        String role = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")
                ? "ADMIN"
                : "USER";

        Profile profile = authService.getCurrentProfile();
        Deck deck = deckMapper.toDeck(request);
        deck.setProfile(profile);
        switch (role) {
            case "ADMIN":
                deck.setOfficial(true);
                break;
            case "USER":
                if (request.getVisibility() == DeckVisibility.PUBLIC) {
                    throw new AppException(ErrorCode.ACCESS_DENIED);
                }
                deck.setOfficial(false);
                break;
            default:
                throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        applyParentDeck(deck, request.getParentDeckId());
        applyCoverImage(deck, coverImage);
        deck.setTags(processTags(request.getTagIds()));
        return deckMapper.toDeckResponse(deckRepository.save(deck));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "deck_detail",
        key = "#id",
        unless = "#result == null || #result.visibility == T(com.fivetuat.fsoftprojectserver.enums.DeckVisibility).PRIVATE"
    )
    public DeckResponse getDeck(Long id) {
        Deck deck = getExistingDeck(id);
        if (deck.getVisibility() == DeckVisibility.PRIVATE) {
            Profile profile = authService.getCurrentProfile();
            deckValidator.verifyDeckReadPermission(deck, profile.getId());
        }
        return deckMapper.toDeckResponse(deck);
    }

    @Transactional
    @Override
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public DeckResponse updateDeck(Long id, DeckUpdateRequest request, MultipartFile coverImage) {
        String role = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN")
                ? "ADMIN"
                : "USER";

        Deck deck;
        boolean pendingModeration = false;
        switch (role) {
            case "ADMIN":
                deck = getExistingDeck(id);
                deck.setVisibility(request.getVisibility());
                break;
            case "USER":
                deck = getMyDeck(id);
                if (request.getVisibility() == DeckVisibility.PUBLIC && !deck.isOfficial()) {
                    if (deck.getParentDeck() != null) {
                        throw new AppException(ErrorCode.FORKED_DECK_CANNOT_BE_PUBLIC);
                    }
                    if (deck.isPendingPublic()) {
                        throw new AppException(ErrorCode.DECK_ALREADY_PENDING_PUBLIC);
                    }
                    pendingModeration = true;
                } else {
                    deck.setVisibility(request.getVisibility());
                }
                request.setOfficial(deck.isOfficial());
                break;
            default:
                throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        deckMapper.updateDeck(deck, request);

        applyParentDeck(deck, request.getParentDeckId());
        applyCoverImage(deck, coverImage);
        deck.setTags(processTags(request.getTagIds()));

        if (pendingModeration) {
            deck.setVisibility(DeckVisibility.PRIVATE);
            deck.setPendingPublic(true);
            deck.setRejectionReason(null);
        } else if (deck.getVisibility() == DeckVisibility.PUBLIC) {
            deck.setPendingPublic(false);
            deck.setRejectionReason(null);
        } else {
            deck.setPendingPublic(false);
        }

        deck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(deck);
    }

    @Transactional
    @Override
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public void convertDeckVisibility(Long id, DeckVisibility deckVisibility) {
        Deck deck = getMyDeck(id);
        if (deckVisibility == DeckVisibility.PUBLIC) {
            if (deck.getParentDeck() != null) {
                throw new AppException(ErrorCode.FORKED_DECK_CANNOT_BE_PUBLIC);
            }
            if (deck.isPendingPublic()) {
                throw new AppException(ErrorCode.DECK_ALREADY_PENDING_PUBLIC);
            }
            deck.setPendingPublic(true);
            deck.setRejectionReason(null);
        } else {
            deck.setVisibility(DeckVisibility.PRIVATE);
            deck.setPendingPublic(false);
        }
        deckRepository.save(deck);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public void deleteDeck(Long id) {
        Deck deck = getExistingDeck(id);
        Profile profile = authService.getCurrentProfile();

        deckValidator.verifyOwner(deck, profile.getId());

        deck.setDeleted(true);
        deckRepository.save(deck);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"deck_detail", "public_decks"}, allEntries = true)
    public void purgeSoftDeletedDecks() {
        List<Deck> deletedDecks = deckRepository.findByIsDeletedTrue();
        if (!deletedDecks.isEmpty()) {
            log.info("Bắt đầu dọn dẹp vĩnh viễn {} bộ bài đã đánh dấu soft delete...", deletedDecks.size());
            deckRepository.deleteAll(deletedDecks);
            log.info("Đã xóa vĩnh viễn {} bộ bài soft delete thành công.", deletedDecks.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getMyDecks(int page, int size) {
        Profile profile = authService.getCurrentProfile();
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<Deck> deckPage = deckRepository.findByProfileAndIsDeletedFalse(profile, pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Transactional
    @Override
    public DeckResponse fork(Long id) {
        Profile currentProfile = authService.getCurrentProfile();
        Deck deck = getExistingDeck(id);

        deckValidator.validateForkEligibility(deck, currentProfile.getId());

        deck.setForkCount(deck.getForkCount() + 1);
        deckRepository.save(deck);

        Deck newDeck = buildForkedDeck(deck, currentProfile);
        Deck savedDeck = deckRepository.save(newDeck);

        copyCardsToForkedDeck(deck.getId(), savedDeck);

        return deckMapper.toDeckResponse(savedDeck);
    }

    @Transactional
    @Override
    public void share(Long deckId, ShareDeckRequest request) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        deckShareHelper.share(deck, owner, request);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "public_decks",
        key = "#page + ':' + #size + ':' + (#keyword?:'') + ':' + (#tagId?:'') + ':' + (#sourceLang?:'') + ':' + (#targetLang?:'')"
    )
    public PageResponse<DeckResponse> getPublicDecks(int page, int size, String keyword, Long tagId, String sourceLang, String targetLang) {
        Pageable pageable = PageUtils.createPageable(page, size);

        String sanitizedKeyword = sanitizeString(keyword);
        String sanitizedSourceLang = sanitizeString(sourceLang);
        String sanitizedTargetLang = sanitizeString(targetLang);

        Page<Deck> deckPage = deckRepository.searchPublicDecks(sanitizedKeyword, tagId, sanitizedSourceLang, sanitizedTargetLang, pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(
        value = "public_decks",
        key = "'status:' + (#status != null ? #status : 'PUBLIC') + ':' + #page + ':' + #size",
        unless = "#status == T(com.fivetuat.fsoftprojectserver.enums.DeckVisibility).PRIVATE"
    )
    public PageResponse<DeckResponse> getDecksByStatus(DeckVisibility status, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        DeckVisibility targetStatus = (status != null) ? status : DeckVisibility.PUBLIC;
        Page<Deck> deckPage;

        switch (targetStatus) {
            case PRIVATE -> {
                Profile profile = authService.getCurrentProfile();
                deckPage = deckRepository.findByProfileAndVisibilityAndIsDeletedFalse(profile, DeckVisibility.PRIVATE, pageable);
            }
            default -> deckPage = deckRepository.findByVisibilityAndIsDeletedFalse(DeckVisibility.PUBLIC, pageable);
        }

        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Override
    public Deck getMyDeck(Long id) {
        Profile profile = authService.getCurrentProfile();
        return deckRepository.findByIdAndProfileIdAndIsDeletedFalse(id, profile.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }

    @Override
    public Deck getDeckWithEditPermission(Long id) {
        Deck deck = getExistingDeck(id);
        Profile profile = authService.getCurrentProfile();

        boolean isOwner = deckValidator.isDeckOwner(deck, profile.getId());
        boolean hasEditPermission = deckShareRepository.findByDeckIdAndProfileId(id, profile.getId())
                .map(share -> share.getPermission() == SharePermission.EDIT)
                .orElse(false);

        if (!isOwner && !hasEditPermission) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return deck;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckShareResponse> getSharedUsers(Long deckId, Pageable pageable) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        return deckShareHelper.getSharedUsers(deck, owner, pageable);
    }

    @Override
    @Transactional
    public void updateSharePermission(Long deckId, UUID profileId, SharePermission permission) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        deckShareHelper.updateSharePermission(deck, owner, profileId, permission);
    }

    @Override
    @Transactional
    public void revokeShare(Long deckId, UUID profileId) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        deckShareHelper.revokeShare(deck, owner, profileId);
    }

    @Transactional
    @Override
    public DeckResponse toggleShareLink(Long deckId) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        return deckShareHelper.toggleShareLink(deck, owner);
    }

    @Override
    @Transactional(readOnly = true)
    public DeckResponse getDeckByShareToken(String shareToken) {
        Deck deck = deckRepository.findByShareTokenAndIsShareLinkEnabledTrueAndIsDeletedFalse(shareToken)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        return deckMapper.toDeckResponse(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getSharedWithMeDecks(int page, int size) {
        Profile profile = authService.getCurrentProfile();
        return deckShareHelper.getSharedWithMeDecks(profile, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CrammingQueueResponse getCramCards(Long deckId, Boolean shuffle, String filter, Integer limit) {
        Deck deck = getExistingDeck(deckId);
        return deckCrammingHelper.getCramCards(deck, shuffle, filter, limit);
    }

    private Deck getExistingDeck(Long id) {
        return deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }

    private void applyParentDeck(Deck deck, Long parentDeckId) {
        if (parentDeckId != null) {
            Deck parent = getExistingDeck(parentDeckId);
            deck.setParentDeck(parent);
        } else {
            deck.setParentDeck(null);
        }
    }

    private void applyCoverImage(Deck deck, MultipartFile coverImage) {
        if (coverImage != null && !coverImage.isEmpty()) {
            if (deck.getCoverImageUrl() != null && !deck.getCoverImageUrl().isBlank()) {
                uploadService.deleteFile(deck.getCoverImageUrl());
            }
            String imageUrl = uploadService.uploadFile(coverImage, FolderName.DECKS);
            log.info("Uploaded cover image: {}", imageUrl);
            deck.setCoverImageUrl(imageUrl);
        }
    }

    private Deck buildForkedDeck(Deck sourceDeck, Profile newOwner) {
        Deck newDeck = new Deck(sourceDeck);
        newDeck.setProfile(newOwner);
        newDeck.setForkCount(0);
        newDeck.setParentDeck(sourceDeck);
        newDeck.setVisibility(DeckVisibility.PRIVATE);
        newDeck.setOfficial(false);

        if (sourceDeck.getTags() != null && !sourceDeck.getTags().isEmpty()) {
            newDeck.setTags(new HashSet<>(sourceDeck.getTags()));
        }
        return newDeck;
    }

    private void copyCardsToForkedDeck(Long sourceDeckId, Deck targetDeck) {
        List<Card> cards = cardRepository.findByDeckId(sourceDeckId);
        if (cards != null && !cards.isEmpty()) {
            List<Card> copiedCards = cards.stream()
                    .map(card -> {
                        Card newCard = new Card(card);
                        newCard.setDeck(targetDeck);
                        return newCard;
                    })
                    .collect(Collectors.toList());
            cardRepository.saveAll(copiedCards);
        }
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

    private String sanitizeString(String input) {
        return (input != null && !input.trim().isEmpty()) ? input.trim() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getPublicDecksByUserId(UUID profileId, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<Deck> deckPage = deckRepository.findByProfileIdAndVisibilityAndIsDeletedFalse(profileId, DeckVisibility.PUBLIC, pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    @Override
    public List<Long> getAccessibleDeckIds() {
        Profile currentProfile = authService.getCurrentProfile();
        return deckRepository.findAllAccessibleDeckIdsByProfileId(currentProfile.getId());
    }
}