package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建滑块验证码挑战的请求。
 *
 * <p>验证码 challenge 会和规范化后的 username + clientIp 绑定，后续登录时只能用于同一登录主体。
 * 这里不接收密码，避免前端为了获取验证码而重复发送敏感字段。</p>
 */
@Schema(description = "创建滑块验证码挑战请求")
public class SliderCaptchaChallengeRequest {

    /** 登录用户名，用于绑定验证码所属的登录主体。 */
    @Schema(description = "登录用户名", example = "alice")
    @NotBlank
    @Size(max = 64)
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
