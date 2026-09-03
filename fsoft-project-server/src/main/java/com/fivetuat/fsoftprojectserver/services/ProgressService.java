package com.fivetuat.fsoftprojectserver.services;

import java.time.LocalDate;
import java.util.List;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.ActivityType;

public interface ProgressService {
    UserStreakResponse getMyStreak();

    List<UserActivityResponse> getMyActivities(LocalDate startDate, LocalDate endDate);
        
    void updateUserActivityAndStreak(Profile profile, LocalDate activityDate, ActivityType activityType);
}
