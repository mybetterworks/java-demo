package com.example.javademo.task.config;

import com.example.javademo.task.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 任务服务 MVC 配置。
 *
 * <p>任务业务接口都需要 JWT；健康检查放行给 Gateway、脚本和后续注册中心使用。</p>
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
                .addPathPatterns("/api/tasks/**")
                .excludePathPatterns("/api/tasks/health");
    }
}
