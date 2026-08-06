package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import org.springframework.stereotype.Service;

import java.util.Set;

public interface RoleService {
    void createRole(String roleName, String description, Set<Permission> permissions);

    Role getRoleByName(String roleName);
}
