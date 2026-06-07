package com.example.javademo.notification.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * notification-service WebSocket 配置。
 *
 * <p>v0.8 把实时通知入口放在 notification-service：/ws/notifications。
 * Gateway 负责把外部同路径 WebSocket 转发到这里；服务自身仍然在握手阶段校验 JWT，保证直连服务时也不能绕过认证。</p>
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final NotificationWebSocketAuthHandshakeInterceptor authHandshakeInterceptor;

    public WebSocketConfig(NotificationWebSocketHandler notificationWebSocketHandler,
                           NotificationWebSocketAuthHandshakeInterceptor authHandshakeInterceptor) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notifications")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*");
    }
}
