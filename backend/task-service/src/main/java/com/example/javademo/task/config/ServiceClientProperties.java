package com.example.javademo.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务下游调用配置。
 *
 * <p>v0.6.1 开始 task-service 通过 OpenFeign + Nacos 服务名访问下游服务，因此这里不再保存静态 URL，
 * 而是只保存两个逻辑服务名，便于启动日志、健康检查和 Feign 声明保持一致。</p>
 */
@ConfigurationProperties(prefix = "java-demo.services")
public class ServiceClientProperties {

    /** 用户服务在 Nacos 中注册的服务名。 */
    private String userServiceName = "java-demo-app";

    /** 通知服务在 Nacos 中注册的服务名。 */
    private String notificationServiceName = "notification-service";

    public String getUserServiceName() {
        return userServiceName;
    }

    public void setUserServiceName(String userServiceName) {
        this.userServiceName = userServiceName;
    }

    public String getNotificationServiceName() {
        return notificationServiceName;
    }

    public void setNotificationServiceName(String notificationServiceName) {
        this.notificationServiceName = notificationServiceName;
    }
}
