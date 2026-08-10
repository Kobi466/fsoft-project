package com.fivetuat.fsoftprojectserver.configurations;


import java.util.Set;

import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.PermissionService;
import com.fivetuat.fsoftprojectserver.services.RoleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApplicationInitConfig implements ApplicationRunner {
    PermissionService permissionService;
    RoleService roleService;
    AuthService authService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing application data...");
        var c= permissionService.createPermission("CREATE_ROLE", "CREATE_ROLE");
        var c1=permissionService.createPermission("CREATE_USER", "CREATE_USER");
        var c2=permissionService.createPermission("CREATE_PERMISSION", "CREATE_PERMISSION");
        var c3=permissionService.createPermission("CREATE_SET", "CREATE_SET");
        var c4=permissionService.createPermission("CREATE_WORD", "CREATE_WORD");

        roleService.createRole("ADMIN", "ADMIN", Set.of(c, c1, c2, c3, c4));
        roleService.createRole("USER", "USER", Set.of(c1));

        authService.createAuthAdmin(Auth.builder()
                .email("admin")
                .phone("0123456789")
                .passwordHash("13456789")
                .build());

    }
}