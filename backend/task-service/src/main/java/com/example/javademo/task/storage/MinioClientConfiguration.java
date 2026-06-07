package com.example.javademo.task.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * task-service 的 MinIO SDK 客户端配置。
 *
 * <p>MinioClient 作为单例复用，上传和下载任务附件时都走同一个客户端。
 * 配置日志由启动摘要记录，但不会输出 secretKey。</p>
 */
@Configuration
public class MinioClientConfiguration {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
