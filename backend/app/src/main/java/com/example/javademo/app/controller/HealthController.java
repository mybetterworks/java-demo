package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用健康检查接口。
 *
 * <p>该接口不需要登录，主要用于本地启动验证、Docker/脚本探活以及后续网关接入时的连通性检查。</p>
 */
@Tag(name = "Health", description = "应用健康检查接口")
@RestController
public class HealthController {

    /**
     * 返回应用存活状态。
     *
     * @return 包含 UP 状态和当前服务端时间的统一响应
     */
    @Operation(summary = "健康检查", description = "返回应用存活状态和当前时间。")
    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("time", OffsetDateTime.now().toString());
        return ApiResponse.success(data);
    }
}
