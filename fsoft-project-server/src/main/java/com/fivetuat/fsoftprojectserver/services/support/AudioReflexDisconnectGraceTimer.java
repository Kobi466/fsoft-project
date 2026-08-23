package com.fivetuat.fsoftprojectserver.services.support;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AudioReflexDisconnectGraceTimer {

    private final ThreadPoolTaskScheduler audioReflexTaskScheduler;
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();

    private String key(String roomCode, String userId) {
        return roomCode + "::" + userId;
    }

    public void schedule(String roomCode, String userId, Runnable task, int delaySeconds) {
        cancel(roomCode, userId);
        ScheduledFuture<?> future = audioReflexTaskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));
        timers.put(key(roomCode, userId), future);
    }

    public void cancel(String roomCode, String userId) {
        ScheduledFuture<?> existing = timers.remove(key(roomCode, userId));
        if (existing != null) existing.cancel(false);
    }
}