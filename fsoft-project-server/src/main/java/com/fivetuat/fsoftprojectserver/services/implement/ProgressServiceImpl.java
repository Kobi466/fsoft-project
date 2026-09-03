package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.response.UserActivityResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.UserStreakResponse;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.entities.UserActivity;
import com.fivetuat.fsoftprojectserver.entities.UserStreak;
import com.fivetuat.fsoftprojectserver.enums.ActivityType;
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

    UserStreakRepository userStreakRepository;
    UserActivityRepository userActivityRepository;
    UserStreakMapper userStreakMapper;
    UserActivityMapper userActivityMapper;
    AuthService authService;

    @Override
    @Transactional
    public UserStreakResponse getMyStreak() {
        Profile profile = authService.getCurrentProfile();
        log.info("Getting streak for profile: {}", profile.getId());

        UserStreak streak = getOrCreateUniqueStreak(profile);
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

    @Override
    @Transactional
    public void updateUserActivityAndStreak(Profile profile, LocalDate activityDate, ActivityType activityType) {
        if (profile == null) {
            log.warn("Profile is null, skip updating activity and streak.");
            return;
        }

        LocalDate targetDate = (activityDate != null) ? activityDate : LocalDate.now();
        ActivityType type = (activityType != null) ? activityType : ActivityType.CARD_REVIEW;

        log.info("Updating activity and streak for profile: {} on date: {}, type: {}", profile.getId(), targetDate, type);

        try {
            // 1. Cập nhật nhật ký hoạt động UserActivity (Tự động gộp dòng trùng lặp trong DB nếu có)
            List<UserActivity> activities = userActivityRepository.findAllByProfileIdAndActivityDate(profile.getId(), targetDate);
            UserActivity activity;

            if (activities.isEmpty()) {
                activity = UserActivity.builder()
                        .profile(profile)
                        .activityDate(targetDate)
                        .cardsReviewedCount(0)
                        .quizzesCompletedCount(0)
                        .gamesPlayedCount(0)
                        .build();
            } else {
                activity = activities.get(0);
                // Dọn dẹp dòng trùng lặp nếu từng bị insert đúp trong quá khứ
                if (activities.size() > 1) {
                    log.warn("Phát hiện {} bản ghi trùng lặp UserActivity cho profile {} vào ngày {}. Tiến hành tự động hợp nhất...",
                            activities.size(), profile.getId(), targetDate);
                    for (int i = 1; i < activities.size(); i++) {
                        UserActivity extra = activities.get(i);
                        activity.setCardsReviewedCount(activity.getCardsReviewedCount() + extra.getCardsReviewedCount());
                        activity.setQuizzesCompletedCount(activity.getQuizzesCompletedCount() + extra.getQuizzesCompletedCount());
                        activity.setGamesPlayedCount(activity.getGamesPlayedCount() + extra.getGamesPlayedCount());
                        userActivityRepository.delete(extra);
                    }
                }
            }

            switch (type) {
                case QUIZ_COMPLETED -> activity.setQuizzesCompletedCount(activity.getQuizzesCompletedCount() + 1);
                case GAME_PLAYED -> activity.setGamesPlayedCount(activity.getGamesPlayedCount() + 1);
                case CARD_REVIEW -> activity.setCardsReviewedCount(activity.getCardsReviewedCount() + 1);
            }
            userActivityRepository.save(activity);

            // 2. Cập nhật chuỗi Streak liên tục
            updateStreakInternal(profile, targetDate);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật nhật ký hoạt động và Streak cho profile {}: {}", profile.getId(), e.getMessage(), e);
        }
    }

    private void updateStreakInternal(Profile profile, LocalDate activityDate) {
        UserStreak streak = getOrCreateUniqueStreak(profile);
        LocalDate lastActive = streak.getLastActiveDate();

        if (lastActive == null) {
            streak.setCurrentStreak(1);
            streak.setLastActiveDate(activityDate);
        } else if (lastActive.equals(activityDate.minusDays(1))) {
            streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            streak.setLastActiveDate(activityDate);
        } else if (lastActive.isBefore(activityDate.minusDays(1))) {
            streak.setCurrentStreak(1);
            streak.setLastActiveDate(activityDate);
        } else if (activityDate.isAfter(lastActive)) {
            streak.setLastActiveDate(activityDate);
        }

        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }

        userStreakRepository.save(streak);
    }

    private UserStreak getOrCreateUniqueStreak(Profile profile) {
        List<UserStreak> streaks = userStreakRepository.findAllByProfileId(profile.getId());
        if (streaks.isEmpty()) {
            UserStreak newStreak = UserStreak.builder()
                    .profile(profile)
                    .currentStreak(0)
                    .longestStreak(0)
                    .lastActiveDate(null)
                    .build();
            return userStreakRepository.save(newStreak);
        }

        UserStreak primaryStreak = streaks.get(0);
        if (streaks.size() > 1) {
            log.warn("Phát hiện {} bản ghi trùng lặp UserStreak cho profile {}. Tiến hành xóa dọn dẹp các dòng dư thừa...",
                    streaks.size(), profile.getId());
            for (int i = 1; i < streaks.size(); i++) {
                userStreakRepository.delete(streaks.get(i));
            }
        }
        return primaryStreak;
    }
}
