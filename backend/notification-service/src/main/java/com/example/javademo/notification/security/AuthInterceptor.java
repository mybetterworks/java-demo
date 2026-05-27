package com.example.javademo.notification.security;

import com.example.javademo.notification.common.ApiResponse;
import com.example.javademo.notification.common.BusinessException;
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
 * 通知服务 JWT 拦截器。
 *
 * <p>Gateway 已经做了第一道 JWT 校验，但每个业务服务仍保留自己的校验能力，
 * 防止开发调试时绕过 Gateway 导致未认证请求直接进入服务。</p>
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
            // 通知服务可能被任务服务或前端调用，认证失败时只记录路径和原因摘要。
            log.warn("Notification authentication failed, reason=missing_bearer_token, method={}, path={}",
                    request.getMethod(), request.getRequestURI());
            writeUnauthorized(response, "Missing bearer token");
            return false;
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length()).trim();
            AuthUser authUser = jwtService.parseToken(token);
            CurrentUserContext.set(authUser);
            log.debug("Notification authentication succeeded, userId={}, username={}", authUser.getId(), authUser.getUsername());
            return true;
        } catch (BusinessException exception) {
            // 不输出 token 原文，只记录可排查的错误摘要和接口路径。
            log.warn("Notification authentication failed, reason={}, method={}, path={}",
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
