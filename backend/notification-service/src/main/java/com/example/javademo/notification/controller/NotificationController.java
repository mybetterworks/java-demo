package com.example.javademo.notification.controller;

import com.example.javademo.notification.common.ApiResponse;
import com.example.javademo.notification.config.OpenApiConfig;
import com.example.javademo.notification.dto.CreateNotificationRequest;
import com.example.javademo.notification.dto.NotificationResponse;
import com.example.javademo.notification.dto.PageResponse;
import com.example.javademo.notification.security.CurrentUserContext;
import com.example.javademo.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 通知接口。
 *
 * <p>所有业务接口都需要 JWT。创建接口当前允许已登录用户调用，主要用于 task-service 转发当前用户 token
 * 创建任务通知；后续可以升级为服务间鉴权或消息队列。</p>
 */
@Tag(name = "Notifications", description = "站内信通知接口")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "创建通知", description = "创建站内信通知，当前主要供 task-service 调用。")
    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return ApiResponse.success("created", notificationService.createNotification(request, CurrentUserContext.getRequired()));
    }

    @Operation(summary = "我的通知", description = "分页查询当前登录用户收到的通知。")
    @GetMapping("/my")
    public ApiResponse<PageResponse<NotificationResponse>> myNotifications(
            @Parameter(description = "当前页码，从 1 开始") @RequestParam(name = "current", defaultValue = "1") Long current,
            @Parameter(description = "每页条数，最大 100") @RequestParam(name = "size", defaultValue = "10") Long size,
            @Parameter(description = "已读状态，0 未读，1 已读") @RequestParam(name = "readStatus", required = false) Integer readStatus) {
        return ApiResponse.success(notificationService.pageMyNotifications(CurrentUserContext.getRequired(), current, size, readStatus));
    }

    @Operation(summary = "我的未读数量", description = "查询当前登录用户未读通知数量。")
    @GetMapping("/my/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount() {
        long count = notificationService.countMyUnread(CurrentUserContext.getRequired());
        return ApiResponse.success(Map.of("count", count));
    }

    @Operation(summary = "标记单条已读", description = "把当前用户的一条通知标记为已读。")
    @PutMapping("/{id:\\d+}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable("id") Long id) {
        return ApiResponse.success("read", notificationService.markRead(id, CurrentUserContext.getRequired()));
    }

    @Operation(summary = "全部已读", description = "把当前用户的全部未读通知标记为已读。")
    @PutMapping("/read-all")
    public ApiResponse<Map<String, Long>> markAllRead() {
        long count = notificationService.markAllRead(CurrentUserContext.getRequired());
        return ApiResponse.success("read all", Map.of("count", count));
    }
}
