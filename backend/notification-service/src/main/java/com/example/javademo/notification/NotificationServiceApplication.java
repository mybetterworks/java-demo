package com.example.javademo.notification;

import com.example.javademo.notification.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 通知服务启动类。
 *
 * <p>v0.5.1 新增 notification-service，用来承载站内信、未读数和已读状态。
 * 当前仍使用本地进程启动，后续接入 Nacos 后会注册为独立服务，供 Gateway 和 task-service
 * 通过服务名访问。</p>
 */
@SpringBootApplication
@MapperScan("com.example.javademo.notification.mapper")
@EnableConfigurationProperties(JwtProperties.class)
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
