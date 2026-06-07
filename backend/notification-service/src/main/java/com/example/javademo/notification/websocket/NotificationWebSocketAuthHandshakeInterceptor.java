package com.example.javademo.notification.websocket;

import com.example.javademo.notification.common.BusinessException;
import com.example.javademo.notification.security.AuthUser;
import com.example.javademo.notification.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 握手鉴权拦截器。
 *
 * <p>浏览器原生 WebSocket 不能像 fetch 一样自由设置 Authorization header，因此前端会把 JWT 放在
 * token 查询参数中：/ws/notifications?token=...。这里只解析和验签 token，不把 token 原文写入日志；
 * 鉴权成功后把 AuthUser 放入 attributes，后续 handler 就能知道这条连接属于哪个用户。</p>
 */
@Component
public class NotificationWebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketAuthHandshakeInterceptor.class);

    private final JwtService jwtService;

    public NotificationWebSocketAuthHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("WebSocket handshake rejected, reason=missing_token, path={}", request.getURI().getPath());
            return false;
        }
        try {
            AuthUser authUser = jwtService.parseToken(token);
            attributes.put(NotificationWebSocketHandler.AUTH_USER_ATTRIBUTE, authUser);
            log.info("WebSocket handshake authenticated, userId={}, username={}, path={}",
                    authUser.getId(), authUser.getUsername(), request.getURI().getPath());
            return true;
        } catch (BusinessException exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.warn("WebSocket handshake rejected, reason={}, path={}", exception.getMessage(), request.getURI().getPath());
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.warn("WebSocket handshake failed, path={}, reason={}", request.getURI().getPath(), exception.getClass().getSimpleName());
        }
    }

    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length()).trim();
        }
        return UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");
    }
}
