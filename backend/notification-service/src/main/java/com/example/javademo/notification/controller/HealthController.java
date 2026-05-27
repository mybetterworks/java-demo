package com.example.javademo.notification.controller;

import com.example.javademo.notification.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通知服务健康检查接口。
 *
 * <p>该接口不需要 JWT，便于 Gateway、脚本和后续 Nacos 健康探测确认服务是否可用。</p>
 */
@RestController
public class HealthController {

    @GetMapping("/api/notifications/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "notification-service"
        ));
    }
}
