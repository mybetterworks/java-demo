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
 * <p>v0.6.2 起，java-demo-app 除了继续承担用户 REST 能力外，
 * 还要同时作为 Dubbo provider 暴露用户校验 RPC。
 * 因此启动日志除了记录 Nacos、数据源和日志文件外，
 * 还会额外输出 Dubbo application、registry 和 protocol 信息。</p>
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
                "Runtime logging initialized, serviceName={}, port={}, profiles={}, logFile={}, rootLevel={}, appLevel={}, datasource={}, userValidationProviderMode={}, dubboAppName={}, dubboRegistryAddress={}, dubboRegistryGroup={}, dubboProtocolName={}, dubboProtocolPort={}, nacosDiscoveryEnabled={}, nacosConfigEnabled={}, nacosServerAddr={}, configSource={}, configLabel={}",
                environment.getProperty("spring.application.name", "java-demo-app"),
                environment.getProperty("local.server.port", environment.getProperty("server.port", "unknown")),
                resolveProfiles(),
                environment.getProperty("logging.file.name", "logs/java-demo-app.log"),
                environment.getProperty("logging.level.root", "INFO"),
                environment.getProperty("logging.level.com.example.javademo.app", "INFO"),
                sanitizeConfigValue(environment.getProperty("spring.datasource.url", "not-configured")),
                environment.getProperty("java-demo.rpc.user-provider-mode", "dubbo"),
                sanitizeConfigValue(environment.getProperty("dubbo.application.name", "java-demo-app")),
                sanitizeConfigValue(environment.getProperty("dubbo.registry.address", "not-configured")),
                environment.getProperty("dubbo.registry.group", "JAVA_DEMO_DUBBO"),
                environment.getProperty("dubbo.protocol.name", "dubbo"),
                environment.getProperty("dubbo.protocol.port", "20881"),
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
