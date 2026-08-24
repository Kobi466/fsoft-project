// configurations/AudioReflexTaskSchedulerConfig.java
package com.fivetuat.fsoftprojectserver.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class AudioReflexTaskSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler audioReflexTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("audio-reflex-timer-");
        scheduler.initialize();
        return scheduler;
    }
}