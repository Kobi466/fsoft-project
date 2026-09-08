package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.fivetuat.fsoftprojectserver.dtos.response.RateLimiterResult;
import com.fivetuat.fsoftprojectserver.services.RateLimiterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterServiceImpl implements RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String SLIDING_WINDOW_LUA_SCRIPT = """
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local clearBefore = now - window
            redis.call('ZREMRANGEBYSCORE', key, 0, clearBefore)
            local currentCount = redis.call('ZCARD', key)
            if currentCount < limit then
                redis.call('ZADD', key, now, now)
                redis.call('EXPIRE', key, math.ceil(window / 1000) + 1)
                local remaining = limit - currentCount - 1
                return {1, remaining, math.ceil(window / 1000)}
            else
                local oldest = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
                local resetSec = math.ceil(window / 1000)
                if #oldest >= 2 then
                    local oldestTime = tonumber(oldest[2])
                    resetSec = math.max(1, math.ceil((oldestTime + window - now) / 1000))
                end
                return {0, 0, resetSec}
            end
            """;

    @SuppressWarnings("rawtypes")
    private final RedisScript<List> rateLimitRedisScript = new DefaultRedisScript<>(SLIDING_WINDOW_LUA_SCRIPT, List.class);

    @Override
    public RateLimiterResult checkLimit(String key, long limit, long periodSeconds) {
        long now = System.currentTimeMillis();
        long windowMs = periodSeconds * 1000;

        try {
            @SuppressWarnings("unchecked")
            List<Long> result = stringRedisTemplate.execute(
                    rateLimitRedisScript,
                    Collections.singletonList(key),
                    String.valueOf(now),
                    String.valueOf(windowMs),
                    String.valueOf(limit)
            );

            if (result != null && result.size() >= 3) {
                Long resAllowed = result.get(0);
                Long resRemaining = result.get(1);
                Long resReset = result.get(2);

                boolean allowed = resAllowed != null && resAllowed == 1L;
                long remaining = resRemaining != null ? resRemaining : 0L;
                long resetSeconds = resReset != null ? resReset : periodSeconds;

                return RateLimiterResult.builder()
                        .allowed(allowed)
                        .limit(limit)
                        .remaining(remaining)
                        .resetSeconds(resetSeconds)
                        .build();
            }
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra Redis Rate Limit cho key {}: {}", key, e.getMessage(), e);
        }

        return RateLimiterResult.builder()
                .allowed(true)
                .limit(limit)
                .remaining(limit)
                .resetSeconds(0)
                .build();
    }

//    @Override
//    public RateLimiterResult checkLimit(String key, long limit, long periodSeconds) {
//        long now = System.currentTimeMillis();
//        long windowMs = periodSeconds * 1000;
//        long clearBefore = now - windowMs;
//
//        // xóa các request đã quá hạn 60s từ 0->clearBefore
//        stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, clearBefore);
//
//        // đếm số request còn sống trong 60s gần nhất
//        Long currentCount = stringRedisTemplate.opsForZSet().zCard(key);
//
//        if (currentCount != null && currentCount < limit) {
//            // nếu chưa vượt limit -> Thêm mốc thời gian hiện tại vào Redis ZSet
//            stringRedisTemplate.opsForZSet().add(key, String.valueOf(now), now);
//            stringRedisTemplate.expire(key, periodSeconds + 1, TimeUnit.SECONDS);
//
//            return RateLimiterResult.builder()
//                    .allowed(true)
//                    .limit(limit)
//                    .remaining(limit - currentCount - 1)
//                    .resetSeconds(periodSeconds)
//                    .build();
//        }
//
//        // nếu đã vượt limit -> Chặn request
//        return RateLimiterResult.builder()
//                .allowed(false)
//                .limit(limit)
//                .remaining(0)
//                .resetSeconds(periodSeconds)
//                .build();
//    }

}
