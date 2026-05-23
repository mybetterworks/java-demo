package com.example.javademo.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 更新用户基础信息请求。
 *
 * <p>v0.2 暂不允许修改用户名，避免涉及登录名变更、唯一索引迁移和 token 主体变化。
 * 可更新字段保持在昵称、状态和角色三个最小管理字段上。</p>
 */
@Schema(description = "更新用户基础信息请求")
public class UpdateUserRequest {

    /** 新昵称，允许为空字符串时由服务端拒绝，避免展示名变成不可读状态。 */
    @Schema(description = "用户昵称", example = "Robert")
    @Size(max = 64)
    private String nickname;

    /** 用户状态，1 表示启用，0 表示禁用。 */
    @Schema(description = "用户状态，1 启用，0 禁用", example = "1")
    private Integer status;

    /** 简化角色字段。 */
    @Schema(description = "简化角色字段，当前仅存储不做权限判断", example = "ADMIN")
    @Size(max = 32)
    private String role;

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
