package com.example.javademo.app.controller;

import com.example.javademo.app.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用健康检查接口。
 *
 * <p>该接口不需要登录，主要用于本地启动验证、Gateway 连通性检查和 Nacos 配置读取检查。
 * 从 v0.6.2 起，它还会返回 Dubbo provider 摘要，便于确认用户校验 RPC 是否已经暴露成功。</p>
 */
@Tag(name = "Health", description = "应用健康检查接口")
@RestController
public class HealthController {

    private final Environment environment;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    /**
     * 返回应用存活状态与当前关键配置摘要。
     *
     * @return 包含存活状态、配置来源和 Dubbo provider 摘要的统一响应
     */
    @Operation(summary = "健康检查", description = "返回应用存活状态、配置来源和 Dubbo provider 摘要。")
    @GetMapping("/api/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("status", "UP");
        data.put("service", environment.getProperty("spring.application.name", "java-demo-app"));
        data.put("time", OffsetDateTime.now().toString());
        data.put("configSource", environment.getProperty("java-demo.runtime.config-source", "local"));
        data.put("configLabel", environment.getProperty("java-demo.runtime.config-label", "not-configured"));
        data.put("serviceRole", environment.getProperty("java-demo.runtime.service-role", "user-service"));
        data.put("jwtExpirationSeconds", environment.getProperty("app.jwt.expiration-seconds", Long.class, 7200L));
        data.put("userValidationProviderMode", environment.getProperty("java-demo.rpc.user-provider-mode", "dubbo"));
        data.put("dubboApplicationName", environment.getProperty("dubbo.application.name", "java-demo-app"));
        data.put("dubboRegistryGroup", environment.getProperty("dubbo.registry.group", "JAVA_DEMO_DUBBO"));
        data.put("dubboProtocolPort", environment.getProperty("dubbo.protocol.port", "20881"));
        return ApiResponse.success(data);
    }
}
