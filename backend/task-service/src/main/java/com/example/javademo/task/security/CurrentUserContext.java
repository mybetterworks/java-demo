package com.example.javademo.task.security;

import com.example.javademo.task.common.BusinessException;

/**
 * 任务服务当前用户上下文。
 *
 * <p>AuthInterceptor 在请求进入 Controller 前写入当前用户，请求结束后清理。这里继续沿用
 * java-demo-app 的 ThreadLocal 模型，便于对比单体和微服务拆分后的认证上下文处理方式。</p>
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
