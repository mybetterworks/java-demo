package com.example.javademo.notification.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知服务 Nacos 配置刷新日志。
 *
 * <p>通知服务虽然当前还没有复杂的可视化配置项，但 v0.6 需要证明它已经接入配置中心并且能够在刷新时输出
 * 非敏感诊断信息。</p>
 */
@Component
public class NacosConfigRefreshLogger {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigRefreshLogger.class);

    private final Environment environment;

    public NacosConfigRefreshLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(EnvironmentChangeEvent.class)
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        List<String> changedKeys = event.getKeys().stream()
                .filter(this::isRelevantKey)
                .sorted()
                .toList();
        if (changedKeys.isEmpty()) {
            return;
        }
        log.info("Nacos config refreshed, serviceName={}, changedKeys={}",
                environment.getProperty("spring.application.name", "notification-service"), changedKeys);
    }

    private boolean isRelevantKey(String key) {
        return key.startsWith("app.jwt.")
                || key.startsWith("java-demo.")
                || key.startsWith("spring.cloud.nacos.");
    }
}
