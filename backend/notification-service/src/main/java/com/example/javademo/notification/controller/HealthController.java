package com.example.javademo.notification.controller;

import com.example.javademo.notification.common.ApiResponse;
import com.example.javademo.notification.websocket.NotificationWebSocketPushService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通知服务健康检查接口。
 *
 * <p>该接口不需要 JWT，便于 Gateway、脚本和 v0.6 Nacos 配置读取验证确认服务是否可用。</p>
 */
@RestController
public class HealthController {

    private final Environment environment;
    private final NotificationWebSocketPushService webSocketPushService;

    public HealthController(Environment environment, NotificationWebSocketPushService webSocketPushService) {
        this.environment = environment;
        this.webSocketPushService = webSocketPushService;
    }

    @GetMapping("/api/notifications/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", environment.getProperty("spring.application.name", "notification-service"));
        data.put("time", OffsetDateTime.now().toString());
        data.put("configSource", environment.getProperty("java-demo.runtime.config-source", "local"));
        data.put("configLabel", environment.getProperty("java-demo.runtime.config-label", "not-configured"));
        data.put("serviceRole", environment.getProperty("java-demo.runtime.service-role", "notification-service"));
        data.put("redisEnabled", environment.getProperty("java-demo.redis.enabled", Boolean.class, true));
        data.put("redisHost", environment.getProperty("spring.data.redis.host", "127.0.0.1"));
        data.put("redisPort", environment.getProperty("spring.data.redis.port", Integer.class, 6379));
        data.put("cacheEnabled", environment.getProperty("java-demo.cache.enabled", Boolean.class, true));
        data.put("notificationUnreadCacheTtlSeconds", environment.getProperty("java-demo.cache.notification-unread-ttl-seconds", Long.class, 60L));
        data.put("rateLimitEnabled", environment.getProperty("java-demo.rate-limit.enabled", Boolean.class, true));
        data.put("webSocketEndpoint", "/ws/notifications");
        data.put("webSocketOnlineSessions", webSocketPushService.totalOnlineSessionCount());
        return ApiResponse.success(data);
    }
}
