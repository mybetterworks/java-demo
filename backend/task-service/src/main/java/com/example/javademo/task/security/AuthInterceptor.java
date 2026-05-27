package com.example.javademo.task.security;

import com.example.javademo.task.common.ApiResponse;
import com.example.javademo.task.common.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 任务服务 JWT 拦截器。
 *
 * <p>该拦截器负责从 Authorization header 中解析 Bearer token，并把用户信息写入当前线程上下文。
 * 日志中不打印 token 本身，避免敏感信息泄露。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            // 任务接口必须携带 token；这里只记录失败摘要，避免把 Authorization 头写入日志。
            log.warn("Task authentication failed, reason=missing_bearer_token, method={}, path={}",
                    request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, "Missing bearer token");
            return false;
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            AuthUser authUser = jwtService.parseToken(token);
            CurrentUserContext.set(authUser);
            log.debug("Task authentication succeeded, userId={}, username={}", authUser.getId(), authUser.getUsername());
            return true;
        } catch (BusinessException exception) {
            // token 无效或过期时记录统一原因，不记录 token 内容。
            log.warn("Task authentication failed, reason={}, method={}, path={}",
                    exception.getMessage(), request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, exception.getMessage());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        CurrentUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(401, message)));
    }
}
