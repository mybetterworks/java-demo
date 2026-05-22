package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户注册请求 DTO。
 *
 * <p>DTO 用于隔离外部 API 入参与内部实体。注册时前端只需要提交用户名、密码和可选昵称，
 * 服务端会负责用户名规范化、密码哈希和默认状态填充。</p>
 */
@Schema(description = "用户注册请求")
public class RegisterRequest {

    /** 登录用户名，要求 3 到 64 个字符；业务层会 trim 并转小写。 */
    @Schema(description = "登录用户名，注册后会统一转为小写", example = "alice")
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    /** 明文密码只在请求中短暂出现，进入服务层后立即转成 BCrypt 哈希。 */
    @Schema(description = "登录密码，服务端只保存 BCrypt 哈希", example = "secret123")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    /** 用户昵称可选；为空时服务端默认使用用户名作为昵称。 */
    @Schema(description = "用户昵称", example = "Alice")
    @Size(max = 64)
    private String nickname;

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
