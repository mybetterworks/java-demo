package com.example.javademo.app;

import com.example.javademo.app.config.JwtProperties;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Java Demo 用户服务启动类。
 *
 * <p>虽然当前模块目录名仍然保留为 app，但从 v0.6 开始它已经作为独立用户服务运行。
 * 到 v0.6.2，它除了继续承担登录、JWT 与用户管理职责外，
 * 还需要作为 Dubbo provider 暴露用户校验 RPC，供 task-service 调用。</p>
 */
@MapperScan("com.example.javademo.app.mapper")
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
@EnableConfigurationProperties(JwtProperties.class)
public class JavaDemoApplication {

    /**
     * 应用主入口。
     *
     * @param args 命令行参数，例如 `--server.port=8252`
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaDemoApplication.class, args);
    }
}
