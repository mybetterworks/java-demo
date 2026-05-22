package com.example.javademo.app;

import com.example.javademo.app.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Java Demo 后端应用启动类。
 *
 * <p>当前 v0.1 采用单体 Spring Boot 应用承载登录闭环，后续 milestone 再逐步演进为
 * 网关、服务注册、拆分服务等微服务形态。这里同时开启 MyBatis Mapper 扫描和 JWT 配置绑定，
 * 让应用启动时可以自动发现数据访问层并加载 app.jwt 下的配置。</p>
 */
@MapperScan("com.example.javademo.app.mapper")
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JavaDemoApplication {

    /**
     * 应用主入口，本地开发时可以直接从 IDE 运行，也可以通过 Maven 或可执行 jar 启动。
     *
     * @param args 命令行参数，例如临时覆盖端口：--server.port=18080
     */
    public static void main(String[] args) {
        SpringApplication.run(JavaDemoApplication.class, args);
    }
}
