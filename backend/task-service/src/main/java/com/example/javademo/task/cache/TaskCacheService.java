package com.example.javademo.task.cache;

import com.example.javademo.task.dto.PageResponse;
import com.example.javademo.task.dto.TaskResponse;
import com.fasterxml.jackson.databind.JavaType;
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
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务查询缓存服务。
 *
 * <p>v0.7 缓存“我的任务”、管理端任务分页和任务详情。列表缓存使用版本号参与 key：
 * 任务创建、更新、状态变化或删除时只需要递增版本号，新查询自然读不到旧 key，旧缓存等 TTL 到期后自动清理。
 * 这比在学习项目中扫描删除大量列表 key 更容易理解，也更安全。</p>
 */
@Service
public class TaskCacheService {

    private static final Logger log = LoggerFactory.getLogger(TaskCacheService.class);

    private static final String TASK_LIST_VERSION_KEY_PART = ":task:list:version";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Environment environment;
    private final Map<String, MemoryEntry> memoryFallback = new ConcurrentHashMap<>();
    private final AtomicLong memoryListVersion = new AtomicLong(0);

    public TaskCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    public Optional<PageResponse<TaskResponse>> getPage(String key) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(PageResponse.class, TaskResponse.class);
        return read(key, type, "task page");
    }

    public void putPage(String key, PageResponse<TaskResponse> page) {
        write(key, page, "task page");
    }

    public Optional<TaskResponse> getTask(Long taskId) {
        return read(taskKey(taskId), objectMapper.constructType(TaskResponse.class), "task detail");
    }

    public void putTask(TaskResponse task) {
        if (task != null && task.getId() != null) {
            write(taskKey(task.getId()), task, "task detail");
        }
    }

    public String myTasksKey(Long userId, long current, long size, String status) {
        return prefix() + ":task:my:v" + currentListVersion() + ":" + userId + ":" + current + ":" + size + ":" + normalizePart(status);
    }

    public String taskPageKey(long current, long size, String status, Long assigneeUserId) {
        return prefix() + ":task:page:v" + currentListVersion() + ":" + current + ":" + size + ":" + normalizePart(status) + ":" + (assigneeUserId == null ? "ALL" : assigneeUserId);
    }

    /**
     * 任务变更后失效列表缓存。
     */
    public void invalidateTaskLists(String reason) {
        if (!cacheEnabled()) {
            return;
        }
        if (!redisEnabled()) {
            long version = memoryListVersion.incrementAndGet();
            log.info("Task list cache invalidated, reason={}, version={}, cache=memory", reason, version);
            return;
        }
        try {
            Long version = redisTemplate.opsForValue().increment(listVersionKey());
            log.info("Task list cache invalidated, reason={}, version={}, cache=redis", reason, version);
        } catch (Exception exception) {
            long version = memoryListVersion.incrementAndGet();
            log.warn("Redis task list cache invalidation failed, reason={}, redisReason={}, fallbackVersion={}",
                    reason, exception.getClass().getSimpleName(), version);
        }
    }

    public void evictTask(Long taskId, String reason) {
        if (taskId == null) {
            return;
        }
        String key = taskKey(taskId);
        memoryFallback.remove(key);
        if (!cacheEnabled()) {
            return;
        }
        if (!redisEnabled()) {
            log.info("Task detail cache evicted, taskId={}, reason={}, cache=memory", taskId, reason);
            return;
        }
        try {
            redisTemplate.delete(key);
            log.info("Task detail cache evicted, taskId={}, reason={}, cache=redis", taskId, reason);
        } catch (Exception exception) {
            log.warn("Redis task detail cache eviction failed, taskId={}, reason={}, redisReason={}",
                    taskId, reason, exception.getClass().getSimpleName());
        }
    }

    private <T> Optional<T> read(String key, JavaType type, String label) {
        if (!cacheEnabled()) {
            return Optional.empty();
        }
        if (!redisEnabled()) {
            return readMemory(key, type, label);
        }
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null || json.isBlank()) {
                log.debug("{} cache miss, key={}, cache=redis", label, key);
                return Optional.empty();
            }
            log.debug("{} cache hit, key={}, cache=redis", label, key);
            return Optional.of(objectMapper.readValue(json, type));
        } catch (Exception exception) {
            log.warn("Redis {} cache read failed, key={}, reason={}, fallback=memory",
                    label, key, exception.getClass().getSimpleName());
            return readMemory(key, type, label);
        }
    }

    private void write(String key, Object value, String label) {
        if (!cacheEnabled()) {
            return;
        }
        Duration ttl = Duration.ofSeconds(environment.getProperty("java-demo.cache.task-ttl-seconds", Long.class, 60L));
        if (!redisEnabled()) {
            writeMemory(key, value, ttl, label);
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
            log.debug("{} cache written, key={}, ttlSeconds={}, cache=redis", label, key, ttl.toSeconds());
        } catch (Exception exception) {
            log.warn("Redis {} cache write failed, key={}, reason={}, fallback=memory",
                    label, key, exception.getClass().getSimpleName());
            writeMemory(key, value, ttl, label);
        }
    }

    private <T> Optional<T> readMemory(String key, JavaType type, String label) {
        MemoryEntry entry = memoryFallback.get(key);
        if (entry == null || entry.isExpired()) {
            memoryFallback.remove(key);
            log.debug("{} cache miss, key={}, cache=memory", label, key);
            return Optional.empty();
        }
        log.debug("{} cache hit, key={}, cache=memory", label, key);
        return Optional.of(objectMapper.convertValue(entry.value(), type));
    }

    private void writeMemory(String key, Object value, Duration ttl, String label) {
        memoryFallback.put(key, new MemoryEntry(value, Instant.now().plus(ttl)));
        log.debug("{} cache written, key={}, ttlSeconds={}, cache=memory", label, key, ttl.toSeconds());
    }

    private long currentListVersion() {
        if (!cacheEnabled()) {
            return 0L;
        }
        if (!redisEnabled()) {
            return memoryListVersion.get();
        }
        try {
            String value = redisTemplate.opsForValue().get(listVersionKey());
            if (value == null || value.isBlank()) {
                return 0L;
            }
            return Long.parseLong(value);
        } catch (Exception exception) {
            log.warn("Redis task list cache version read failed, reason={}, fallback=memory",
                    exception.getClass().getSimpleName());
            return memoryListVersion.get();
        }
    }

    private boolean redisEnabled() {
        return environment.getProperty("java-demo.redis.enabled", Boolean.class, true);
    }

    private boolean cacheEnabled() {
        return environment.getProperty("java-demo.cache.enabled", Boolean.class, true);
    }

    private String listVersionKey() {
        return prefix() + TASK_LIST_VERSION_KEY_PART;
    }

    private String taskKey(Long taskId) {
        return prefix() + ":task:detail:" + taskId;
    }

    private String prefix() {
        return environment.getProperty("java-demo.redis.key-prefix", "java-demo:v0_8");
    }

    private String normalizePart(String value) {
        return value == null || value.isBlank() ? "ALL" : value.trim().toUpperCase();
    }

    private record MemoryEntry(Object value, Instant expiresAt) {
        boolean isExpired() {
            return !expiresAt.isAfter(Instant.now());
        }
    }
}
