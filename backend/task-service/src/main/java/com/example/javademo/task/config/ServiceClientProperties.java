package com.example.javademo.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务下游调用地址配置。
 *
 * <p>v0.5.1 先使用静态 URL，方便理解服务间调用的本质。v0.6 接入 Nacos 后，
 * 这些地址会逐步演进为服务发现名称或由配置中心托管。</p>
 */
@ConfigurationProperties(prefix = "java-demo.services")
public class ServiceClientProperties {

    /** 用户服务地址，当前指向 java-demo-app。 */
    private String userServiceUrl = "http://localhost:8091";

    /** 通知服务地址，当前指向 notification-service。 */
    private String notificationServiceUrl = "http://localhost:8094";

    public String getUserServiceUrl() {
        return userServiceUrl;
    }

    public void setUserServiceUrl(String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }

    public String getNotificationServiceUrl() {
        return notificationServiceUrl;
    }

    public void setNotificationServiceUrl(String notificationServiceUrl) {
        this.notificationServiceUrl = notificationServiceUrl;
    }
}
