package com.example.javademo.notification.websocket;

/**
 * WebSocket 推送结果摘要。
 *
 * <p>业务层不需要知道每个 session 的细节，只需要知道目标用户当前有多少连接、成功写出了多少连接。
 * 这两个数字会写入日志，也会作为系统广播接口的响应，方便本地验收 WebSocket 是否真的有在线客户端。</p>
 */
public record NotificationWebSocketPushResult(int onlineSessionCount, int deliveredSessionCount) {
}
