package com.example.javademo.task;

import com.example.javademo.task.config.JwtProperties;
import com.example.javademo.task.config.ServiceClientProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 任务服务启动类。
 *
 * <p>v0.6 的关键变化之一，是把 task-service 对用户服务和通知服务的访问从“静态地址 + RestTemplate”
 * 演进为“服务发现 + RestTemplate”。因此这里除了保留 JWT 与下游地址配置外，还显式开启 Nacos 服务发现。</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.example.javademo.task.mapper")
@EnableConfigurationProperties({JwtProperties.class, ServiceClientProperties.class})
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
