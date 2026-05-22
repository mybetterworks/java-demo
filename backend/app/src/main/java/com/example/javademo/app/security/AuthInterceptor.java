package com.example.javademo.app.security;

import com.example.javademo.app.common.ApiResponse;
import com.example.javademo.app.common.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证拦截器。
 *
 * <p>该拦截器运行在 Controller 方法之前，负责从 Authorization 请求头中读取 Bearer token，
 * 调用 JwtService 校验后，把当前用户写入 CurrentUserContext。这样 Controller 和 Service
 * 就可以用统一方式获取当前用户，而不用每个接口重复解析 token。</p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    /** HTTP Authorization 头中 Bearer token 的标准前缀。 */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtService jwtService, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
    }

    /**
     * Controller 执行前的认证入口。
     *
     * <p>OPTIONS 请求通常是浏览器跨域预检请求，直接放行；业务请求必须携带 Bearer token。
     * token 校验失败时直接写出统一 JSON 响应并返回 false，阻止请求继续进入 Controller。</p>
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            writeUnauthorized(response, "Missing bearer token");
            return false;
        }

        try {
            String token = authorization.substring(BEARER_PREFIX.length());
            CurrentUserContext.set(jwtService.parseToken(token));
            return true;
        } catch (BusinessException exception) {
            writeUnauthorized(response, exception.getMessage());
            return false;
        }
    }

    /**
     * 请求完成后清理 ThreadLocal。
     *
     * <p>Servlet 容器会复用线程，如果不清理 ThreadLocal，后一个请求可能误读到前一个请求的用户信息。</p>
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        CurrentUserContext.clear();
    }

    /**
     * 写出 401 统一响应。
     *
     * <p>拦截器阶段还没有进入 Controller，因此无法依赖全局异常处理器，只能在这里直接写响应体。</p>
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(401, message)));
    }
}
