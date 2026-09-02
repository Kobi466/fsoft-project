package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.PageResponse;
import com.fivetuat.fsoftprojectserver.dtos.request.AdminUpdateUserRolesRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.AdminUserResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.repositories.AuthRepository;
import com.fivetuat.fsoftprojectserver.repositories.RoleRepository;
import com.fivetuat.fsoftprojectserver.services.AdminUserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminUserServiceImpl implements AdminUserService {

    AuthRepository authRepository;
    RoleRepository roleRepository;

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
                .build();
    }
}
