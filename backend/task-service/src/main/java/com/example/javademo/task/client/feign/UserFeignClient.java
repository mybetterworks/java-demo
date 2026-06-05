package com.example.javademo.task.client.feign;

import com.example.javademo.task.client.UserProfileResponse;
import com.example.javademo.task.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 用户服务 Feign 客户端。
 *
 * <p>v0.6.1 开始 task-service 不再手写用户服务访问 URL，而是直接通过 Nacos 中注册的服务名
 * 调用 java-demo-app。这里只声明任务服务真正需要的最小接口，避免把整个用户模块控制器都复制一遍。</p>
 */
@FeignClient(name = "${java-demo.services.user-service-name:java-demo-app}", path = "/api/users")
public interface UserFeignClient {

    /**
     * 根据用户 ID 查询用户详情。
     *
     * @param id 用户 ID
     * @param authorization 当前登录用户 JWT，对下游继续沿用 Bearer 认证
     * @param requestId 当前链路 requestId，用于跨服务日志串联
     * @return 用户服务统一响应结构
     */
    @GetMapping("/{id:\\d+}")
    ApiResponse<UserProfileResponse> getUser(
            @PathVariable("id") Long id,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(name = "X-Request-Id", required = false) String requestId);
}
