package com.example.javademo.notification.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 通知服务启动日志记录器。
 *
 * <p>通知服务在 v0.6 也需要进入 Nacos 注册发现与配置中心体系，因此启动日志除了原有数据源摘要外，
 * 还要补充 Nacos 地址与当前配置来源信息。</p>
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
                "Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, notificationLevel={}, datasource={}, redisEnabled={}, redisAddress={}, cacheEnabled={}, notificationUnreadCacheTtlSeconds={}, rateLimitEnabled={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "notification-service"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/notification-service.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.notification", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
                environment.getProperty("java-demo.redis.enabled", "true"),
                environment.getProperty("spring.data.redis.host", "127.0.0.1") + ":" + environment.getProperty("spring.data.redis.port", "6379"),
                environment.getProperty("java-demo.cache.enabled", "true"),
                environment.getProperty("java-demo.cache.notification-unread-ttl-seconds", "60"),
                environment.getProperty("java-demo.rate-limit.enabled", "true"),
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
     * 对配置摘要做兜底脱敏，确保日志里不会出现数据库密码之类的敏感参数。
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
