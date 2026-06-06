package com.example.javademo.task;

import com.example.javademo.task.config.JwtProperties;
import com.example.javademo.task.config.ServiceClientProperties;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 任务服务启动类。
 *
 * <p>v0.6 已接入 Nacos 服务发现，v0.6.1 把同步下游调用统一迁移到 OpenFeign。
 * 到 v0.6.2，链路继续演进为混合模式：
 * 负责人用户校验切换到 Dubbo，
 * 通知创建继续保留 OpenFeign。
 * 因此这里需要同时开启 Nacos、Dubbo 和 Feign 的相关扫描能力。</p>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
@EnableFeignClients(basePackages = "com.example.javademo.task.client.feign")
@MapperScan("com.example.javademo.task.mapper")
@EnableConfigurationProperties({JwtProperties.class, ServiceClientProperties.class})
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
