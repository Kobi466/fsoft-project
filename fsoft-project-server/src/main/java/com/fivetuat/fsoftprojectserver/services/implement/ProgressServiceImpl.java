package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.UserStreak;
import com.fivetuat.fsoftprojectserver.entities.UserActivity;
import com.fivetuat.fsoftprojectserver.mappers.UserActivityMapper;
import com.fivetuat.fsoftprojectserver.mappers.UserStreakMapper;
import com.fivetuat.fsoftprojectserver.repositories.UserActivityRepository;
import com.fivetuat.fsoftprojectserver.repositories.UserStreakRepository;
import com.fivetuat.fsoftprojectserver.services.AuthService;
import com.fivetuat.fsoftprojectserver.services.ProgressService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProgressServiceImpl implements ProgressService {

    AuthService authService;
    UserStreakRepository userStreakRepository;
    UserActivityRepository userActivityRepository;
    UserStreakMapper userStreakMapper;
    UserActivityMapper userActivityMapper;

    @Override
    @Transactional
    public UserStreakResponse getMyStreak() {
        Profile profile = authService.getCurrentProfile();
        log.info("Getting streak info for profile: {}", profile.getId());

        UserStreak streak = userStreakRepository.findByProfileId(profile.getId())
                .orElseGet(() -> {
                    log.info("Streak not found for profile {}, initializing default streak.", profile.getId());
                    UserStreak newStreak = UserStreak.builder()
                            .profile(profile)
                            .currentStreak(0)
                            .longestStreak(0)
                            .lastActiveDate(null)
                            .build();
                    return userStreakRepository.save(newStreak);
                });

        return userStreakMapper.toUserStreakResponse(streak);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserActivityResponse> getMyActivities(LocalDate startDate, LocalDate endDate) {
        Profile profile = authService.getCurrentProfile();
        log.info("Getting activities for profile: {} between {} and {}", profile.getId(), startDate, endDate);

        List<UserActivity> activities = userActivityRepository
                .findByProfileIdAndActivityDateBetween(profile.getId(), startDate, endDate);

        return userActivityMapper.toUserActivityResponseList(activities);
    }
}
