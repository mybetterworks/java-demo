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
 * <p>启动完成后输出日志和数据库配置摘要，方便在多服务同时运行时确认当前进程确实是通知服务，
 * 并且日志会写入预期文件。</p>
 */
@Component
public class RuntimeLoggingStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RuntimeLoggingStartupLogger.class);

    private final Environment environment;

    public RuntimeLoggingStartupLogger(Environment environment) {
        this.environment = environment;
    }

    /**
     * 输出通知服务启动后的日志基线配置。
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, notificationLevel={}, datasource={}",
                environment.getProperty("spring.application.name", "notification-service"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/notification-service.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.notification", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")));
    }

    private String resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles);
    }

    /**
     * 对配置摘要进行兜底脱敏，确保日志里不会出现数据库密码类参数。
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
