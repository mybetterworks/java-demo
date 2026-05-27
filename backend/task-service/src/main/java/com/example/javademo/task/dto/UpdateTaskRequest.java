package com.example.javademo.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * 更新任务请求。
 *
 * <p>字段均为可选；传入的字段会覆盖现有任务字段。标题如果传入则不能为空白，负责人变化时会重新校验用户存在。</p>
 */
@Schema(description = "更新任务请求")
public class UpdateTaskRequest {

    @Size(max = 120)
    @Schema(description = "任务标题", example = "学习 Nacos 服务注册和配置中心")
    private String title;

    @Size(max = 2000)
    @Schema(description = "任务描述")
    private String description;

    @Positive
    @Schema(description = "负责人用户 ID", example = "2")
    private Long assigneeUserId;

    @Size(max = 16)
    @Schema(description = "优先级：LOW、MEDIUM、HIGH", example = "HIGH")
    private String priority;

    @Schema(description = "截止时间，ISO-8601 格式")
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
