package com.fivetuat.fsoftprojectserver.services.implement;


import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import com.fivetuat.fsoftprojectserver.repositories.RoleRepository;
import com.fivetuat.fsoftprojectserver.services.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;

    @Override
    public void createRole(String roleName, String description, Set<Permission> permissions) {
        Optional<Role> optionalRole = roleRepository.findByName(roleName);
        if (optionalRole.isPresent()) {
	        log.info("Role already exists: {}", roleName);
        } else {
            Role role = Role.builder()
                    .name(roleName)
                    .description(description)
                    .permissions(permissions)
                    .build();
            roleRepository.save(role);
        }
    }

    @Override
    public Role getRoleByName(String roleName) {
        return roleRepository.findByName(roleName).orElse(null);
    }
}
