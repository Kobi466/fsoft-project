package com.fivetuat.fsoftprojectserver.mappers;


import com.fivetuat.fsoftprojectserver.dtos.request.UserCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.UserResponse;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;
import com.fivetuat.fsoftprojectserver.entities.authentication.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "fullName", source = "profile.fullName")
    @Mapping(target = "roles", source = "auth.roles")
    @Mapping(target = "avatar", source = "profile.avatar")
    UserResponse toUserResponse(Auth auth);

    @Mapping(target = "profile.fullName", source = "fullName")
    @Mapping(target = "passwordHash", source = "passWord")
    Auth toAuth(UserCreateRequest userCreateRequest);

    default String mapRoleToString(Role role) {
        return role.getName();
    }
}
