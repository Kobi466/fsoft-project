package com.fivetuat.fsoftprojectserver.services.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PendingActionStoreTest {

    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> valueOperations;

    private PendingActionStore.PendingAction sampleAction() {
        return new PendingActionStore.PendingAction(
                UUID.randomUUID(), "requestRejectDeck",
                Map.of("deckId", 12, "reason", "spam"),
                UUID.randomUUID(), "Từ chối bộ bài 12",
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void saveWritesJsonWithTtl() {
        PendingActionStore.PendingAction action = sampleAction();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        new PendingActionStore(redisTemplate, mapper).save(action);

        verify(valueOperations).set(
                eq("admin-ai:pending:" + action.actionId()), anyString(), eq(Duration.ofMinutes(6)));
    }

    @Test
    void findReturnsActionWhenPresent() {
        PendingActionStore.PendingAction action = sampleAction();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("admin-ai:pending:" + action.actionId()))
                .thenReturn(mapper.valueToTree(action).toString());

        Optional<PendingActionStore.PendingAction> found =
                new PendingActionStore(redisTemplate, mapper).find(action.actionId().toString());

        assertTrue(found.isPresent());
        assertEquals("requestRejectDeck", found.get().tool());
        assertEquals(12, found.get().params().get("deckId"));
    }

    @Test
    void findReturnsEmptyWhenMissing() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        assertTrue(new PendingActionStore(redisTemplate, new ObjectMapper())
                .find(UUID.randomUUID().toString()).isEmpty());
    }

    @Test
    void deleteRemovesKey() {
        when(redisTemplate.delete(anyString())).thenReturn(true);

        new PendingActionStore(redisTemplate, new ObjectMapper()).delete(UUID.randomUUID().toString());

        verify(redisTemplate).delete(startsWith("admin-ai:pending:"));
    }

    @Test
    void indexLatestAndFindLatestByAdmin() {
        PendingActionStore.PendingAction action = sampleAction();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        PendingActionStore store = new PendingActionStore(redisTemplate, mapper);
        store.save(action);
        store.indexLatest(action.adminId(), action.actionId());

        verify(valueOperations).set(
                eq("admin-ai:pending-admin:" + action.adminId()),
                eq(action.actionId().toString()),
                eq(Duration.ofMinutes(6)));
    }
}
