package com.example.javademo.task.ratelimit;

import com.example.javademo.task.common.ApiResponse;
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
 * 任务查询限流过滤器。
 *
 * <p>当前只限制 GET 查询类接口，不限制创建、更新和删除，避免学习阶段的写操作验收被限流干扰。
 * 写接口后续可以在独立安全版本中再补更细的策略。</p>
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
        if (requiresLimit(request) && !rateLimitService.allow(
                "task-query",
                resolveIdentity(request),
                environment.getProperty("java-demo.rate-limit.task-query-limit", Integer.class, 120))) {
            writeTooManyRequests(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresLimit(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod())
                && path.startsWith("/api/tasks")
                && !"/api/tasks/health".equals(path);
    }

    private String resolveIdentity(HttpServletRequest request) {
        String gatewayUserId = request.getHeader("X-Gateway-User-Id");
        if (gatewayUserId != null && !gatewayUserId.isBlank()) {
            return "user:" + gatewayUserId.trim();
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
}
