package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 管理端创建用户请求。
 *
 * <p>与注册接口相比，创建用户允许管理端直接指定状态和角色，用于 v0.2 学习用户管理 CRUD。
 * 当前版本还没有 RBAC 权限模型，因此“管理端”含义是“已登录用户可调用受保护接口”。</p>
 */
@Schema(description = "管理端创建用户请求")
public class CreateUserRequest {

    /** 登录用户名，服务端会统一 trim 并转小写。 */
    @Schema(description = "登录用户名，服务端会统一转小写", example = "bob")
    @NotBlank
    @Size(min = 3, max = 64)
    private String username;

    /** 初始密码，入库前会使用 BCrypt 哈希。 */
    @Schema(description = "初始密码，服务端只保存 BCrypt 哈希", example = "secret123")
    @NotBlank
    @Size(min = 6, max = 64)
    private String password;

    /** 昵称为空时服务端会默认使用用户名。 */
    @Schema(description = "用户昵称", example = "Bob")
    @Size(max = 64)
    private String nickname;

    /** 用户状态，空值默认启用。 */
    @Schema(description = "用户状态，1 启用，0 禁用；为空默认 1", example = "1")
    private Integer status;

    /** 简化角色字段，空值默认 USER。 */
    @Schema(description = "简化角色字段，当前仅存储不做权限判断", example = "USER")
    @Size(max = 32)
    private String role;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
