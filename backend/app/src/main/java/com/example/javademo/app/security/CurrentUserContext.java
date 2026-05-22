package com.example.javademo.app.security;

import com.example.javademo.app.common.BusinessException;

/**
 * 当前请求用户上下文。
 *
 * <p>AuthInterceptor 在请求进入 Controller 前把解析出的 AuthUser 放入 ThreadLocal，
 * 后续同一请求线程中的 Controller/Service 就可以读取当前用户。请求结束后必须调用 clear 清理，
 * 避免线程复用时发生用户串号。</p>
 */
public final class CurrentUserContext {

    /** 每个请求线程独立保存当前用户信息。 */
    private static final ThreadLocal<AuthUser> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    /** 写入当前请求用户，通常只由 AuthInterceptor 调用。 */
    public static void set(AuthUser user) {
        CURRENT_USER.set(user);
    }

    /**
     * 获取当前请求用户。
     *
     * @return 当前认证用户
     * @throws BusinessException 当前请求未通过认证时抛出 401
     */
    public static AuthUser getRequired() {
        AuthUser user = CURRENT_USER.get();
        if (user == null) {
            throw BusinessException.unauthorized("Current user is missing");
        }
        return user;
    }

    /** 清理当前线程中的用户信息，防止线程池复用导致上下文泄漏。 */
    public static void clear() {
        CURRENT_USER.remove();
    }
}
