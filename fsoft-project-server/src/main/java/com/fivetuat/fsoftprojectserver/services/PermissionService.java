package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.entities.authentication.Permission;
import org.springframework.stereotype.Service;

public interface PermissionService {
    Permission createPermission(String permissionName, String description);
}
