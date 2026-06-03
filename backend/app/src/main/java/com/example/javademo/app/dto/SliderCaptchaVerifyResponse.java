package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 滑块验证码校验成功响应。
 *
 * <p>captchaToken 是短 TTL、一次性使用的登录前置凭证，只能随下一次 /api/auth/login 提交。
 * 它不是 JWT，不能访问任何受保护业务接口。</p>
 */
@Schema(description = "滑块验证码校验成功响应")
public class SliderCaptchaVerifyResponse {

    /** 一次性验证码 token，只能用于下一次登录请求。 */
    @Schema(description = "一次性验证码 token")
    private String captchaToken;

    /** token 过期秒数，当前为 120 秒。 */
    @Schema(description = "验证码 token 过期秒数", example = "120")
    private long expiresInSeconds;

    public SliderCaptchaVerifyResponse() {
    }

    public SliderCaptchaVerifyResponse(String captchaToken, long expiresInSeconds) {
        this.captchaToken = captchaToken;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
