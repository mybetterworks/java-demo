package com.example.javademo.notification.security;

/**
 * 通知服务当前认证用户。
 *
 * <p>除用户 ID 和用户名外，这里还保存本次请求的原始 accessToken。v0.5.1 通知服务暂不需要继续
 * 调用下游服务，但保留该字段可以让任务服务与通知服务使用同一种认证上下文模型。</p>
 */
public class AuthUser {

    private final Long id;
    private final String username;
    private final String accessToken;

    public AuthUser(Long id, String username, String accessToken) {
        this.id = id;
        this.username = username;
        this.accessToken = accessToken;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
