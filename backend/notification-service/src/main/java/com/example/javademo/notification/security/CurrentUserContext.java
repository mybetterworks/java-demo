package com.example.javademo.notification.security;

import com.example.javademo.notification.common.BusinessException;

/**
 * 通知服务当前用户上下文。
 *
 * <p>AuthInterceptor 在请求进入 Controller 前写入当前用户，请求结束后清理。该设计和
 * java-demo-app 保持一致，便于学习 ThreadLocal 在 Servlet 线程模型中的使用和风险。</p>
 */
public final class CurrentUserContext {

    private static final ThreadLocal<AuthUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(AuthUser user) {
        CURRENT_USER.set(user);
    }

    public static AuthUser getRequired() {
        AuthUser user = CURRENT_USER.get();
        if (user == null) {
            throw BusinessException.unauthorized("Current user is missing");
        }
        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
