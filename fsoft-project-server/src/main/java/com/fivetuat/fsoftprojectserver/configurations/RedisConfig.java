package com.fivetuat.fsoftprojectserver.configurations;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {

    @Value("${spring.profiles.active:dev}")
    private String prefix;

    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Hỗ trợ ClassLoader cho Spring Boot DevTools (RestartClassLoader)
        objectMapper.setTypeFactory(
                objectMapper.getTypeFactory().withClassLoader(Thread.currentThread().getContextClassLoader())
        );

        // Đăng ký PolymorphicTypeValidator để Jackson tự động thêm thuộc tính @class cho các DTO & PageResponse
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        objectMapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Configure serializers
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

//    @Bean
//    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory){
//        RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory);
//        template.afterPropertiesSet();
//        return template;
//    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(createRedisObjectMapper());

        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
//                .disableCachingNullValues()
                .prefixCacheNameWith("fsoft:" + prefix + ":")
                .entryTtl(Duration.ofMinutes(30));
    }

//    @Bean
//    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(){
//        return (builder) -> builder
//                .withCacheConfiguration("posts",
//                        cacheConfiguration()
//                                .entryTtl(Duration.ofMinutes(RandomGenerator.getDefault().nextInt(1, 29)))
//                        )
//                .withCacheConfiguration("courses", cacheConfiguration()
//                        .entryTtl(Duration.ofMinutes(RandomGenerator.getDefault().nextInt(1, 59))));
//    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = cacheConfiguration();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)

                .withCacheConfiguration("dictionary", defaultConfig.entryTtl(Duration.ofDays(7)))
                .withCacheConfiguration("ai_suggestions", defaultConfig.entryTtl(Duration.ofDays(7)))

                .withCacheConfiguration("tags", defaultConfig.entryTtl(Duration.ofHours(24)))

                .withCacheConfiguration("admin_dashboard_stats", defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration("public_profiles", defaultConfig.entryTtl(Duration.ofMinutes(30)))

                .withCacheConfiguration("deck_detail", defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("deck_cards", defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("card_detail", defaultConfig.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("public_decks", defaultConfig.entryTtl(Duration.ofMinutes(15)))

                .withCacheConfiguration("community_latest_posts", defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("community_popular_posts", defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("community_post_detail", defaultConfig.entryTtl(Duration.ofMinutes(15)))

                .withCacheConfiguration("srs_mastery", defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .withCacheConfiguration("user_streak", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("user_activities", defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("unread_notifications", defaultConfig.entryTtl(Duration.ofMinutes(5)))

                .withCacheConfiguration("ai_situational_lessons", defaultConfig.entryTtl(Duration.ofDays(7)))
                .withCacheConfiguration("ai_stories", defaultConfig.entryTtl(Duration.ofDays(7)))


                .withCacheConfiguration("fallback_vocab_lookups", defaultConfig.entryTtl(Duration.ofMinutes(3)))
                .withCacheConfiguration("fallback_card_searches", defaultConfig.entryTtl(Duration.ofMinutes(3)))

                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis Cache GET error on cache '{}' for key '{}': {}. Falling back to database.", cache.getName(), key, exception.getMessage());
                try {
                    cache.evict(key);
                } catch (Exception e) {
                    log.warn("Failed to evict invalid key '{}' from cache '{}'", key, cache.getName());
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis Cache PUT error on cache '{}' for key '{}': {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis Cache EVICT error on cache '{}' for key '{}': {}", cache.getName(), key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis Cache CLEAR error on cache '{}': {}", cache.getName(), exception.getMessage());
            }
        };
    }
}

