package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.DeckUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.entities.*;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.*;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.DeckService;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

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
    ProfileService profileService;
    DeckShareRepository deckShareRepository;

    @Transactional
    @Override
    public DeckResponse createDeck(DeckCreateRequest request, MultipartFile coverImage) {
        Profile profile = authService.getCurrentProfile();

        Deck deck = deckMapper.toDeck(request);
        deck.setProfile(profile);

        if (request.getParentDeckId() != null) {
            Deck parent = this.getExistingDeck(request.getParentDeckId());
            deck.setParentDeck(parent);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String imageUrl = uploadService.uploadFile(coverImage, FolderName.DECKS);
            log.info("image {}", imageUrl);
            deck.setCoverImageUrl(imageUrl);
        }

        deck.setTags(processTags(request.getTagIds()));

        deck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(deck);
    }

    // View public deck or my deck
    @Override
    @Transactional(readOnly = true)
    public DeckResponse getDeck(Long id) {
        Deck deck = this.getExistingDeck(id);

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
        var deck = this.getMyDeck(id);

        deckMapper.updateDeck(deck, request);

        if (request.getParentDeckId() != null) {
            Deck parent = this.getExistingDeck(request.getParentDeckId());
            deck.setParentDeck(parent);
        } else {
            deck.setParentDeck(null);
        }

        if (coverImage != null && !coverImage.isEmpty()) {
            String imageUrl = uploadService.uploadFile(coverImage, FolderName.DECKS);
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
        Deck deck = this.getExistingDeck(id);

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
                        newCard.setDeck(savedDeck);
                        return newCard;
                    }).collect(Collectors.toList());
            cardRepository.saveAll(copiedCards);
        }

        return deckMapper.toDeckResponse(savedDeck);
    }

    @Transactional
    @Override
    public void share(Long deckId, ShareDeckRequest request) {
        var owner = this.authService.getCurrentProfile();
        var deck = this.getExistingDeck(deckId);
        if (!deck.getProfile().getId().equals(owner.getId())){
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Profile recipient;
        try {
            recipient = this.profileService.getProfileByEmail(request.getEmail());
        }catch (AppException appException) {
            inviteNewUser(request.getEmail());
            return;
        }
        if (recipient.getId().equals(owner.getId())){
            throw new AppException(ErrorCode.CANNOT_SHARE_WITH_YOURSELF);
        }
//        if (this.deckShareRepository.existsByDeckIdAndProfileId(deckId, recipient.getId())){
//            throw new AppException(ErrorCode.DECK_ALREADY_SHARED);
//            // update la cap nhap lai quyen thay vi nem loi
//        }
        Optional<DeckShare> deckShareOtp = this.deckShareRepository.findByDeckIdAndProfileId(deckId, recipient.getId());
        if (deckShareOtp.isPresent()){
            var deckShare = deckShareOtp.get();
            if (!deckShare.getPermission().equals(request.getPermission())){
                deckShare.setPermission(request.getPermission());
                this.deckShareRepository.save(deckShare);
            }
            return;
        }
        var deckShare = DeckShare.builder()
                .deck(deck)
                .profile(recipient)
                .permission(request.getPermission())
                .build();
        this.deckShareRepository.save(deckShare);
        notificationUserShared(request.getEmail());
    }

    private void inviteNewUser(String email){
        // notification user register
    }

    private void notificationUserShared(String email){
        // notification user shared
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
//            case SHARED -> {
//                Profile profile = authService.getCurrentProfile();
//                deckPage = deckRepository.findSharedDecksByProfile(profile, pageable);
//            }
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

    private Deck getExistingDeck(Long id) {
        return deckRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }


    @Override
    public Deck getMyDeck(Long id) {
        UUID profileId = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
        return deckRepository.findByIdAndProfileIdAndIsDeletedFalse(id, profileId)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));
    }

    @Override
    public void save(Deck deck){
        deckRepository.save(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeckShareResponse> getSharedUsers(Long deckId, Pageable pageable) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        if (!deck.getProfile().getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        Page<DeckShare> shares = deckShareRepository.findByDeckId(
                deckId, pageable
        );

        return PageResponse.<DeckShareResponse>builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalPages(shares.getTotalPages())
                .totalElements(shares.getTotalElements())
                .content(
                        shares.stream().map(
                                share -> DeckShareResponse.builder()
                                        .profileId(share.getProfile().getId())
                                        .email(share.getProfile().getAuth().getEmail())
                                        .fullName(share.getProfile().getFullName())
                                        .avatar(share.getProfile().getAvatar())
                                        .permission(share.getPermission())
                                        .build()
                        ).collect(Collectors.toList())
                )
                .build();
    }

    @Override
    @Transactional
    public void updateSharePermission(Long deckId, UUID profileId, SharePermission permission) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        if (!deck.getProfile().getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        DeckShare deckShare = deckShareRepository.findByDeckIdAndProfileId(deckId, profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        deckShare.setPermission(permission);
        deckShareRepository.save(deckShare);
    }

    @Override
    @Transactional
    public void revokeShare(Long deckId, UUID profileId) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);
        if (!deck.getProfile().getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        DeckShare deckShare = deckShareRepository.findByDeckIdAndProfileId(deckId, profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        deckShareRepository.delete(deckShare);
    }

    @Transactional
    @Override
    public DeckResponse toggleShareLink(Long deckId) {
        Profile owner = authService.getCurrentProfile();
        Deck deck = getExistingDeck(deckId);

        if (!deck.getProfile().getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        boolean newState = !deck.isShareLinkEnabled();
        deck.setShareLinkEnabled(newState);

        if (newState && deck.getShareToken() == null) {
            deck.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        }

        deck = deckRepository.save(deck);
        return deckMapper.toDeckResponse(deck);
    }

    @Override
    @Transactional(readOnly = true)
    public DeckResponse getDeckByShareToken(String shareToken) {
        Deck deck = deckRepository.findByShareTokenAndIsShareLinkEnabledTrueAndIsDeletedFalse(shareToken)
                .orElseThrow(() -> new AppException(ErrorCode.DECK_NOT_FOUND));

        return deckMapper.toDeckResponse(deck);
    }
}