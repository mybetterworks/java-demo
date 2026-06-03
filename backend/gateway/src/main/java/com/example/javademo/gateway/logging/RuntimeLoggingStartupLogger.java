package com.example.javademo.gateway.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Gateway 启动日志记录器。
 *
 * <p>v0.6 开始 Gateway 不再主要依赖静态 URI，而是需要通过 Nacos 服务发现把请求路由到用户、任务和通知服务。
 * 因此这里会在启动完成后输出 Nacos 地址、路由目标和日志文件位置，便于排查“服务已注册但网关没路由过去”
 * 这类典型联调问题。</p>
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
                "Gateway runtime initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, gatewayLevel={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, backendRoute={}, taskRoute={}, notificationRoute={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "java-demo-gateway"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/java-demo-gateway.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.gateway", "INFO"),
                environment.getProperty("spring.cloud.nacos.discovery.enabled", "true"),
                environment.getProperty("spring.cloud.nacos.config.enabled", "true"),
                sanitizeConfigValue(environment.getProperty("spring.cloud.nacos.discovery.server-addr", "not-configured")),
                sanitizeConfigValue(environment.getProperty("spring.cloud.gateway.routes[0].uri", "not-configured")),
                sanitizeConfigValue(environment.getProperty("spring.cloud.gateway.routes[1].uri", "not-configured")),
                sanitizeConfigValue(environment.getProperty("spring.cloud.gateway.routes[2].uri", "not-configured")),
                environment.getProperty("java-demo.runtime.config-source", "local"),
                environment.getProperty("java-demo.runtime.config-label", "not-configured")
        );
    }

    private String resolveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        return activeProfiles.length == 0 ? "default" : String.join(",", activeProfiles);
    }

    /**
     * 对可能带凭据的地址做兜底脱敏。
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
