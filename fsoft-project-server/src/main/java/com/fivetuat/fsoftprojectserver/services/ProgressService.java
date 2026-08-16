package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;

import java.time.LocalDate;
import java.util.List;

public interface ProgressService {
    UserStreakResponse getMyStreak();
    List<UserActivityResponse> getMyActivities(LocalDate startDate, LocalDate endDate);
}
