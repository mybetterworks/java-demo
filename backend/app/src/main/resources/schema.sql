-- v0.1 最小用户表。
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

    -- 记录创建时间，默认使用数据库当前时间。
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 记录更新时间，MySQL 会在行更新时自动刷新。
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 用户名唯一约束是并发注册时的最终防线。
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);
