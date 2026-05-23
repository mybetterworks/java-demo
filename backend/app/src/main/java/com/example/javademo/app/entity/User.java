package com.example.javademo.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户表实体。
 *
 * <p>该类通过 MyBatis Plus 映射到 sys_user 表。字段命名使用 Java 驼峰风格，
 * MyBatis Plus 配置中的 map-underscore-to-camel-case 会自动完成数据库下划线字段到 Java 字段的转换，
 * 例如 password_hash 映射到 passwordHash。v0.2 在 v0.1 登录字段基础上增加角色、逻辑删除和最近登录时间，
 * 用同一张表支撑最小用户管理功能。</p>
 */
@TableName("sys_user")
public class User {

    /** 用户主键，数据库自增。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，业务上保持唯一，注册时会统一 trim 并转小写。 */
    private String username;

    /** BCrypt 哈希后的密码，不保存明文密码。 */
    private String passwordHash;

    /** 用户昵称，v0.1 没有单独个人资料表，因此直接放在用户表中。 */
    private String nickname;

    /** 用户状态，当前约定 1 表示启用，后续可扩展禁用、锁定等状态。 */
    private Integer status;

    /** 简化角色字段，v0.2 先使用字符串表达角色，不引入复杂 RBAC 表结构。 */
    private String role;

    /**
     * 逻辑删除标记。
     *
     * <p>MyBatis Plus 会根据 @TableLogic 自动在查询中追加 deleted = 0，
     * 调用 deleteById 时也会转换为更新 deleted = 1，避免学习阶段误删数据。</p>
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;

    /** 最近一次登录成功时间，登录接口会在签发 JWT 前刷新该字段。 */
    private LocalDateTime lastLoginAt;

    /** 记录创建时间，由注册逻辑写入。 */
    private LocalDateTime createdAt;

    /** 记录更新时间，v0.1 注册时与创建时间相同，后续更新用户资料时会刷新。 */
    private LocalDateTime updatedAt;

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
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
