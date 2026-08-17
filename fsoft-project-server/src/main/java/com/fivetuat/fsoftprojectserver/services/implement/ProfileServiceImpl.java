package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.enums.FolderName;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.ProfileMapper;
import com.fivetuat.fsoftprojectserver.repositories.ProfileRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import com.fivetuat.fsoftprojectserver.services.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileServiceImpl implements ProfileService {
    AuthService authService;
    ProfileRepository profileRepository;
    ProfileMapper profileMapper;
    UploadService uploadService;
    @Override
    public ProfileUpdateResponse getMyProfile() {
        Profile profile = authService.getCurrentProfile();
        return profileMapper.toProfileUpdateResponse(profile);
    }

    @Override
    @Transactional
    public ProfileUpdateResponse updateProfile(ProfileUpdateRequest request) {
        log.info("Updating profile info");
        Profile profile = authService.getCurrentProfile();
        profileMapper.updateProfile(profile, request);
        Profile savedProfile = profileRepository.save(profile);
        return profileMapper.toProfileUpdateResponse(savedProfile);
    }

    @Override
    @Transactional
    public ProfileUpdateResponse updateAvatar(MultipartFile avatarFile) {
        log.info("Updating avatar");

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_ERROR);
        }
        Profile profile = authService.getCurrentProfile();
        // Lam đã triển khai AWS S3 xong, upload avatar vào thư mục "avatars"
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
}