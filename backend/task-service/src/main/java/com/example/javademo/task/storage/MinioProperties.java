package com.example.javademo.task.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * task-service 使用的 MinIO 配置。
 *
 * <p>配置项与 java-demo-app 保持同名，便于在 Docker/Nacos 中用同一组环境变量管理。
 * task-service 当前只使用任务附件 bucket 和附件大小限制，但保留头像 bucket 字段可以让健康检查摘要一致。</p>
 */
@Component
@ConfigurationProperties(prefix = "java-demo.minio")
public class MinioProperties {

    private boolean enabled = true;
    private String endpoint = "http://127.0.0.1:9000";
    private String publicEndpoint = "http://127.0.0.1:9000";
    private String accessKey = "java_demo_minio";
    private String secretKey = "java_demo_minio_pwd_123";
    private String avatarBucket = "java-demo-avatars";
    private String taskAttachmentBucket = "java-demo-task-attachments";
    private String avatarPublicUrlPrefix = "/api/users/public/avatars";
    private long maxAvatarSizeBytes = 2 * 1024 * 1024L;
    private long maxTaskAttachmentSizeBytes = 10 * 1024 * 1024L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getPublicEndpoint() {
        return publicEndpoint;
    }

    public void setPublicEndpoint(String publicEndpoint) {
        this.publicEndpoint = publicEndpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getAvatarBucket() {
        return avatarBucket;
    }

    public void setAvatarBucket(String avatarBucket) {
        this.avatarBucket = avatarBucket;
    }

    public String getTaskAttachmentBucket() {
        return taskAttachmentBucket;
    }

    public void setTaskAttachmentBucket(String taskAttachmentBucket) {
        this.taskAttachmentBucket = taskAttachmentBucket;
    }

    public String getAvatarPublicUrlPrefix() {
        return avatarPublicUrlPrefix;
    }

    public void setAvatarPublicUrlPrefix(String avatarPublicUrlPrefix) {
        this.avatarPublicUrlPrefix = avatarPublicUrlPrefix;
    }

    public long getMaxAvatarSizeBytes() {
        return maxAvatarSizeBytes;
    }

    public void setMaxAvatarSizeBytes(long maxAvatarSizeBytes) {
        this.maxAvatarSizeBytes = maxAvatarSizeBytes;
    }

    public long getMaxTaskAttachmentSizeBytes() {
        return maxTaskAttachmentSizeBytes;
    }

    public void setMaxTaskAttachmentSizeBytes(long maxTaskAttachmentSizeBytes) {
        this.maxTaskAttachmentSizeBytes = maxTaskAttachmentSizeBytes;
    }
}
