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

-- v0.9 任务附件元数据表。
-- 文件内容不存入 MySQL，只保存 MinIO object_key、原始文件名、大小和类型，避免业务表承载大对象。
CREATE TABLE IF NOT EXISTS task_attachment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    uploader_user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    file_size BIGINT NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_task_attachment_task (task_id, deleted, created_at),
    INDEX idx_task_attachment_uploader (uploader_user_id, deleted, created_at)
);
