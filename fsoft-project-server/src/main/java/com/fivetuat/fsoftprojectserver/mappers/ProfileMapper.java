package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.ProfileUpdateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.ProfileUpdateResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.PublicProfileResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(target = "avatar", ignore = true)
    void updateProfile(@MappingTarget Profile profile, ProfileUpdateRequest updateRequest);

    ProfileUpdateResponse toProfileUpdateResponse(Profile profile);

    @Mapping(target = "currentStreak", ignore = true)
    @Mapping(target = "longestStreak", ignore = true)
    @Mapping(target = "totalPosts", ignore = true)
    @Mapping(target = "totalPublicDecks", ignore = true)
    PublicProfileResponse toPublicProfileResponse(Profile profile);
}
