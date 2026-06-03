package com.example.javademo.task.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务服务 Nacos 配置刷新日志。
 *
 * <p>任务服务既依赖 JWT 配置，也依赖下游服务地址配置，因此在配置刷新时要明确记录哪些键发生了变化，
 * 方便排查“服务已注册但下游调用仍失败”的问题。</p>
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
                environment.getProperty("spring.application.name", "task-service"), changedKeys);
    }

    private boolean isRelevantKey(String key) {
        return key.startsWith("app.jwt.")
                || key.startsWith("java-demo.")
                || key.startsWith("spring.cloud.nacos.");
    }
}
