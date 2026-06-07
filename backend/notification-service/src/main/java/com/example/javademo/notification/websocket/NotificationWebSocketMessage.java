package com.example.javademo.notification.websocket;

import com.example.javademo.notification.dto.NotificationResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 通知 WebSocket 统一消息体。
 *
 * <p>v0.8 先不引入 STOMP、消息队列或复杂订阅协议，而是用一个简单 JSON 结构承载实时通知。
 * 这样 React、Vue、Gateway 和后端日志都能直观看到同一种消息格式，便于学习 WebSocket 的最小闭环。
 * 字段保持宽松：不同 type 只填充自己需要的字段，前端按 type 判断如何刷新列表、未读数或提示用户。</p>
 */
public class NotificationWebSocketMessage {

    /** 每条推送消息的唯一 ID，前端用它判断是否收到新事件，日志也可以用它串联推送链路。 */
    private String eventId;

    /** 消息类型，例如 CONNECTION_ACK、NOTIFICATION_CREATED、UNREAD_COUNT_CHANGED、SYSTEM_BROADCAST。 */
    private String type;

    /** 接收用户 ID；广播消息为空，避免把广播误解为只发给单个用户。 */
    private Long receiverUserId;

    /** 面向用户展示的简短标题，不能携带 token、密码等敏感信息。 */
    private String title;

    /** 面向用户展示的简短内容，当前只保存业务提示，不写入认证凭证。 */
    private String content;

    /** 通知详情，仅通知创建或通知状态变化类消息使用。 */
    private NotificationResponse notification;

    /** 当前未读数；创建通知、标记已读和全部已读后都会推送。 */
    private Long unreadCount;

    /** 当前目标用户在线 WebSocket session 数，用于展示简单在线状态。 */
    private Integer onlineSessionCount;

    /** ISO-8601 时间字符串，避免前端再猜测服务端时区。 */
    private String createdAt;

    public NotificationWebSocketMessage() {
    }

    public static NotificationWebSocketMessage connectionAck(Long userId, int onlineSessionCount) {
        NotificationWebSocketMessage message = base("CONNECTION_ACK", userId);
        message.setTitle("WebSocket connected");
        message.setContent("通知实时推送连接已建立");
        message.setOnlineSessionCount(onlineSessionCount);
        return message;
    }

    public static NotificationWebSocketMessage notificationCreated(NotificationResponse notification, long unreadCount, int onlineSessionCount) {
        NotificationWebSocketMessage message = base("NOTIFICATION_CREATED", notification.getReceiverUserId());
        message.setTitle(notification.getTitle());
        message.setContent(notification.getContent());
        message.setNotification(notification);
        message.setUnreadCount(unreadCount);
        message.setOnlineSessionCount(onlineSessionCount);
        return message;
    }

    public static NotificationWebSocketMessage unreadCountChanged(Long userId, long unreadCount, String reason, int onlineSessionCount) {
        NotificationWebSocketMessage message = base("UNREAD_COUNT_CHANGED", userId);
        message.setTitle("未读通知数量已更新");
        message.setContent(reason);
        message.setUnreadCount(unreadCount);
        message.setOnlineSessionCount(onlineSessionCount);
        return message;
    }

    public static NotificationWebSocketMessage systemBroadcast(String title, String content, int onlineSessionCount) {
        NotificationWebSocketMessage message = base("SYSTEM_BROADCAST", null);
        message.setTitle(title);
        message.setContent(content);
        message.setOnlineSessionCount(onlineSessionCount);
        return message;
    }

    public static NotificationWebSocketMessage pong(Long userId, int onlineSessionCount) {
        NotificationWebSocketMessage message = base("PONG", userId);
        message.setTitle("pong");
        message.setContent("服务端已收到前端心跳");
        message.setOnlineSessionCount(onlineSessionCount);
        return message;
    }

    private static NotificationWebSocketMessage base(String type, Long receiverUserId) {
        NotificationWebSocketMessage message = new NotificationWebSocketMessage();
        message.setEventId(UUID.randomUUID().toString());
        message.setType(type);
        message.setReceiverUserId(receiverUserId);
        message.setCreatedAt(OffsetDateTime.now().toString());
        return message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationResponse getNotification() {
        return notification;
    }

    public void setNotification(NotificationResponse notification) {
        this.notification = notification;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Integer getOnlineSessionCount() {
        return onlineSessionCount;
    }

    public void setOnlineSessionCount(Integer onlineSessionCount) {
        this.onlineSessionCount = onlineSessionCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
