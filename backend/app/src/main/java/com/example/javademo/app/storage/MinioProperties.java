package com.example.javademo.app.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置。
 *
 * <p>v0.9 把头像和任务附件都放到 MinIO，但仍由各自业务服务负责接口和元数据。
 * 这里集中保存 endpoint、bucket、大小限制等参数，避免上传逻辑里散落环境变量名称。</p>
 */
@Component
@ConfigurationProperties(prefix = "java-demo.minio")
public class MinioProperties {

    /** 是否启用对象存储。测试环境默认关闭，真实上传时由 MockBean 或真实 MinIO 承接。 */
    private boolean enabled = true;

    /** 后端访问 MinIO API 的地址。 */
    private String endpoint = "http://127.0.0.1:9000";

    /** 浏览器直接访问对象存储时可能使用的公开地址；当前头像/附件默认走后端代理。 */
    private String publicEndpoint = "http://127.0.0.1:9000";

    /** MinIO access key，只用于服务端 SDK，不返回给前端。 */
    private String accessKey = "java_demo_minio";

    /** MinIO secret key，只用于服务端 SDK，禁止写入业务日志。 */
    private String secretKey = "java_demo_minio_pwd_123";

    /** 用户头像 bucket。 */
    private String avatarBucket = "java-demo-avatars";

    /** 任务附件 bucket。 */
    private String taskAttachmentBucket = "java-demo-task-attachments";

    /** 头像公开代理 URL 前缀，最终格式为 /api/users/public/avatars/{userId}?v=时间戳。 */
    private String avatarPublicUrlPrefix = "/api/users/public/avatars";

    /** 头像最大字节数，默认 2MB。 */
    private long maxAvatarSizeBytes = 2 * 1024 * 1024L;

    /** 任务附件最大字节数，默认 10MB。 */
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
