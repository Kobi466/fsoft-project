package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.entities.UserActivity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserActivityMapper {
    UserActivityResponse toUserActivityResponse(UserActivity userActivity);
    List<UserActivityResponse> toUserActivityResponseList(List<UserActivity> userActivities);
}
