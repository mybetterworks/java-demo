package com.example.javademo.notification.websocket;

import com.example.javademo.notification.security.AuthUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 在线会话与推送服务。
 *
 * <p>v0.8 只做单实例内的在线 session 管理：同一个用户可以有多个浏览器标签页，每个标签页对应一个
 * WebSocketSession。后续如果 notification-service 多实例部署，用户 A 可能连到实例 1，而通知创建请求
 * 落到实例 2，这时就需要 Redis Pub/Sub、MQ 或专门的 WebSocket 网关做跨实例分发。当前版本先把单实例
 * 可运行链路讲清楚。</p>
 */
@Service
public class NotificationWebSocketPushService {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketPushService.class);

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> userIdBySessionId = new ConcurrentHashMap<>();

    public NotificationWebSocketPushService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 注册已通过握手鉴权的 WebSocket session。
     */
    public int register(AuthUser authUser, WebSocketSession session) {
        sessionsByUser.computeIfAbsent(authUser.getId(), ignored -> ConcurrentHashMap.newKeySet()).add(session);
        userIdBySessionId.put(session.getId(), authUser.getId());
        int onlineCount = onlineSessionCount(authUser.getId());
        log.info("WebSocket connected, sessionId={}, userId={}, username={}, onlineSessionCount={}",
                session.getId(), authUser.getId(), authUser.getUsername(), onlineCount);
        return onlineCount;
    }

    /**
     * 注销 session。
     *
     * <p>连接断开、推送失败和传输异常都会进入这里，保证内存中的在线状态不会长期残留。</p>
     */
    public void unregister(WebSocketSession session, String reason) {
        if (session == null) {
            return;
        }
        Long userId = userIdBySessionId.remove(session.getId());
        if (userId == null) {
            log.debug("WebSocket session already unregistered, sessionId={}, reason={}", session.getId(), reason);
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                sessionsByUser.remove(userId, sessions);
            }
        }
        log.info("WebSocket disconnected, sessionId={}, userId={}, reason={}, onlineSessionCount={}",
                session.getId(), userId, reason, onlineSessionCount(userId));
    }

    /**
     * 向指定用户所有在线连接推送消息。
     */
    public NotificationWebSocketPushResult sendToUser(Long userId, NotificationWebSocketMessage message) {
        if (userId == null) {
            return new NotificationWebSocketPushResult(0, 0);
        }
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        int onlineCount = onlineSessionCount(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("WebSocket push skipped, userId={}, eventId={}, type={}, reason=no_online_session",
                    userId, message.getEventId(), message.getType());
            return new NotificationWebSocketPushResult(0, 0);
        }

        String payload = serialize(message);
        int delivered = 0;
        for (WebSocketSession session : sessions) {
            if (sendPayload(session, payload, message)) {
                delivered++;
            }
        }
        log.info("WebSocket push completed, userId={}, eventId={}, type={}, onlineSessionCount={}, deliveredSessionCount={}",
                userId, message.getEventId(), message.getType(), onlineCount, delivered);
        return new NotificationWebSocketPushResult(onlineCount, delivered);
    }

    /**
     * 向所有在线连接广播消息。
     */
    public NotificationWebSocketPushResult broadcast(NotificationWebSocketMessage message) {
        String payload = serialize(message);
        int onlineCount = totalOnlineSessionCount();
        int delivered = 0;
        for (Set<WebSocketSession> sessions : sessionsByUser.values()) {
            for (WebSocketSession session : sessions) {
                if (sendPayload(session, payload, message)) {
                    delivered++;
                }
            }
        }
        log.info("WebSocket broadcast completed, eventId={}, type={}, onlineSessionCount={}, deliveredSessionCount={}",
                message.getEventId(), message.getType(), onlineCount, delivered);
        return new NotificationWebSocketPushResult(onlineCount, delivered);
    }

    public int onlineSessionCount(Long userId) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null) {
            return 0;
        }
        sessions.removeIf(session -> !session.isOpen());
        if (sessions.isEmpty()) {
            sessionsByUser.remove(userId, sessions);
        }
        return sessions.size();
    }

    public int totalOnlineSessionCount() {
        return sessionsByUser.keySet().stream()
                .mapToInt(this::onlineSessionCount)
                .sum();
    }

    private String serialize(NotificationWebSocketMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("WebSocket message serialization failed", exception);
        }
    }

    private boolean sendPayload(WebSocketSession session, String payload, NotificationWebSocketMessage message) {
        if (session == null || !session.isOpen()) {
            unregister(session, "closed_before_send");
            return false;
        }
        try {
            /*
             * WebSocketSession 的并发 send 不是所有容器都保证线程安全。
             * 学习版用 session 对象做细粒度同步，避免同一连接同时写出多条消息导致帧交错。
             */
            synchronized (session) {
                if (!session.isOpen()) {
                    return false;
                }
                session.sendMessage(new TextMessage(payload));
            }
            return true;
        } catch (IOException exception) {
            Long userId = userIdBySessionId.get(session.getId());
            log.warn("WebSocket push failed, sessionId={}, userId={}, eventId={}, type={}, reason={}",
                    session.getId(), userId, message.getEventId(), message.getType(), exception.getClass().getSimpleName());
            unregister(session, "send_failed");
            return false;
        }
    }
}
