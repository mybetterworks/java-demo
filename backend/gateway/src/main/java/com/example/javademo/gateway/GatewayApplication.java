package com.example.javademo.gateway;

import com.example.javademo.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Spring Cloud Gateway 启动类。
 *
 * <p>v0.6 开始 Gateway 需要同时承担两件事：一是继续作为统一 JWT 校验入口；二是把原来基于静态地址的路由
 * 升级为 Nacos 服务发现路由。这里显式开启服务发现，方便学习者从启动类就看出“网关已经接入注册中心”。</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(JwtProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
