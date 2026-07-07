package com.fivetuat.fsoftprojectserver.configurations;


import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.PermissionService;
import com.fivetuat.fsoftprojectserver.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ApplicationInitConfig implements ApplicationRunner {
    PermissionService permissionService;
    RoleService roleService;
    AuthService authService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
