package com.example.javademo.task.client;

/**
 * 调用 notification-service 创建通知的请求对象。
 *
 * <p>该 DTO 是任务服务对通知服务 API 的本地建模，避免直接依赖 notification-service Java 包，
 * 保持微服务之间只通过 HTTP 契约通信。</p>
 */
public class CreateNotificationRequest {

    private Long receiverUserId;
    private String title;
    private String content;
    private String type;
    private String bizType;
    private Long bizId;

    public CreateNotificationRequest() {
    }

    public CreateNotificationRequest(Long receiverUserId, String title, String content, String type, String bizType, Long bizId) {
        this.receiverUserId = receiverUserId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.bizType = bizType;
        this.bizId = bizId;
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
}
