package com.example.javademo.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统广播请求。
 *
 * <p>该 DTO 用于 v0.8 WebSocket 验证：已登录用户可以触发一条不入库的实时系统消息，
 * 后端会把它广播给当前 notification-service 实例上的所有在线 WebSocket 连接。</p>
 */
public class SystemBroadcastRequest {

    @NotBlank(message = "Broadcast title must not be blank")
    @Size(max = 80, message = "Broadcast title must not exceed 80 characters")
    private String title;

    @NotBlank(message = "Broadcast content must not be blank")
    @Size(max = 500, message = "Broadcast content must not exceed 500 characters")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
