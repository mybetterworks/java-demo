package com.example.javademo.task.cache;

import com.example.javademo.task.client.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * task-service 负责人用户校验缓存。
 *
 * <p>该缓存使用与 java-demo-app 相同的 Redis key：`user:summary:{userId}`。
 * java-demo-app 在用户更新、删除或改密时会删除同一个 key，因此 task-service 下一次负责人校验会自动回源
 * Dubbo provider，不需要额外服务间失效通知。</p>
 */
@Service
public class UserValidationCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserValidationCacheService.class);

    private static final String USER_SUMMARY_KEY_PART = ":user:summary:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final Map<String, MemoryEntry> memoryFallback = new ConcurrentHashMap<>();

    public UserValidationCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    public Optional<UserProfileResponse> getUser(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        if (!cacheEnabled()) {
            return Optional.empty();
        }
        String key = userKey(userId);
        if (!redisEnabled()) {
            return readMemory(key);
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                log.debug("Task user validation cache miss, userId={}, cache=redis", userId);
                return Optional.empty();
            }
            log.debug("Task user validation cache hit, userId={}, cache=redis", userId);
            return Optional.of(objectMapper.readValue(json, UserProfileResponse.class));
        } catch (Exception exception) {
            log.warn("Redis task user validation cache read failed, userId={}, reason={}, fallback=memory",
                    userId, exception.getClass().getSimpleName());
            return readMemory(key);
        }
    }

    public void putUser(UserProfileResponse user) {
        if (user == null || user.getId() == null) {
            return;
        }
        if (!cacheEnabled()) {
            return;
        }
        String key = userKey(user.getId());
        Duration ttl = Duration.ofSeconds(environment.getProperty("java-demo.cache.user-ttl-seconds", Long.class, 300L));
        if (!redisEnabled()) {
            writeMemory(key, user, ttl);
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(user), ttl);
            log.debug("Task user validation cache written, userId={}, ttlSeconds={}, cache=redis", user.getId(), ttl.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis task user validation cache write failed, userId={}, reason={}, fallback=memory",
                    user.getId(), exception.getClass().getSimpleName());
            writeMemory(key, user, ttl);
        }
    }

    private boolean redisEnabled() {
        return environment.getProperty("java-demo.redis.enabled", Boolean.class, true);
    }

    private boolean cacheEnabled() {
        return environment.getProperty("java-demo.cache.enabled", Boolean.class, true);
    }

    private String userKey(Long userId) {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_8") + USER_SUMMARY_KEY_PART + userId;
    }

    private Optional<UserProfileResponse> readMemory(String key) {
        MemoryEntry entry = memoryFallback.get(key);
        if (entry == null || entry.isExpired()) {
            memoryFallback.remove(key);
            log.debug("Task user validation cache miss, key={}, cache=memory", key);
            return Optional.empty();
        }
        log.debug("Task user validation cache hit, key={}, cache=memory", key);
        return Optional.of(entry.value());
    }

    private void writeMemory(String key, UserProfileResponse user, Duration ttl) {
        memoryFallback.put(key, new MemoryEntry(user, Instant.now().plus(ttl)));
        log.debug("Task user validation cache written, userId={}, ttlSeconds={}, cache=memory", user.getId(), ttl.toSeconds());
    }

    private record MemoryEntry(UserProfileResponse value, Instant expiresAt) {
        boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}
