package com.example.javademo.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 创建任务请求。
 *
 * <p>负责人可以不传，此时默认分配给当前登录用户；如果传入负责人 ID，任务服务会调用用户服务校验用户存在。</p>
 */
@Schema(description = "创建任务请求")
public class CreateTaskRequest {

    @NotBlank
    @Size(max = 120)
    @Schema(description = "任务标题", example = "学习 Nacos 服务注册")
    private String title;

    @Size(max = 2000)
    @Schema(description = "任务描述", example = "完成 v0.6 前的服务拆分准备")
    private String description;

    @Positive
    @Schema(description = "负责人用户 ID，不传则默认当前用户", example = "1")
    private Long assigneeUserId;

    @Size(max = 16)
    @Schema(description = "优先级：LOW、MEDIUM、HIGH", example = "MEDIUM")
    private String priority;

    @Schema(description = "截止时间，ISO-8601 格式，例如 2026-05-28T18:00:00")
    private LocalDateTime dueTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueTime() {
        return dueTime;
    }

    public void setDueTime(LocalDateTime dueTime) {
        this.dueTime = dueTime;
    }
}
