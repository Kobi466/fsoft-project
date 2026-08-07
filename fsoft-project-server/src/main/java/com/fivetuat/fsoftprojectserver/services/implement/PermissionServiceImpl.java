package com.fivetuat.fsoftprojectserver.services.implement;


import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import com.fivetuat.fsoftprojectserver.repositories.PermissionRepository;
import com.fivetuat.fsoftprojectserver.services.PermissionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    @Override
    public Permission createPermission(String permissionName, String description) {
        Optional<Permission> optionalPermission = permissionRepository.findById(permissionName);
        if (optionalPermission.isPresent()) {
            log.info("Permission already exists: " + permissionName);
            return optionalPermission.get();
        } else {
            return permissionRepository.save(Permission.builder()
                    .name(permissionName)
                    .description(description)
                    .build());
        }
    }

}
