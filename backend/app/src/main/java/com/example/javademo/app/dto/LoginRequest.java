package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求 DTO。
 *
 * <p>登录接口只接收用户名和密码。为了避免枚举用户，服务层会对用户名不存在和密码错误返回相同错误信息。</p>
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
}
