-- v0.5.1 多业务服务 database 初始化脚本。
-- MySQL 官方镜像只会在数据目录首次初始化时执行 /docker-entrypoint-initdb.d 下的脚本。
-- 已经存在旧数据卷的本地环境，需要使用 README 中的 docker exec 命令手动补齐这些 database。
CREATE DATABASE IF NOT EXISTS java_demo_task DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS java_demo_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON java_demo_task.* TO 'java_demo'@'%';
GRANT ALL PRIVILEGES ON java_demo_notification.* TO 'java_demo'@'%';

FLUSH PRIVILEGES;
