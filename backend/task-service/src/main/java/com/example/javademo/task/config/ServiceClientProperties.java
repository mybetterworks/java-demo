package com.example.javademo.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 任务服务下游调用配置。
 *
 * <p>v0.6 开始默认通过 Nacos 服务发现访问用户服务和通知服务，因此默认地址从 localhost 静态地址演进为
 * 服务名地址。同时保留 discoveryEnabled 开关，便于自动化测试继续使用普通 RestTemplate 与 Mock 服务。</p>
 */
@ConfigurationProperties(prefix = "java-demo.services")
public class ServiceClientProperties {

    /** 用户服务地址，默认通过服务名访问。 */
    private String userServiceUrl = "http://java-demo-app";

    /** 通知服务地址，默认通过服务名访问。 */
    private String notificationServiceUrl = "http://notification-service";

    /**
     * 是否启用服务发现。
     *
     * <p>真实 v0.6 运行默认值为 true；测试环境和临时静态排障可以显式关闭。</p>
     */
    private boolean discoveryEnabled = true;

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

    public boolean isDiscoveryEnabled() {
        return discoveryEnabled;
    }

    public void setDiscoveryEnabled(boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }
}
