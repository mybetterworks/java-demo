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
 * <p>v0.6 接入 Nacos 后，除了保留原有日志文件、端口和数据源摘要，还需要额外记录注册中心与配置中心地址、
 * 当前配置来源和配置标签，便于排查“服务已启动但没有注册上去”或“读取到的仍是本地默认配置”等问题。</p>
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
                "Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, appLevel={}, datasource={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "java-demo-app"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/java-demo-app.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.app", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
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
     * 对可能携带凭据的配置值做兜底脱敏。
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
