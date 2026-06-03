package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求 DTO。
 *
 * <p>低风险登录只需要用户名和密码。v0.5.4 起，当同一登录主体 5 分钟内失败达到阈值后，
 * 需要额外提交滑块验证码校验通过后得到的一次性 captchaToken。为了避免枚举用户，
 * 服务层仍会对用户名不存在和密码错误返回相同错误信息。</p>
 */
@Schema(description = "用户登录请求")
public class LoginRequest {

    /** 登录用户名，服务层会按注册同样规则进行 trim 和小写化。 */
    @Schema(description = "登录用户名", example = "alice")
    @NotBlank
    @Size(max = 64)
    private String username;

    /** 用户提交的明文密码，仅用于与数据库中的 BCrypt 哈希做匹配校验。 */
    @Schema(description = "登录密码", example = "secret123")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    /**
     * 滑块验证码校验通过后返回的一次性 token。
     *
     * <p>该字段只在后端判定当前 username + clientIp 已进入风险状态时必填。
     * token 只用于本次登录风险校验，不能替代 JWT，也不会写入日志。</p>
     */
    @Schema(description = "滑块验证码一次性 token，普通登录可不传")
    @Size(max = 128)
    private String captchaToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptchaToken() {
        return captchaToken;
    }

    public void setCaptchaToken(String captchaToken) {
        this.captchaToken = captchaToken;
    }
}
