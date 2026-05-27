package com.example.javademo.task.security;

/**
 * 任务服务当前认证用户。
 *
 * <p>除了用户 ID 和用户名，还保留原始 accessToken。任务服务调用用户服务和通知服务时会转发该 token，
 * 让下游服务仍按当前登录用户语义完成鉴权。</p>
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
