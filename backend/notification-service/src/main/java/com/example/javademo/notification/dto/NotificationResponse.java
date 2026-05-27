package com.example.javademo.notification.dto;

import com.example.javademo.notification.entity.NotificationMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 通知响应对象。
 *
 * <p>不直接把实体暴露给前端，便于后续隐藏内部字段或调整展示字段。</p>
 */
@Schema(description = "通知响应")
public class NotificationResponse {

    private Long id;
    private Long receiverUserId;
    private String title;
    private String content;
    private String type;
    private String bizType;
    private Long bizId;
    private Integer readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public static NotificationResponse from(NotificationMessage message) {
        NotificationResponse response = new NotificationResponse();
        response.setId(message.getId());
        response.setReceiverUserId(message.getReceiverUserId());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setType(message.getType());
        response.setBizType(message.getBizType());
        response.setBizId(message.getBizId());
        response.setReadStatus(message.getReadStatus());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Integer getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(Integer readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
