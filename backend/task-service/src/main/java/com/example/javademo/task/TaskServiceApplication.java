package com.example.javademo.task;

import com.example.javademo.task.config.JwtProperties;
import com.example.javademo.task.config.ServiceClientProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 任务服务启动类。
 *
 * <p>v0.6 先完成了 Nacos 服务发现，v0.6.1 再把 task-service 对用户服务和通知服务的同步 HTTP 调用
 * 从手写 RestTemplate 演进为 OpenFeign。因此这里除了继续开启 Nacos 服务发现外，还要显式开启
 * Feign Client 扫描。</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.javademo.task.client.feign")
@MapperScan("com.example.javademo.task.mapper")
@EnableConfigurationProperties({JwtProperties.class, ServiceClientProperties.class})
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
