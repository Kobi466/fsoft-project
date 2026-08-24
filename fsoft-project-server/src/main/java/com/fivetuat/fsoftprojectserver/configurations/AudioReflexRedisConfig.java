// configurations/AudioReflexRedisConfig.java
package com.fivetuat.fsoftprojectserver.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivetuat.fsoftprojectserver.dtos.game.AudioReflexRoomState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class AudioReflexRedisConfig {

    @Bean(name = "audioReflexRoomRedisTemplate")
    public RedisTemplate<String, AudioReflexRoomState> audioReflexRoomRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        RedisTemplate<String, AudioReflexRoomState> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(mapper, AudioReflexRoomState.class));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new Jackson2JsonRedisSerializer<>(mapper, AudioReflexRoomState.class));
        template.afterPropertiesSet();
        return template;
    }
}