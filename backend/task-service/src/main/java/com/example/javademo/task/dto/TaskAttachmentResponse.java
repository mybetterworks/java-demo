package com.example.javademo.task.dto;

import com.example.javademo.task.entity.TaskAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 任务附件响应 DTO。
 *
 * <p>响应中返回 downloadUrl，但不返回 MinIO bucket 或 objectKey。
 * 前端点击下载时仍通过 task-service 代理接口读取对象，保持服务端对对象路径的控制权。</p>
 */
@Schema(description = "任务附件响应")
public class TaskAttachmentResponse {

    private Long id;
    private Long taskId;
    private Long uploaderUserId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime createdAt;

    public static TaskAttachmentResponse from(TaskAttachment attachment) {
        TaskAttachmentResponse response = new TaskAttachmentResponse();
        response.setId(attachment.getId());
        response.setTaskId(attachment.getTaskId());
        response.setUploaderUserId(attachment.getUploaderUserId());
        response.setOriginalFilename(attachment.getOriginalFilename());
        response.setContentType(attachment.getContentType());
        response.setFileSize(attachment.getFileSize());
        response.setDownloadUrl("/api/tasks/" + attachment.getTaskId() + "/attachments/" + attachment.getId() + "/content");
        response.setCreatedAt(attachment.getCreatedAt());
        return response;
    }

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

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
