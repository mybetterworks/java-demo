package com.example.javademo.task.controller;

import com.example.javademo.task.common.ApiResponse;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 任务服务健康检查接口。
 *
 * <p>该接口不需要 JWT，主要用于 Gateway 路由验证、脚本探测与 Nacos 配置加载检查。
 * v0.6.2 以后，它还会显式暴露“用户 Dubbo + 通知 Feign”的运行摘要，
 * 便于联调时快速确认当前主路径。</p>
 */
@RestController
public class HealthController {

    private final Environment environment;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/api/tasks/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", environment.getProperty("spring.application.name", "task-service"));
        data.put("time", OffsetDateTime.now().toString());
        data.put("configSource", environment.getProperty("java-demo.runtime.config-source", "local"));
        data.put("configLabel", environment.getProperty("java-demo.runtime.config-label", "not-configured"));
        data.put("serviceRole", environment.getProperty("java-demo.runtime.service-role", "task-service"));
        data.put("serviceCallMode", "mixed-dubbo-feign");
        data.put("userValidationMode", environment.getProperty("java-demo.rpc.user-validation-mode", "dubbo"));
        data.put("notificationCallMode", environment.getProperty("java-demo.rpc.notification-mode", "openfeign"));
        data.put("userServiceName", environment.getProperty("java-demo.services.user-service-name", "java-demo-app"));
        data.put("notificationServiceName", environment.getProperty("java-demo.services.notification-service-name", "notification-service"));
        data.put("redisEnabled", environment.getProperty("java-demo.redis.enabled", Boolean.class, true));
        data.put("redisHost", environment.getProperty("spring.data.redis.host", "127.0.0.1"));
        data.put("redisPort", environment.getProperty("spring.data.redis.port", Integer.class, 6379));
        data.put("cacheEnabled", environment.getProperty("java-demo.cache.enabled", Boolean.class, true));
        data.put("userCacheTtlSeconds", environment.getProperty("java-demo.cache.user-ttl-seconds", Long.class, 300L));
        data.put("taskCacheTtlSeconds", environment.getProperty("java-demo.cache.task-ttl-seconds", Long.class, 60L));
        data.put("rateLimitEnabled", environment.getProperty("java-demo.rate-limit.enabled", Boolean.class, true));
        data.put("dubboApplicationName", environment.getProperty("dubbo.application.name", "task-service"));
        data.put("dubboRegistryGroup", environment.getProperty("dubbo.registry.group", "JAVA_DEMO_DUBBO"));
        data.put("dubboConsumerTimeoutMs", environment.getProperty("dubbo.consumer.timeout", "3000"));
        data.put("nacosDiscoveryEnabled", environment.getProperty("spring.cloud.nacos.discovery.enabled", Boolean.class, true));
        return ApiResponse.success(data);
    }
}
