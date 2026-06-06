package com.example.javademo.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务下游调用配置。
 *
 * <p>v0.6.1 开始 task-service 只再保存下游服务的逻辑名称，不再维护静态 URL。
 * 到 v0.6.2，用户校验虽然切换到了 Dubbo，
 * 但用户服务名仍然会出现在健康检查、启动日志和联调说明中；
 * 通知服务名则继续供 OpenFeign 主路径使用。</p>
 */
@ConfigurationProperties(prefix = "java-demo.services")
public class ServiceClientProperties {

    /** 用户服务逻辑名称。 */
    private String userServiceName = "java-demo-app";

    /** 通知服务逻辑名称。 */
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
