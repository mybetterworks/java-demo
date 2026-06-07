package com.example.javademo.notification.websocket;

import com.example.javademo.notification.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Locale;

/**
 * 通知 WebSocket 处理器。
 *
 * <p>鉴权在握手拦截器中完成，真正建立连接后这里只负责登记 session、发送连接确认、处理前端心跳和清理连接。
 * 当前项目没有引入 STOMP，因此客户端发送的文本只支持轻量 PING；业务推送统一由 NotificationWebSocketPushService 主动写出。</p>
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    public static final String AUTH_USER_ATTRIBUTE = "authUser";

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    private final NotificationWebSocketPushService pushService;

    public NotificationWebSocketHandler(NotificationWebSocketPushService pushService) {
        this.pushService = pushService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        AuthUser authUser = authUser(session);
        if (authUser == null) {
            log.warn("WebSocket connection rejected after handshake, sessionId={}, reason=missing_auth_user", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Missing authentication"));
            return;
        }

        int onlineCount = pushService.register(authUser, session);
        NotificationWebSocketMessage ack = NotificationWebSocketMessage.connectionAck(authUser.getId(), onlineCount);
        pushService.sendToUser(authUser.getId(), ack);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        AuthUser authUser = authUser(session);
        if (authUser == null) {
            return;
        }
        String payload = message.getPayload() == null ? "" : message.getPayload().trim();
        if ("PING".equals(payload.toUpperCase(Locale.ROOT))) {
            NotificationWebSocketMessage pong = NotificationWebSocketMessage.pong(
                    authUser.getId(),
                    pushService.onlineSessionCount(authUser.getId())
            );
            pushService.sendToUser(authUser.getId(), pong);
            log.debug("WebSocket ping handled, sessionId={}, userId={}", session.getId(), authUser.getId());
            return;
        }
        log.debug("WebSocket client message ignored, sessionId={}, userId={}, payloadLength={}",
                session.getId(), authUser.getId(), payload.length());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WebSocket transport error, sessionId={}, reason={}",
                session == null ? "unknown" : session.getId(), exception.getClass().getSimpleName());
        pushService.unregister(session, "transport_error");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        pushService.unregister(session, "close_" + status.getCode());
    }

    private AuthUser authUser(WebSocketSession session) {
        Object value = session.getAttributes().get(AUTH_USER_ATTRIBUTE);
        return value instanceof AuthUser authUser ? authUser : null;
    }
}
