package com.example.javademo.task.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 下游 REST 调用配置。
 *
 * <p>v0.5.1 刻意使用 Spring 自带 RestTemplate，而不是提前引入 OpenFeign。
 * 这样可以把学习重点放在“服务边界、静态地址、JWT 转发和失败处理”上。</p>
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
