package com.fivetuat.fsoftprojectserver.services;



import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;

import java.util.UUID;

public interface UserService {
//    PageResponse<UserDashboardResponse> getUserList(Pageable pageable);
    Auth getUserById(UUID id);
    void deleteUser(UUID id);
//    UserResponse provideRole()
}
