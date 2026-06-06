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
 * <p>任务服务既依赖数据库，也依赖两个下游服务。
 * 到 v0.6.2，它的主路径已经变成“Dubbo 用户校验 + Feign 通知创建”，
 * 因此启动摘要除了记录数据源、Nacos 和日志文件外，
 * 还会把 Dubbo consumer 与 Feign 的关键参数一并打印出来。</p>
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
                "Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, taskLevel={}, datasource={}, serviceCallMode={}, userValidationMode={}, notificationCallMode={}, userServiceName={}, notificationServiceName={}, dubboAppName={}, dubboRegistryAddress={}, dubboRegistryGroup={}, dubboConsumerTimeoutMs={}, dubboConsumerRetries={}, dubboConsumerCheck={}, feignConnectTimeoutMs={}, feignReadTimeoutMs={}, feignLoggerLevel={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "task-service"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/task-service.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.task", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
                "mixed-dubbo-feign",
                environment.getProperty("java-demo.rpc.user-validation-mode", "dubbo"),
                environment.getProperty("java-demo.rpc.notification-mode", "openfeign"),
                sanitizeConfigValue(environment.getProperty("java-demo.services.user-service-name", "java-demo-app")),
                sanitizeConfigValue(environment.getProperty("java-demo.services.notification-service-name", "notification-service")),
                sanitizeConfigValue(environment.getProperty("dubbo.application.name", "task-service")),
                sanitizeConfigValue(environment.getProperty("dubbo.registry.address", "not-configured")),
                environment.getProperty("dubbo.registry.group", "JAVA_DEMO_DUBBO"),
                environment.getProperty("dubbo.consumer.timeout", "3000"),
                environment.getProperty("dubbo.consumer.retries", "0"),
                environment.getProperty("dubbo.consumer.check", "false"),
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
     * 对 JDBC URL、注册中心地址等可能携带凭据的配置做兜底脱敏。
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
