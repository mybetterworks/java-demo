package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理端修改用户密码请求。
 *
 * <p>改密独立成接口，是为了避免“更新用户资料”误把密码当普通字段处理。
 * 这样可以集中保证密码长度校验、哈希存储和后续密码审计策略。</p>
 */
@Schema(description = "修改用户密码请求")
public class ChangePasswordRequest {

    /** 新密码，服务端会重新生成 BCrypt 哈希。 */
    @Schema(description = "新密码，服务端只保存 BCrypt 哈希", example = "newSecret123")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
