package com.example.javademo.app.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户服务 Nacos 配置刷新日志。
 *
 * <p>v0.6 接入配置中心后，登录链路相关配置刷新需要留痕，但又不能把密钥、token 等敏感值直接输出。
 * 因此这里只记录变更键名。</p>
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
                environment.getProperty("spring.application.name", "java-demo-app"), changedKeys);
    }

    private boolean isRelevantKey(String key) {
        return key.startsWith("app.jwt.")
                || key.startsWith("java-demo.")
                || key.startsWith("spring.cloud.nacos.");
    }
}
