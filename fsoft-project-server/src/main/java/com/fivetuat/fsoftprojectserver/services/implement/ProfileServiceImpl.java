package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PublicProfileResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.DeckVisibility;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.enums.PostStatus;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.ProfileMapper;
import com.fivetuat.fsoftprojectserver.repositories.CommunityPostRepository;
import com.fivetuat.fsoftprojectserver.repositories.DeckRepository;
import com.fivetuat.fsoftprojectserver.repositories.ProfileRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserStreakRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {

    AuthService authService;
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    UploadService uploadService;
    UserStreakRepository userStreakRepository;
    CommunityPostRepository communityPostRepository;
    DeckRepository deckRepository;

    @Override
    public ProfileUpdateResponse getMyProfile() {
        Profile profile = authService.getCurrentProfile();
        return profileMapper.toProfileUpdateResponse(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "public_profiles", key = "#result.id")
    public ProfileUpdateResponse updateProfile(ProfileUpdateRequest request) {
        log.info("Updating profile info");
        Profile profile = authService.getCurrentProfile();
        profileMapper.updateProfile(profile, request);
        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toProfileUpdateResponse(savedProfile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "public_profiles", key = "#result.id")
    public ProfileUpdateResponse updateAvatar(MultipartFile avatarFile) {
        log.info("Updating avatar");

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        Profile profile = authService.getCurrentProfile();
        String avatarUrl = uploadService.uploadFile(avatarFile, FolderName.AVATARS);
        profile.setAvatar(avatarUrl);

        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toProfileUpdateResponse(savedProfile);
    }

    @Override
    public Profile getProfileByEmail(String email) {
        return this.profileRepository.findByAuthEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_FOUND)
        );
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "public_profiles", key = "#profileId")
    public PublicProfileResponse getPublicProfileById(UUID profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        PublicProfileResponse response = profileMapper.toPublicProfileResponse(profile);

        // Lấy thông tin chuỗi ngày học liên tục (Streak)
        userStreakRepository.findByProfileId(profileId).ifPresent(streak -> {
            response.setCurrentStreak(streak.getCurrentStreak());
            response.setLongestStreak(streak.getLongestStreak());
        });

        // Đếm tổng số bài viết cộng đồng đã được duyệt
        long totalPosts = communityPostRepository.countByProfileIdAndStatusAndIsDeletedFalse(profileId, PostStatus.APPROVED);
        response.setTotalPosts(totalPosts);

        // Đếm tổng số bộ thẻ công khai đã chia sẻ
        long totalPublicDecks = deckRepository.countByProfileIdAndVisibilityAndIsDeletedFalse(profileId, DeckVisibility.PUBLIC);
        response.setTotalPublicDecks(totalPublicDecks);

        return response;
    }
}