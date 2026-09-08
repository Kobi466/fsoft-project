package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminBanUserRequest;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateUserRolesRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminUserResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.RefreshTokenRepository;
import com.fivetuat.fsoftprojectserver.repositories.RoleRepository;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService {

    AuthRepository authRepository;
    RoleRepository roleRepository;
    RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getAllUsers(String keyword, String role, Pageable pageable) {
        Page<Auth> authPage = authRepository.findAll(pageable);

        List<AdminUserResponse> content = authPage.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserResponse>builder()
                .content(content)
                .totalElements(authPage.getTotalElements())
                .totalPages(authPage.getTotalPages())
                .pageNo(authPage.getNumber())
                .pageSize(authPage.getSize())
                .last(authPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserResponse getUserById(UUID userId) {
        Auth auth = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        return mapToAdminUserResponse(auth);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRoles(UUID userId, AdminUpdateUserRolesRequest request) {
        Auth auth = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoleNames()) {
            Role r = roleRepository.findByName(roleName)
                    .orElseGet(() -> roleRepository.findById(roleName)
                            .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
            roles.add(r);
        }

        auth.setRoles(roles);
        Auth updatedAuth = authRepository.save(auth);

        return mapToAdminUserResponse(updatedAuth);
    }

    @Override
    @Transactional
    public AdminUserResponse banUser(UUID userId, AdminBanUserRequest request) {
        Auth auth = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        Optional<UUID> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent() && currentUserLogin.get().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        auth.setBanned(true);
        auth.setBannedAt(LocalDateTime.now());
        auth.setBanReason(request.getReason());

        if (request.getBanDurationDays() != null && request.getBanDurationDays() > 0) {
            auth.setBannedUntil(LocalDateTime.now().plusDays(request.getBanDurationDays()));
        } else {
            auth.setBannedUntil(null);
        }

        Auth updatedAuth = authRepository.save(auth);

        refreshTokenRepository.deleteByAuthId(userId);

        return mapToAdminUserResponse(updatedAuth);
    }

    @Override
    @Transactional
    public AdminUserResponse unbanUser(UUID userId) {
        Auth auth = authRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        auth.setBanned(false);
        auth.setBannedAt(null);
        auth.setBannedUntil(null);
        auth.setBanReason(null);

        Auth updatedAuth = authRepository.save(auth);
        return mapToAdminUserResponse(updatedAuth);
    }

    private AdminUserResponse mapToAdminUserResponse(Auth auth) {
        Profile profile = auth.getProfile();
        Set<String> roleNames = auth.getRoles() != null ?
                auth.getRoles().stream().map(Role::getName).collect(Collectors.toSet()) : Collections.emptySet();

        return AdminUserResponse.builder()
                .id(auth.getId())
                .email(auth.getEmail())
                .phone(auth.getPhone())
                .fullName(profile != null ? profile.getFullName() : null)
                .avatar(profile != null ? profile.getAvatar() : null)
                .roles(roleNames)
                .createTime(profile != null ? profile.getCreateTime() : null)
                .updateTime(profile != null ? profile.getUpdateTime() : null)
                .isBanned(auth.isBanned())
                .bannedAt(auth.getBannedAt())
                .bannedUntil(auth.getBannedUntil())
                .banReason(auth.getBanReason())
                .build();
    }
}
