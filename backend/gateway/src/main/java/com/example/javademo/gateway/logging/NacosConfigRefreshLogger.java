package com.example.javademo.gateway.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Gateway Nacos 配置刷新日志。
 *
 * <p>配置中心刷新时不记录具体值，只记录发生变化的键名，避免把 JWT 密钥等敏感配置写到日志里，同时仍然保留
 * 足够的排障线索。</p>
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
                environment.getProperty("spring.application.name", "java-demo-gateway"), changedKeys);
    }

    private boolean isRelevantKey(String key) {
        return key.startsWith("app.jwt.")
                || key.startsWith("java-demo.")
                || key.startsWith("spring.cloud.nacos.");
    }
}
