package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;

import java.time.LocalDate;
import java.util.List;

public interface ProgressService {
    UserStreakResponse getMyStreak();
    List<UserActivityResponse> getMyActivities(LocalDate startDate, LocalDate endDate);
    //Implement later
    void updateUserActivityAndStreak(Profile profile, LocalDate activityDate);
}
