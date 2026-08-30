package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PublicProfileResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Profile Controller", description = "Quản lý thông tin hồ sơ cá nhân của người dùng")
public class ProfileController {
    
    ProfileService profileService;
    
    @GetMapping("/me")
    @Operation(
        summary = "Truy xuất thông tin cá nhân hiện tại", 
        description = "Lấy dữ liệu hồ sơ (Profile) của người dùng đang đăng nhập dựa vào Access Token hiện tại, bao gồm họ tên, giới thiệu bản thân (bio), mạng xã hội, ảnh đại diện."
    )
    public ApiResponse<ProfileUpdateResponse> getMyProfile() {
        return ApiResponse.ok(profileService.getMyProfile(), SuccessCode.GET_PROFILE_SUCCESS);
    }

    @GetMapping("/{profileId}")
    @Operation(
        summary = "Xem hồ sơ công khai của người dùng", 
        description = "Truy xuất hồ sơ công khai của một người dùng theo Profile ID, bao gồm thông tin cá nhân, liên kết mạng xã hội, chuỗi ngày học liên tục (Streak) và các chỉ số đóng góp (tổng số bài viết, tổng số bộ thẻ công khai)."
    )
    public ApiResponse<PublicProfileResponse> getPublicProfileById(
            @Parameter(description = "ID hồ sơ cá nhân của người dùng") @PathVariable UUID profileId
    ) {
        return ApiResponse.ok(profileService.getPublicProfileById(profileId), SuccessCode.GET_PUBLIC_PROFILE_SUCCESS);
    }

    @PutMapping("/me")
    @Operation(
        summary = "Cập nhật thông tin cơ bản", 
        description = "Cho phép người dùng tự sửa đổi các thông tin hiển thị cơ bản của bản thân như: Tên hiển thị (DisplayName), Phần tự giới thiệu (Bio) và Các liên kết mạng xã hội."
    )
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @Parameter(description = "JSON Object chứa các thông tin thay đổi") @RequestBody @Valid ProfileUpdateRequest request
    ) {
        return ApiResponse.ok(profileService.updateProfile(request), SuccessCode.UPDATE_PROFILE_SUCCESS);
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Cập nhật ảnh đại diện (Avatar)", 
        description = "Tải lên một hình ảnh từ máy cá nhân để đổi Avatar. Hệ thống tự động lưu trữ và trả về đường dẫn URL của hình ảnh mới."
    )
    public ApiResponse<ProfileUpdateResponse> updateAvatar(
            @Parameter(description = "File hình ảnh (JPG, PNG) của người dùng") @RequestPart("avatarFile") MultipartFile avatarFile
    ) {
        return ApiResponse.ok(profileService.updateAvatar(avatarFile), SuccessCode.UPDATE_AVATAR_SUCCESS);
    }
}