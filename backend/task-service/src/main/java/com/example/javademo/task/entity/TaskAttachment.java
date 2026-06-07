package com.example.javademo.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 任务附件元数据实体。
 *
 * <p>附件文件内容保存在 MinIO，数据库只保存与任务的关系、展示文件名、对象 key、MIME 类型和大小。
 * 这种“元数据进库、二进制进对象存储”的设计可以让任务详情查询保持轻量，也方便后续审计和搜索。</p>
 */
@TableName("task_attachment")
public class TaskAttachment {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联任务 ID。 */
    private Long taskId;

    /** 上传者用户 ID，来自 JWT。 */
    private Long uploaderUserId;

    /** 用户看到的原始文件名，已经做过基础清洗。 */
    private String originalFilename;

    /** MinIO 对象 key，只在服务端使用，不让前端直接拼 bucket 路径。 */
    private String objectKey;

    /** 文件 MIME 类型，用于下载响应头。 */
    private String contentType;

    /** 文件大小，单位字节。 */
    private Long fileSize;

    /** 逻辑删除标记，当前 v0.9 暂未暴露删除接口，预留给后续审计。 */
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

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getUploaderUserId() {
        return uploaderUserId;
    }

    public void setUploaderUserId(Long uploaderUserId) {
        this.uploaderUserId = uploaderUserId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
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
