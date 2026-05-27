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
 * <p>任务服务依赖用户服务和通知服务，因此启动摘要除了日志文件和数据库外，还会记录下游服务地址。
 * 摘要只包含地址和级别，不输出任何 token、数据库密码或 Authorization header。</p>
 */
@Component
public class RuntimeLoggingStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RuntimeLoggingStartupLogger.class);

    private final Environment environment;

    public RuntimeLoggingStartupLogger(Environment environment) {
        this.environment = environment;
    }

    /**
     * 输出任务服务启动后的关键运行配置。
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, taskLevel={}, datasource={}, userServiceUrl={}, notificationServiceUrl={}",
                environment.getProperty("spring.application.name", "task-service"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/task-service.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.task", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
                sanitizeConfigValue(environment.getProperty("java-demo.services.user-service-url", "not-configured")),
                sanitizeConfigValue(environment.getProperty("java-demo.services.notification-service-url", "not-configured")));
    }

    private String resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles);
    }

    /**
     * 为后续可能带账号密码的 URL 做统一脱敏，当前静态服务地址不会携带敏感信息。
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
