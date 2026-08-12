package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import org.springframework.web.multipart.MultipartFile;
public interface ProfileService {
    ProfileUpdateResponse getMyProfile();
    ProfileUpdateResponse updateProfile(ProfileUpdateRequest request);
    ProfileUpdateResponse updateAvatar(MultipartFile avatarFile);
}
