-- notification-service 建表脚本。
-- v0.5.1 只建立通知 MVP 需要的最小字段；后续 WebSocket、MQ、搜索和审计能力会在此基础上继续扩展。
CREATE TABLE IF NOT EXISTS notification_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    receiver_user_id BIGINT NOT NULL,
    title VARCHAR(120) NOT NULL,
    content VARCHAR(1000) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'SYSTEM',
    biz_type VARCHAR(64) NOT NULL DEFAULT 'GENERAL',
    biz_id BIGINT NULL,
    read_status TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    read_at DATETIME NULL,
    INDEX idx_notification_receiver_read_created (receiver_user_id, read_status, created_at),
    INDEX idx_notification_biz (biz_type, biz_id)
);
