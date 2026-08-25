package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateUserRolesRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminUserResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminUserService {
    PageResponse<AdminUserResponse> getAllUsers(String keyword, String role, Pageable pageable);
    AdminUserResponse getUserById(UUID userId);
    AdminUserResponse updateUserRoles(UUID userId, AdminUpdateUserRolesRequest request);
}
