package com.example.javademo.task.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 任务服务启动日志记录器。
 *
 * <p>任务服务既依赖数据库，也依赖用户服务与通知服务两个下游，因此启动摘要需要同时输出日志文件、数据源、
 * Feign 目标服务名、Feign 超时与 Nacos 地址，便于快速判断当前是否已经进入 v0.6.1 的
 * OpenFeign + Nacos 主路径。</p>
 */
@Component
public class RuntimeLoggingStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RuntimeLoggingStartupLogger.class);

    private final Environment environment;

    public RuntimeLoggingStartupLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info(
                "Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, taskLevel={}, datasource={}, serviceCallMode={}, userServiceName={}, notificationServiceName={}, feignConnectTimeoutMs={}, feignReadTimeoutMs={}, feignLoggerLevel={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "task-service"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/task-service.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.task", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
                "openfeign",
                sanitizeConfigValue(environment.getProperty("java-demo.services.user-service-name", "java-demo-app")),
                sanitizeConfigValue(environment.getProperty("java-demo.services.notification-service-name", "notification-service")),
                environment.getProperty("spring.cloud.openfeign.client.config.default.connectTimeout", "3000"),
                environment.getProperty("spring.cloud.openfeign.client.config.default.readTimeout", "5000"),
                environment.getProperty("spring.cloud.openfeign.client.config.default.loggerLevel", "basic"),
                environment.getProperty("spring.cloud.nacos.discovery.enabled", "true"),
                environment.getProperty("spring.cloud.nacos.config.enabled", "true"),
                sanitizeConfigValue(environment.getProperty("spring.cloud.nacos.discovery.server-addr", "not-configured")),
                environment.getProperty("java-demo.runtime.config-source", "local"),
                environment.getProperty("java-demo.runtime.config-label", "not-configured")
        );
    }

    private String resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles);
    }

    /**
     * 对下游地址与 JDBC URL 做统一脱敏。
     */
    private String sanitizeConfigValue(String value) {
        if (value == null || value.isBlank()) {
            return "not-configured";
        }
        return value
                .replaceAll("(?i)(password=)[^&;]+", "$1****")
                .replaceAll("(?i)(pwd=)[^&;]+", "$1****");
    }
}
