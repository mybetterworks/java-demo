package com.example.javademo.app.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * 用户表轻量迁移器。
 *
 * <p>项目暂未引入 Flyway 或 Liquibase。v0.2 需要给 v0.1 已存在的 sys_user 表补充 role、
 * deleted、last_login_at 三个字段；如果只修改 CREATE TABLE IF NOT EXISTS，已有 MySQL 表不会自动新增列。
 * 因此这里在应用启动时检查字段是否存在，缺失时执行最小 ALTER TABLE，保证本地旧数据也能继续运行。</p>
 */
@Component
public class UserSchemaMigration implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public UserSchemaMigration(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Spring Boot 完成 SQL 初始化和 Bean 创建后执行字段补齐。
     *
     * <p>每个字段都会先通过 DatabaseMetaData 检查，避免重复执行 ALTER TABLE。
     * SQL 类型选择 MySQL 和 H2 MySQL 模式都能识别的写法，便于本地运行和测试共用。</p>
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        addColumnIfMissing("role", "ALTER TABLE sys_user ADD COLUMN role VARCHAR(32) NOT NULL DEFAULT 'USER'");
        addColumnIfMissing("deleted", "ALTER TABLE sys_user ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0");
        addColumnIfMissing("last_login_at", "ALTER TABLE sys_user ADD COLUMN last_login_at DATETIME NULL");
    }

    /**
     * 字段不存在时执行指定 DDL。
     *
     * @param columnName 数据库字段名
     * @param ddl        需要执行的 ALTER TABLE 语句
     */
    private void addColumnIfMissing(String columnName, String ddl) throws Exception {
        if (!columnExists(columnName)) {
            jdbcTemplate.execute(ddl);
        }
    }

    /**
     * 通过 JDBC 元数据判断字段是否存在。
     *
     * <p>MySQL 和 H2 对大小写处理略有差异，所以同时尝试原始字段名和大写字段名。</p>
     */
    private boolean columnExists(String columnName) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String catalog = connection.getCatalog();
            return columnExists(connection, catalog, "sys_user", columnName)
                    || columnExists(connection, catalog, "sys_user", columnName.toUpperCase())
                    || columnExists(connection, catalog, "SYS_USER", columnName)
                    || columnExists(connection, catalog, "SYS_USER", columnName.toUpperCase());
        }
    }

    private boolean columnExists(Connection connection, String catalog, String tableName, String columnName) throws Exception {
        try (ResultSet columns = connection.getMetaData().getColumns(catalog, null, tableName, columnName)) {
            return columns.next();
        }
    }
}
