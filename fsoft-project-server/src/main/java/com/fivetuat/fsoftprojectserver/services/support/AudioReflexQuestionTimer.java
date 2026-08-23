
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
public class AudioReflexQuestionTimer {
    private final ThreadPoolTaskScheduler audioReflexTaskScheduler;
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    public void schedule(String roomCode, Runnable task, int delaySeconds) {
        cancel(roomCode);
        ScheduledFuture<?> future = audioReflexTaskScheduler.schedule(task, Instant.now().plusSeconds(delaySeconds));
        timers.put(roomCode, future);
    }
    public void cancel(String roomCode) {
        ScheduledFuture<?> existing = timers.remove(roomCode);
        if (existing != null) existing.cancel(false);
    }
}