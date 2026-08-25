package com.fivetuat.fsoftprojectserver.services.support;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ShareDeckRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.DeckShareResponse;
import com.fivetuat.fsoftprojectserver.entities.Deck;
import com.fivetuat.fsoftprojectserver.entities.DeckShare;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.SharePermission;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.DeckMapper;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckShareRepository;
import com.fivetuat.fsoftprojectserver.services.NotificationService;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import com.fivetuat.fsoftprojectserver.utils.PageUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeckShareHelper {

    DeckShareRepository deckShareRepository;
    DeckRepository deckRepository;
    ProfileService profileService;
    NotificationService notificationService;
    DeckMapper deckMapper;
    DeckValidator deckValidator;

    @Transactional
    public void share(Deck deck, Profile owner, ShareDeckRequest request) {
        deckValidator.verifyOwner(deck, owner.getId());

        String ownerName = getProfileDisplayName(owner, owner.getAuth().getEmail());
        Profile recipient = findRecipientOrInvite(request.getEmail(), ownerName, deck.getTitle());
        if (recipient == null) {
            return;
        }

        if (recipient.getId().equals(owner.getId())) {
            throw new AppException(ErrorCode.CANNOT_SHARE_WITH_YOURSELF);
        }

        String recipientName = getProfileDisplayName(recipient, recipient.getAuth().getEmail());
        String permissionStr = (request.getPermission() == SharePermission.EDIT) ? "Xem và Chỉnh sửa" : "Xem bài học";

        saveOrUpdateShare(deck, recipient, request.getPermission(), recipientName, ownerName, permissionStr);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeckShareResponse> getSharedUsers(Deck deck, Profile owner, Pageable pageable) {
        deckValidator.verifyOwner(deck, owner.getId());
        Page<DeckShare> shares = deckShareRepository.findByDeckId(deck.getId(), pageable);
        return PageUtils.toPageResponse(shares, share -> DeckShareResponse.builder()
                .profileId(share.getProfile().getId())
                .email(share.getProfile().getAuth().getEmail())
                .fullName(share.getProfile().getFullName())
                .avatar(share.getProfile().getAvatar())
                .permission(share.getPermission())
                .build());
    }

    @Transactional
    public void updateSharePermission(Deck deck, Profile owner, UUID profileId, SharePermission permission) {
        deckValidator.verifyOwner(deck, owner.getId());

        DeckShare deckShare = deckShareRepository.findByDeckIdAndProfileId(deck.getId(), profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        deckShare.setPermission(permission);
        deckShareRepository.save(deckShare);
    }

    @Transactional
    public void revokeShare(Deck deck, Profile owner, UUID profileId) {
        deckValidator.verifyOwner(deck, owner.getId());

        DeckShare deckShare = deckShareRepository.findByDeckIdAndProfileId(deck.getId(), profileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        deckShareRepository.delete(deckShare);
    }

    @Transactional
    public DeckResponse toggleShareLink(Deck deck, Profile owner) {
        deckValidator.verifyOwner(deck, owner.getId());

        boolean newState = !deck.isShareLinkEnabled();
        deck.setShareLinkEnabled(newState);

        if (newState && deck.getShareToken() == null) {
            deck.setShareToken(UUID.randomUUID().toString().replace("-", ""));
        }

        Deck saved = deckRepository.save(deck);
        return deckMapper.toDeckResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> getSharedWithMeDecks(Profile profile, int page, int size) {
        Pageable pageable = PageUtils.createPageable(page, size);
        Page<Deck> deckPage = deckRepository.findSharedDecksByProfile(profile, pageable);
        return PageUtils.toPageResponse(deckPage, deckMapper::toDeckResponse);
    }

    private String getProfileDisplayName(Profile profile, String fallback) {
        return (profile.getFullName() != null && !profile.getFullName().isBlank())
                ? profile.getFullName()
                : fallback;
    }

    private Profile findRecipientOrInvite(String email, String ownerName, String deckTitle) {
        try {
            return profileService.getProfileByEmail(email);
        } catch (AppException appException) {
            log.info("Inviting unregistered user {} to deck '{}' owned by {}", email, deckTitle, ownerName);
            notificationService.sendDeckInviteEmail(email, ownerName, deckTitle);
            return null;
        }
    }

    private void saveOrUpdateShare(Deck deck, Profile recipient, SharePermission permission, String recipientName, String ownerName, String permissionStr) {
        Optional<DeckShare> deckShareOtp = deckShareRepository.findByDeckIdAndProfileId(deck.getId(), recipient.getId());
        if (deckShareOtp.isPresent()) {
            DeckShare deckShare = deckShareOtp.get();
            if (!deckShare.getPermission().equals(permission)) {
                deckShare.setPermission(permission);
                deckShareRepository.save(deckShare);
                log.info("Notifying registered user {} ({}) about deck '{}' shared by {}", recipientName, recipient.getAuth().getEmail(), deck.getTitle(), ownerName);
                notificationService.sendDeckSharedEmail(recipient.getAuth().getEmail(), recipientName, ownerName, deck.getTitle(), permissionStr);
            }
            return;
        }

        DeckShare deckShare = DeckShare.builder()
                .deck(deck)
                .profile(recipient)
                .permission(permission)
                .build();
        deckShareRepository.save(deckShare);
        log.info("Notifying registered user {} ({}) about deck '{}' shared by {}", recipientName, recipient.getAuth().getEmail(), deck.getTitle(), ownerName);
        notificationService.sendDeckSharedEmail(recipient.getAuth().getEmail(), recipientName, ownerName, deck.getTitle(), permissionStr);
    }
}
