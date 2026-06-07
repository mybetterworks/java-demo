package com.example.javademo.app.cache;

import com.example.javademo.app.dto.UserProfileResponse;
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
 * 用户基础资料缓存服务。
 *
 * <p>v0.7 把用户详情、当前用户资料和 task-service 的负责人校验缓存统一到同一组 Redis key。
 * 这样 java-demo-app 更新或删除用户时，只要删除同一个 key，task-service 侧下一次负责人校验也会自动回源
 * Dubbo provider，避免跨服务缓存失效规则各写一套。</p>
 */
@Service
public class UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserCacheService.class);

    /** 用户缓存 key 后缀保持跨服务一致，task-service 会按相同 key 读取负责人摘要。 */
    private static final String USER_SUMMARY_KEY_PART = ":user:summary:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final Map<String, MemoryCacheEntry> memoryFallback = new ConcurrentHashMap<>();

    public UserCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    /**
     * 从缓存读取用户基础资料。
     *
     * <p>Redis 可用时优先读取 Redis；Redis 未启用或临时不可用时使用本进程内存降级。
     * 降级日志会明确标记 fallback，方便联调时判断当前是否真的走到了 Redis。</p>
     */
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
                log.debug("User cache miss, userId={}, cache=redis", userId);
                return Optional.empty();
            }
            log.debug("User cache hit, userId={}, cache=redis", userId);
            return Optional.of(objectMapper.readValue(json, UserProfileResponse.class));
        } catch (Exception exception) {
            log.warn("Redis user cache read failed, userId={}, reason={}, fallback=memory",
                    userId, exception.getClass().getSimpleName());
            return readMemory(key);
        }
    }

    /**
     * 写入用户基础资料缓存。
     *
     * <p>缓存内容只包含 API 已经允许返回给前端的字段，不包含密码哈希、JWT 或 Authorization header。</p>
     */
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
            log.debug("User cache written, userId={}, ttlSeconds={}, cache=redis", user.getId(), ttl.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis user cache write failed, userId={}, reason={}, fallback=memory",
                    user.getId(), exception.getClass().getSimpleName());
            writeMemory(key, user, ttl);
        }
    }

    /**
     * 删除用户基础资料缓存。
     *
     * <p>用户资料、状态、角色、删除标记或密码更新时间变化后都调用该方法。即使 Redis 删除失败，
     * 也会同步清理本地降级缓存，避免同一 JVM 继续读到旧值。</p>
     */
    public void evictUser(Long userId, String reason) {
        if (userId == null) {
            return;
        }
        String key = userKey(userId);
        memoryFallback.remove(key);
        if (!cacheEnabled()) {
            return;
        }
        if (!redisEnabled()) {
            log.info("User cache evicted, userId={}, reason={}, cache=memory", userId, reason);
            return;
        }
        try {
            redisTemplate.delete(key);
            log.info("User cache evicted, userId={}, reason={}, cache=redis", userId, reason);
        } catch (Exception exception) {
            log.warn("Redis user cache eviction failed, userId={}, reason={}, redisReason={}",
                    userId, reason, exception.getClass().getSimpleName());
        }
    }

    /** 当前 Redis 是否作为主缓存启用。 */
    private boolean redisEnabled() {
        return environment.getProperty("java-demo.redis.enabled", Boolean.class, true);
    }

    private boolean cacheEnabled() {
        return environment.getProperty("java-demo.cache.enabled", Boolean.class, true);
    }

    /** 构造跨服务一致的用户缓存 key。 */
    private String userKey(Long userId) {
        return redisKeyPrefix() + USER_SUMMARY_KEY_PART + userId;
    }

    private String redisKeyPrefix() {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_7");
    }

    private Optional<UserProfileResponse> readMemory(String key) {
        MemoryCacheEntry entry = memoryFallback.get(key);
        if (entry == null || entry.isExpired()) {
            memoryFallback.remove(key);
            log.debug("User cache miss, key={}, cache=memory", key);
            return Optional.empty();
        }
        log.debug("User cache hit, key={}, cache=memory", key);
        return Optional.of(entry.value());
    }

    private void writeMemory(String key, UserProfileResponse user, Duration ttl) {
        memoryFallback.put(key, new MemoryCacheEntry(user, Instant.now().plus(ttl)));
        log.debug("User cache written, userId={}, ttlSeconds={}, cache=memory", user.getId(), ttl.toSeconds());
    }

    /** 内存降级缓存条目，只在 Redis 关闭或异常时使用。 */
    private record MemoryCacheEntry(UserProfileResponse value, Instant expiresAt) {
        boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}
