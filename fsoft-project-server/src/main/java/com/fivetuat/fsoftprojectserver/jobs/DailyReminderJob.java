package com.fivetuat.fsoftprojectserver.jobs;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fivetuat.fsoftprojectserver.dtos.response.ReminderUserResponse;
import com.fivetuat.fsoftprojectserver.repositories.UserStreakRepository;
import com.fivetuat.fsoftprojectserver.services.NotificationEmailService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DailyReminderJob {

    UserStreakRepository userStreakRepository;
    NotificationEmailService notificationEmailService;
    RedisTemplate<String, Object> redisTemplate;

    public static final int BATCH_SIZE = 500;

    @Scheduled(cron = "0 33 20 * * *", zone = "Asia/Ho_Chi_Minh")
    public void executeDailyReminder() {
        LocalDate today = LocalDate.now();
        log.info("=== [CRON JOB PURE SPRING] Bắt đầu quét danh sách học viên nhắc nhở học bài ngày {} ===", today);

        int page = 0;
        long totalDispatched = 0;
        Page<ReminderUserResponse> userPage;

        do {
            Pageable pageable = PageRequest.of(page, BATCH_SIZE);
            userPage = userStreakRepository.findUsersNeedingReminder(today, pageable);

            for (ReminderUserResponse user : userPage.getContent()) {
                if (user.getEmail() == null || user.getEmail().isBlank()) {
                    continue;
                }

                String redisKey = "reminder_sent:" + today + ":" + user.getEmail();
                Boolean isFirstTime = redisTemplate.opsForValue().setIfAbsent(redisKey, "true", 24, TimeUnit.HOURS);

                if (Boolean.TRUE.equals(isFirstTime)) {
                    notificationEmailService.sendDailyReminderEmail(
                            user.getEmail(),
                            user.getFullName(),
                            user.getCurrentStreak()
                    );
                    totalDispatched++;
                } else {
                    log.debug("User {} đã được phát tác vụ nhắc nhở hôm nay, bỏ qua.", user.getEmail());
                }
            }

            page++;
        } while (userPage.hasNext());

        log.info("=== [CRON JOB PURE SPRING] Đã hoàn thành quét dữ liệu. Tổng số tác vụ gửi mail đã phát: {} ===", totalDispatched);
    }
}
