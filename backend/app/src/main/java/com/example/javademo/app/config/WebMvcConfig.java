package com.example.javademo.app.config;

import com.example.javademo.app.security.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置。
 *
 * <p>当前主要职责是注册 JWT 认证拦截器。v0.1 还没有引入 Spring Security 过滤器链，
 * 因此使用 HandlerInterceptor 作为轻量认证入口，先把登录闭环跑通。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * 注册接口认证规则。
     *
     * <p>所有 /api/** 默认都要经过 JWT 校验，但健康检查、注册、登录属于公开接口，
     * 必须排除，否则用户在拿到 token 之前就无法注册或登录。</p>
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/register",
                        "/api/auth/login"
                );
    }
}
