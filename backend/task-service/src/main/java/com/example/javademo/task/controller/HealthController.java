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
 * <p>该接口不需要 JWT，便于 Gateway 路由验证、脚本探测和 v0.6 Nacos 配置刷新验证。</p>
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
        // 健康检查里补充 v0.6.1 的服务调用模式和目标服务名，便于联调时快速确认已经切到 OpenFeign 主路径。
        data.put("serviceCallMode", "openfeign");
        data.put("userServiceName", environment.getProperty("java-demo.services.user-service-name", "java-demo-app"));
        data.put("notificationServiceName", environment.getProperty("java-demo.services.notification-service-name", "notification-service"));
        data.put("nacosDiscoveryEnabled", environment.getProperty("spring.cloud.nacos.discovery.enabled", Boolean.class, true));
        return ApiResponse.success(data);
    }
}
