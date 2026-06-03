package com.example.javademo.app;

import com.example.javademo.app.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Java Demo 用户服务启动类。
 *
 * <p>虽然当前模块名称仍然是 app，但从 v0.6 开始它已经作为 Nacos 中的独立用户服务存在，负责登录、
 * 验证码、JWT 与用户管理能力。显式开启服务发现后，Gateway 与 task-service 就能按服务名访问它。</p>
 */
@MapperScan("com.example.javademo.app.mapper")
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(JwtProperties.class)
public class JavaDemoApplication {

    /**
     * 应用主入口，既可通过 IDE 启动，也可通过 Maven 或可执行 jar 启动。
     *
     * @param args 命令行参数，例如 --server.port=8252
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaDemoApplication.class, args);
    }
}
