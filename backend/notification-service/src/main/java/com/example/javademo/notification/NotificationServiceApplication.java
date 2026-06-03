package com.example.javademo.notification;

import com.example.javademo.notification.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 通知服务启动类。
 *
 * <p>notification-service 从 v0.5.1 起就是独立业务服务；到了 v0.6，它会像其他服务一样注册到 Nacos，
 * 供 Gateway 路由和 task-service 下游调用统一发现。</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.javademo.notification.mapper")
@EnableConfigurationProperties(JwtProperties.class)
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
