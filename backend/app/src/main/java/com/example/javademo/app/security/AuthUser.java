package com.example.javademo.app.security;

/**
 * 当前认证用户的轻量模型。
 *
 * <p>JWT 中只保存认证所需的最小信息：用户 ID 和用户名。更完整的用户资料通过 UserAccountService
 * 回表查询，避免 token 中携带过多可能过期的数据。</p>
 */
public class AuthUser {

    /** 用户 ID，对应 sys_user.id。 */
    private final Long id;

    /** 用户名，对应 sys_user.username。 */
    private final String username;

    public AuthUser(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
}
