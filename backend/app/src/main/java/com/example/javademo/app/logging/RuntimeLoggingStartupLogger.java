package com.example.javademo.app.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 用户服务启动日志记录器。
 *
 * <p>该组件在 Spring Boot 启动完成后输出日志基线配置摘要，帮助学习者确认当前服务名、
 * 端口、日志文件和数据库目标是否符合预期。摘要会主动避免输出数据库密码等敏感信息。</p>
 */
@Component
public class RuntimeLoggingStartupLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RuntimeLoggingStartupLogger.class);

    private final Environment environment;

    public RuntimeLoggingStartupLogger(Environment environment) {
        this.environment = environment;
    }

    /**
     * 输出服务启动后的运行日志配置摘要。
     *
     * <p>这里只读取 Spring Environment 中已经解析好的配置值，不做额外文件读写；日志级别本身仍由
     * application.yml 和环境变量控制。</p>
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, appLevel={}, datasource={}",
                environment.getProperty("spring.application.name", "java-demo-app"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/java-demo-app.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.app", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")));
    }

    /**
     * 解析当前激活 profile；没有显式 profile 时用 default 标识 Spring Boot 默认配置。
     */
    private String resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles);
    }

    /**
     * 对可能携带密码的配置值做兜底脱敏。
     *
     * <p>当前 JDBC URL 没有密码，但这里保留脱敏逻辑，是为了后续切换连接串形式时不误把密码写入日志。</p>
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
