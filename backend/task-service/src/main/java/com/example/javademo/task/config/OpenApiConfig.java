package com.example.javademo.task.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 任务服务 OpenAPI 配置。
 *
 * <p>每个服务保留自己的 Swagger UI，可以直接查看任务接口；Gateway 聚合多服务接口文档留到后续版本。</p>
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI taskOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Demo Task API")
                        .version("v0.5.1")
                        .description("Task APIs for task creation, assignment, status flow and notification validation."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
