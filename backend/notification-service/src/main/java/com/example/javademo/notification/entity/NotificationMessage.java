package com.example.javademo.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 站内信实体。
 *
 * <p>notification-service 在 v0.5.1 只保存最小通知字段：接收人、标题、内容、类型、业务来源和已读状态。
 * 后续接入 WebSocket、MQ、搜索和可观测性时，都可以围绕这张表继续扩展。</p>
 */
@TableName("notification_message")
public class NotificationMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收通知的用户 ID，对应 java-demo-app 的 sys_user.id。 */
    private Long receiverUserId;

    private String title;

    private String content;

    /** 通知类型：SYSTEM、TASK、USER。 */
    private String type;

    /** 业务类型，例如 TASK 或 USER_ACCOUNT，用于后续搜索和跳转。 */
    private String bizType;

    /** 关联业务 ID，例如任务 ID。 */
    private Long bizId;

    /** 已读状态，0 未读，1 已读。 */
    private Integer readStatus;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;

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
