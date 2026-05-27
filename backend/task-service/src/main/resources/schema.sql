-- task-service 建表脚本。
-- v0.5.1 使用最小任务模型；后续审计、搜索、附件、消息和分布式事务能力都可以围绕这张表扩展。
CREATE TABLE IF NOT EXISTS task_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(2000) NULL,
    creator_user_id BIGINT NOT NULL,
    assignee_user_id BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
    due_time DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_task_creator (creator_user_id, deleted, updated_at),
    INDEX idx_task_assignee (assignee_user_id, deleted, updated_at),
    INDEX idx_task_status (status, deleted, updated_at)
);
