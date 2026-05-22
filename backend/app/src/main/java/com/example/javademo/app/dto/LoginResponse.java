package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录成功响应 DTO。
 *
 * <p>前端拿到 accessToken 后，应在后续需要认证的请求中设置：
 * Authorization: Bearer {accessToken}。expiresInSeconds 用于提示 token 过期时间。</p>
 */
@Schema(description = "登录成功响应数据")
public class LoginResponse {

    /** Token 类型，当前固定为 Bearer。 */
    @Schema(description = "Token 类型", example = "Bearer")
    private String tokenType;

    /** JWT 字符串，包含用户 ID、用户名、签发时间和过期时间。 */
    @Schema(description = "JWT 访问令牌")
    private String accessToken;

    /** token 剩余有效期，单位秒。 */
    @Schema(description = "Token 有效期，单位秒", example = "7200")
    private long expiresInSeconds;

    /** 登录成功后顺带返回的用户基础信息，方便前端初始化当前用户状态。 */
    @Schema(description = "当前登录用户")
    private UserProfileResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String tokenType, String accessToken, long expiresInSeconds, UserProfileResponse user) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
        this.user = user;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public UserProfileResponse getUser() {
        return user;
    }

    public void setUser(UserProfileResponse user) {
        this.user = user;
    }
}
