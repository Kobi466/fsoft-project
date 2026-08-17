package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.entities.UserStreak;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserStreakMapper {
    UserStreakResponse toUserStreakResponse(UserStreak userStreak);
}
