package com.example.javademo.task.client.feign;

import com.example.javademo.task.client.CreateNotificationRequest;
import com.example.javademo.task.common.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 通知服务 Feign 客户端。
 *
 * <p>任务服务当前只需要调用“创建通知”这一条同步链路，因此这里只保留最小声明。后续如果通知链路演进到
 * MQ 异步事件，这个 Feign 客户端也会成为明确的可替换边界。</p>
 */
@FeignClient(name = "${java-demo.services.notification-service-name:notification-service}", path = "/api/notifications")
public interface NotificationFeignClient {

    /**
     * 创建任务通知。
     *
     * @param request 通知创建请求
     * @param authorization 当前登录用户 JWT
     * @param requestId 当前链路 requestId
     * @return 通知服务统一响应结构
     */
    @PostMapping
    ApiResponse<Object> createNotification(
            @RequestBody CreateNotificationRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader(name = "X-Request-Id", required = false) String requestId);
}
