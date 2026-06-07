package com.example.javademo.app.ratelimit;

import com.example.javademo.app.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * java-demo-app 业务接口限流过滤器。
 *
 * <p>v0.7 先覆盖登录和用户查询两类入口：登录限流按客户端维度保护认证入口，
 * 用户查询限流按网关用户头或客户端 IP 保护用户列表、详情和当前用户接口。</p>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    public RateLimitFilter(RateLimitService rateLimitService, ObjectMapper objectMapper, Environment environment) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        RateLimitRule rule = resolveRule(request);
        if (rule != null && !rateLimitService.allow(rule.bucket(), resolveIdentity(request), rule.limit())) {
            writeTooManyRequests(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if ("POST".equalsIgnoreCase(method) && "/api/auth/login".equals(path)) {
            return new RateLimitRule("login", environment.getProperty("java-demo.rate-limit.login-limit", Integer.class, 20));
        }
        if ("GET".equalsIgnoreCase(method) && ("/api/users".equals(path) || path.startsWith("/api/users/"))) {
            return new RateLimitRule("user-query", environment.getProperty("java-demo.rate-limit.user-query-limit", Integer.class, 120));
        }
        return null;
    }

    private String resolveIdentity(HttpServletRequest request) {
        String gatewayUserId = request.getHeader("X-Gateway-User-Id");
        if (gatewayUserId != null && !gatewayUserId.isBlank()) {
            return "user:" + gatewayUserId.trim();
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return "ip:" + forwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return "ip:" + (remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr);
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(429, "Too many requests, please try again later"));
    }

    private record RateLimitRule(String bucket, int limit) {
    }
}
