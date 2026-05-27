package com.example.javademo.notification.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知服务 OpenAPI 配置。
 *
 * <p>每个业务服务都保留自己的 Swagger UI，便于学习阶段分别调试服务接口。Gateway 聚合多服务
 * OpenAPI 会留到后续版本处理。</p>
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI notificationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Demo Notification API")
                        .version("v0.5.1")
                        .description("Notification APIs for in-app messages and unread count."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
