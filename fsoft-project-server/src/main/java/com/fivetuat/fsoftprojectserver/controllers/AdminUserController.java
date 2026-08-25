package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.dtos.ApiResponse;
import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateUserRolesRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminUserResponse;
import com.fivetuat.fsoftprojectserver.enums.SuccessCode;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin User Controller", description = "Quản trị người dùng & Phân quyền (Admin)")
public class AdminUserController {

    AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả người dùng (Có phân trang & tìm kiếm)")
    public ApiResponse<PageResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            Pageable pageable
    ) {
        return ApiResponse.ok(adminUserService.getAllUsers(keyword, role, pageable), SuccessCode.GET_USERS_SUCCESS);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Xem chi tiết thông tin người dùng theo ID")
    public ApiResponse<AdminUserResponse> getUserById(@PathVariable UUID userId) {
        return ApiResponse.ok(adminUserService.getUserById(userId), SuccessCode.GET_PROFILE_SUCCESS);
    }

    @PutMapping("/{userId}/roles")
    @Operation(summary = "Cập nhật danh sách Role/Phân quyền cho người dùng")
    public ApiResponse<AdminUserResponse> updateUserRoles(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserRolesRequest request
    ) {
        return ApiResponse.ok(adminUserService.updateUserRoles(userId, request), SuccessCode.UPDATE_PROFILE_SUCCESS);
    }
}
