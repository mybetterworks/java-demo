package com.example.javademo.app.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO SDK 客户端配置。
 *
 * <p>MinioClient 是线程安全客户端，可以作为单例 Bean 复用。
 * 这里不在日志中输出 accessKey 或 secretKey，只把真正敏感的凭据留在 Spring 配置体系内。</p>
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
