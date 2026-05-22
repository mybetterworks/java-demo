package com.example.javademo.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI 配置。
 *
 * <p>Springdoc 会根据 Controller 和 DTO 上的注解自动生成接口文档。
 * 这里额外声明 Bearer JWT 安全方案，让 Swagger UI 页面可以显示 Authorize 按钮，
 * 方便在浏览器里直接调试需要登录的接口。</p>
 */
@Configuration
public class OpenApiConfig {

    /** Swagger UI 中显示的 JWT 认证方案名称，Controller 注解会引用这个常量。 */
    public static final String BEARER_AUTH = "bearerAuth";

    /**
     * 定义 OpenAPI 基本信息和 JWT 安全方案。
     *
     * @return Springdoc 使用的 OpenAPI 描述对象
     */
    @Bean
    public OpenAPI javaDemoOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Demo API")
                        .version("v0.1")
                        .description("MVP login APIs for the Java microservice learning project."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
