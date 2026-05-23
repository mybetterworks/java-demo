-- v0.2 用户管理表结构。
-- 该脚本由 Spring Boot SQL 初始化机制在应用启动时执行。
-- 使用 IF NOT EXISTS 保证重复启动应用不会因为表已存在而失败。
CREATE TABLE IF NOT EXISTS sys_user (
    -- 用户主键，MySQL 自增；MyBatis Plus 会在插入后回填到 User.id。
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- 登录用户名，业务层会统一 trim 并转小写。
    username VARCHAR(64) NOT NULL,

    -- BCrypt 哈希后的密码，不保存明文密码。
    password_hash VARCHAR(128) NOT NULL,

    -- 用户昵称，注册时为空则默认使用 username。
    nickname VARCHAR(64) NOT NULL,

    -- 用户状态，当前约定 1 表示启用。
    status TINYINT NOT NULL DEFAULT 1,

    -- 简化角色字段，v0.2 暂不做复杂 RBAC，只用字符串帮助理解“用户角色”概念。
    role VARCHAR(32) NOT NULL DEFAULT 'USER',

    -- 逻辑删除标记，0 表示未删除，1 表示已删除；删除接口只改该字段，不物理删除数据。
    deleted TINYINT NOT NULL DEFAULT 0,

    -- 最近一次成功登录时间，用于验证登录行为能反写用户资料。
    last_login_at DATETIME NULL,

    -- 记录创建时间，默认使用数据库当前时间。
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 记录更新时间，MySQL 会在行更新时自动刷新。
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 用户名唯一约束是并发注册时的最终防线。
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);
