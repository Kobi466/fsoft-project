package com.fivetuat.fsoftprojectserver.services.implement;



import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import com.fivetuat.fsoftprojectserver.repositories.RoleRepository;
import com.fivetuat.fsoftprojectserver.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class RoleServiceImpl implements RoleService {
    RoleRepository roleRepository;

    @Override
    public void createRole(String roleName, String description, Set<Permission> permissions) {
        Optional<Role> optionalRole = roleRepository.findByName(roleName);
        if (optionalRole.isPresent()) {
            Role role = optionalRole.get();
            role.setDescription(description);
            role.setPermissions(permissions);
            roleRepository.save(role);
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
