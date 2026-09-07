package com.fivetuat.fsoftprojectserver.services.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PendingActionStore {

    static final String KEY_PREFIX = "admin-ai:pending:";
    static final String LATEST_KEY_PREFIX = "admin-ai:pending-admin:";
    static final Duration TTL = Duration.ofMinutes(5);

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper;

    public record PendingAction(
            UUID actionId,
            String tool,
            Map<String, Object> params,
            UUID adminId,
            String summary,
            LocalDateTime createdAt,
            LocalDateTime expiresAt) {
    }

    public void save(PendingAction action) {
        try {
            redisTemplate.opsForValue().set(
                    KEY_PREFIX + action.actionId(),
                    objectMapper.writeValueAsString(action),
                    TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize pending action", e);
        }
    }

    public Optional<PendingAction> find(String actionId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + actionId);
        if (json == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(json, PendingAction.class));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void delete(String actionId) {
        redisTemplate.delete(KEY_PREFIX + actionId);
    }

    public void indexLatest(UUID adminId, UUID actionId) {
        redisTemplate.opsForValue().set(LATEST_KEY_PREFIX + adminId, actionId.toString(), TTL);
    }

    public Optional<PendingAction> findLatestByAdmin(UUID adminId) {
        String actionId = redisTemplate.opsForValue().get(LATEST_KEY_PREFIX + adminId);
        if (actionId == null) return Optional.empty();
        return find(actionId);
    }

    public void clearLatestIndex(UUID adminId) {
        redisTemplate.delete(LATEST_KEY_PREFIX + adminId);
    }
}