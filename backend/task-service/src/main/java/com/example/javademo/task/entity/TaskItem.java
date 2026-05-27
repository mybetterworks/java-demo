package com.example.javademo.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务实体。
 *
 * <p>v0.5.1 只保留任务管理最小字段：创建人、负责人、标题、描述、状态、优先级和截止时间。
 * deleted 使用 MyBatis Plus 逻辑删除，后续审计、搜索和事件回放都可以基于保留数据继续扩展。</p>
 */
@TableName("task_item")
public class TaskItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 创建人用户 ID，对应 java-demo-app 的 sys_user.id。 */
    private Long creatorUserId;

    /** 负责人用户 ID，对应 java-demo-app 的 sys_user.id。 */
    private Long assigneeUserId;

    /** 任务状态：TODO、IN_PROGRESS、DONE、CANCELLED。 */
    private String status;

    /** 优先级：LOW、MEDIUM、HIGH。 */
    private String priority;

    private LocalDateTime dueTime;

    /** 逻辑删除标记，0 未删除，1 已删除。 */
    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(Long creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    public Long getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(Long assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
