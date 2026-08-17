package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import org.springframework.web.multipart.MultipartFile;
public interface ProfileService {
    ProfileUpdateResponse getMyProfile();
    ProfileUpdateResponse updateProfile(ProfileUpdateRequest request);
    ProfileUpdateResponse updateAvatar(MultipartFile avatarFile);
    Profile getProfileByEmail(String email);
}
