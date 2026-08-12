package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Profile Controller", description = "Quản lý thông tin cá nhân người dùng")
public class ProfileController {
    ProfileService profileService;
    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin profile của người dùng đang đăng nhập")
    public ApiResponse<ProfileUpdateResponse> getMyProfile() {
        return ApiResponse.ok(profileService.getMyProfile(), SuccessCode.GET_PROFILE_SUCCESS);
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật thông tin cá nhân (tên, giới thiệu, mạng xã hội)")
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @RequestBody @Valid ProfileUpdateRequest request
    ) {
        return ApiResponse.ok(profileService.updateProfile(request), SuccessCode.UPDATE_PROFILE_SUCCESS);
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật avatar người dùng")
    public ApiResponse<ProfileUpdateResponse> updateAvatar(
            @RequestPart("avatarFile") MultipartFile avatarFile
    ) {
        return ApiResponse.ok(profileService.updateAvatar(avatarFile), SuccessCode.UPDATE_AVATAR_SUCCESS);
    }
}