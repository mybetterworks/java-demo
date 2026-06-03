package com.example.javademo.task.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 下游 REST 调用配置。
 *
 * <p>v0.6 仍然保留 RestTemplate，而不是在同一版本同时引入 OpenFeign。这样可以把学习重点集中在
 * “服务注册发现”和“服务名解析”。为了兼顾真实运行与自动化测试，这里根据配置切换 load-balanced
 * 与普通 RestTemplate 两种模式。</p>
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 真实运行时默认启用的服务发现 RestTemplate。
     *
     * <p>当地址形如 http://java-demo-app 时，LoadBalancer 会先按服务名查找实例，再转发到具体端口。</p>
     */
    @Bean(name = "restTemplate")
    @LoadBalanced
    // prefix表示配置项的前缀，name表示具体的配置项名称，havingValue表示当配置项值为true时才创建这个Bean，matchIfMissing表示如果没有这个配置项时也创建这个Bean。
    @ConditionalOnProperty(prefix = "java-demo.services", name = "discovery-enabled", havingValue = "true", matchIfMissing = true)
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    /**
     * 测试与临时排障时使用的普通 RestTemplate。
     *
     * <p>关闭 discoveryEnabled 后，请求地址会按原样发送，便于使用 MockRestServiceServer 或静态 URL 做回归。</p>
     */
    @Bean(name = "restTemplate")
    @ConditionalOnProperty(prefix = "java-demo.services", name = "discovery-enabled", havingValue = "false")
    public RestTemplate plainRestTemplate() {
        return new RestTemplate();
    }
}
