package com.example.javademo.gateway;

import com.example.javademo.gateway.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Cloud Gateway 启动类。
 *
 * <p>v0.5 开始引入统一入口，但业务服务暂时仍然保持在 backend/app 单体应用中。
 * 网关的职责是站在外部流量最前面完成基础 JWT 校验、跨域处理和路由转发；
 * 这样后续接入 Nacos 后，只需要把当前静态后端地址演进为服务发现地址即可。</p>
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
