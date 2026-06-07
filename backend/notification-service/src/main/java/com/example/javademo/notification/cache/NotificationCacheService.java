package com.example.javademo.notification.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知缓存服务。
 *
 * <p>v0.7 先缓存当前用户未读通知数量。未读数是前端首页和通知中心最容易频繁刷新的轻量指标，
 * 用 Redis TTL 缓存可以减少 notification_message 表的 count 查询压力。</p>
 */
@Service
public class NotificationCacheService {

    private static final Logger log = LoggerFactory.getLogger(NotificationCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final Environment environment;
    private final Map<String, MemoryEntry> memoryFallback = new ConcurrentHashMap<>();

    public NotificationCacheService(StringRedisTemplate redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }

    public OptionalLong getUnreadCount(Long userId) {
        if (userId == null) {
            return OptionalLong.empty();
        }
        if (!cacheEnabled()) {
            return OptionalLong.empty();
        }
        String key = unreadKey(userId);
        if (!redisEnabled()) {
            return readMemory(key, userId);
        }
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null || value.isBlank()) {
                log.debug("Notification unread cache miss, userId={}, cache=redis", userId);
                return OptionalLong.empty();
            }
            log.debug("Notification unread cache hit, userId={}, cache=redis", userId);
            return OptionalLong.of(Long.parseLong(value));
        } catch (Exception exception) {
            log.warn("Redis notification unread cache read failed, userId={}, reason={}, fallback=memory",
                    userId, exception.getClass().getSimpleName());
            return readMemory(key, userId);
        }
    }

    public void putUnreadCount(Long userId, long unreadCount) {
        if (userId == null) {
            return;
        }
        if (!cacheEnabled()) {
            return;
        }
        String key = unreadKey(userId);
        Duration ttl = Duration.ofSeconds(environment.getProperty("java-demo.cache.notification-unread-ttl-seconds", Long.class, 60L));
        if (!redisEnabled()) {
            writeMemory(key, unreadCount, ttl, userId);
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, Long.toString(unreadCount), ttl);
            log.debug("Notification unread cache written, userId={}, unreadCount={}, ttlSeconds={}, cache=redis",
                    userId, unreadCount, ttl.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis notification unread cache write failed, userId={}, reason={}, fallback=memory",
                    userId, exception.getClass().getSimpleName());
            writeMemory(key, unreadCount, ttl, userId);
        }
    }

    public void evictUnreadCount(Long userId, String reason) {
        if (userId == null) {
            return;
        }
        String key = unreadKey(userId);
        memoryFallback.remove(key);
        if (!cacheEnabled()) {
            return;
        }
        if (!redisEnabled()) {
            log.info("Notification unread cache evicted, userId={}, reason={}, cache=memory", userId, reason);
            return;
        }
        try {
            redisTemplate.delete(key);
            log.info("Notification unread cache evicted, userId={}, reason={}, cache=redis", userId, reason);
        } catch (Exception exception) {
            log.warn("Redis notification unread cache eviction failed, userId={}, reason={}, redisReason={}",
                    userId, reason, exception.getClass().getSimpleName());
        }
    }

    private OptionalLong readMemory(String key, Long userId) {
        MemoryEntry entry = memoryFallback.get(key);
        if (entry == null || entry.isExpired()) {
            memoryFallback.remove(key);
            log.debug("Notification unread cache miss, userId={}, cache=memory", userId);
            return OptionalLong.empty();
        }
        log.debug("Notification unread cache hit, userId={}, cache=memory", userId);
        return OptionalLong.of(entry.value());
    }

    private void writeMemory(String key, long unreadCount, Duration ttl, Long userId) {
        memoryFallback.put(key, new MemoryEntry(unreadCount, Instant.now().plus(ttl)));
        log.debug("Notification unread cache written, userId={}, unreadCount={}, ttlSeconds={}, cache=memory",
                userId, unreadCount, ttl.toSeconds());
    }

    private boolean redisEnabled() {
        return environment.getProperty("java-demo.redis.enabled", Boolean.class, true);
    }

    private boolean cacheEnabled() {
        return environment.getProperty("java-demo.cache.enabled", Boolean.class, true);
    }

    private String unreadKey(Long userId) {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_8") + ":notification:unread:" + userId;
    }

    private record MemoryEntry(long value, Instant expiresAt) {
        boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}
