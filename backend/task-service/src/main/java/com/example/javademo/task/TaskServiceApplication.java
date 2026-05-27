package com.example.javademo.task;

import com.example.javademo.task.config.JwtProperties;
import com.example.javademo.task.config.ServiceClientProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 任务服务启动类。
 *
 * <p>v0.5.1 开始把任务领域从原有 java-demo-app 中独立出来，形成真实的微服务边界。
 * 当前仍使用静态 REST 地址调用用户服务和通知服务，后续 v0.6 再迁移到 Nacos 服务发现。</p>
 */
@SpringBootApplication
@MapperScan("com.example.javademo.task.mapper")
@EnableConfigurationProperties({JwtProperties.class, ServiceClientProperties.class})
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
