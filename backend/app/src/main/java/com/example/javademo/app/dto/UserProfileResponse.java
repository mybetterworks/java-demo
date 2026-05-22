package com.example.javademo.app.dto;

import com.example.javademo.app.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 用户基础信息响应 DTO。
 *
 * <p>该对象用于返回给前端的用户资料，刻意不包含 passwordHash，避免敏感字段通过 API 泄露。</p>
 */
@Schema(description = "用户基础信息")
public class UserProfileResponse {

    /** 用户主键。 */
    @Schema(description = "用户 ID", example = "1")
    private Long id;

    /** 登录用户名。 */
    @Schema(description = "用户名", example = "alice")
    private String username;

    /** 用户展示昵称。 */
    @Schema(description = "昵称", example = "Alice")
    private String nickname;

    /** 用户状态，当前 1 表示启用。 */
    @Schema(description = "用户状态，1 表示启用", example = "1")
    private Integer status;

    /** 用户创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /** 用户最近更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(Long id, String username, String nickname, Integer status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 将数据库实体转换为 API 响应对象。
     *
     * <p>转换时只挑选可以暴露给前端的字段，敏感的 passwordHash 不会出现在响应中。</p>
     */
    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
