package com.example.javademo.task.controller;

import com.example.javademo.task.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 任务服务健康检查接口。
 *
 * <p>该接口不需要 JWT，便于 Gateway 路由验证、脚本探测和后续 Nacos 健康检查。</p>
 */
@RestController
public class HealthController {

    @GetMapping("/api/tasks/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success(Map.of(
                "status", "UP",
                "service", "task-service"
        ));
    }
}
