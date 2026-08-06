package com.fivetuat.fsoftprojectserver.services.implement;


import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import com.fivetuat.fsoftprojectserver.repositories.PermissionRepository;
import com.fivetuat.fsoftprojectserver.services.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    @Override
    public Permission createPermission(String permissionName, String description) {
         return  permissionRepository.findById(permissionName)
                .orElseGet(
                        () -> {
                                permissionRepository.save(
                                    Permission.builder()
                                            .name(permissionName)
                                            .description(description)
                                            .build()
                                );
                                return null;
                            }
                        );
    }
}
