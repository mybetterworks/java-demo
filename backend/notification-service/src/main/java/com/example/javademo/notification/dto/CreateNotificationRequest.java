package com.example.javademo.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建通知请求。
 *
 * <p>该接口既可被 task-service 调用，也可在 Swagger 中手动调试。v0.5.1 仍使用 JWT 保护，
 * 服务间调用时由 task-service 转发当前用户 token。</p>
 */
@Schema(description = "创建通知请求")
public class CreateNotificationRequest {

    @NotNull
    @Schema(description = "接收人用户 ID", example = "1")
    private Long receiverUserId;

    @NotBlank
    @Size(max = 120)
    @Schema(description = "通知标题", example = "你有一个新任务")
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Schema(description = "通知内容", example = "任务 1 已分配给你，请及时处理。")
    private String content;

    @Size(max = 32)
    @Schema(description = "通知类型：SYSTEM、TASK、USER", example = "TASK")
    private String type;

    @Size(max = 64)
    @Schema(description = "业务类型", example = "TASK")
    private String bizType;

    @Schema(description = "关联业务 ID", example = "100")
    private Long bizId;

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
