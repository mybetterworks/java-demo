package com.example.javademo.task.config;

import com.example.javademo.rpc.user.UserRpcService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Dubbo 引用配置。
 *
 * <p>这里把 Dubbo 远程代理包装成普通 Spring Bean，
 * 让业务代码继续只依赖 RPC 接口本身，而不直接感知 Dubbo 注解细节。
 * 这样在测试里也可以继续通过 {@code @MockBean UserRpcService} 替换远程调用。</p>
 */
@Configuration
@Profile("!test")
public class DubboReferenceConfiguration {

    @DubboReference(check = false)
    private UserRpcService dubboUserRpcService;

    /**
     * 暴露用户 RPC 代理，供业务层按普通 Spring 依赖注入使用。
     */
    @Bean("userRpcServiceBridge")
    public UserRpcService userRpcServiceBridge() {
        return dubboUserRpcService;
    }
}
