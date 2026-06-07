package com.example.javademo.notification.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * notification-service 固定窗口限流服务。
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redisTemplate;
    private final Environment environment;
    private final Map<String, MemoryWindow> memoryWindows = new ConcurrentHashMap<>();

    public RateLimitService(StringRedisTemplate redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }

    public boolean allow(String bucket, String identity, int limit) {
        if (!environment.getProperty("java-demo.rate-limit.enabled", Boolean.class, true)) {
            return true;
        }
        Duration window = Duration.ofSeconds(environment.getProperty("java-demo.rate-limit.window-seconds", Long.class, 60L));
        String key = redisKey(bucket, identity);
        if (!environment.getProperty("java-demo.redis.enabled", Boolean.class, true)) {
            return allowInMemory(key, bucket, limit, window);
        }
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, window);
            }
            boolean allowed = count == null || count <= limit;
            if (!allowed) {
                log.warn("Rate limit triggered, bucket={}, identityHash={}, count={}, limit={}, windowSeconds={}, cache=redis",
                        bucket, hash(identity), count, limit, window.toSeconds());
            }
            return allowed;
        } catch (Exception exception) {
            log.warn("Redis rate limit failed, bucket={}, reason={}, fallback=memory",
                    bucket, exception.getClass().getSimpleName());
            return allowInMemory(key, bucket, limit, window);
        }
    }

    private boolean allowInMemory(String key, String bucket, int limit, Duration window) {
        MemoryWindow memoryWindow = memoryWindows.compute(key, (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || current.expiresAt().isBefore(now)) {
                return new MemoryWindow(new AtomicInteger(1), now.plus(window));
            }
            current.counter().incrementAndGet();
            return current;
        });
        int count = memoryWindow.counter().get();
        boolean allowed = count <= limit;
        if (!allowed) {
            log.warn("Rate limit triggered, bucket={}, count={}, limit={}, windowSeconds={}, cache=memory",
                    bucket, count, limit, window.toSeconds());
        }
        return allowed;
    }

    private String redisKey(String bucket, String identity) {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_9") + ":rate:" + bucket + ":" + hash(identity);
    }

    private String hash(String value) {
        String normalized = value == null || value.isBlank() ? "anonymous" : value.trim();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                builder.append(String.format("%02x", bytes[i]));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            return Integer.toHexString(normalized.hashCode());
        }
    }

    private record MemoryWindow(AtomicInteger counter, Instant expiresAt) {
    }
}
