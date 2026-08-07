package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;

public interface PermissionService {
    Permission createPermission(String permissionName, String description);
}
