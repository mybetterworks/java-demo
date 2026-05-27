package com.example.javademo.notification.config;

import com.example.javademo.notification.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 通知服务 MVC 配置。
 *
 * <p>所有通知业务接口都需要 JWT；健康检查开放给 Gateway、脚本和后续注册中心探测使用。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/notifications/**")
                .excludePathPatterns("/api/notifications/health");
    }
}
