# Project Progress

最后更新：2026-05-22，Asia/Shanghai。

## 当前状态

`v0.2 User CRUD` 已实现并完成自动化测试、打包、真实 MySQL 接口验收、Swagger UI 接口文档更新和新增代码中文注释补全。

已完成：

| 项目 | 状态 |
|---|---|
| 规划目录 | 已创建 |
| 总路线 | 已创建 |
| 开发规则 | 已创建 |
| milestone 文档 | 已创建 |
| 技术决策初稿 | 已创建 |
| 服务拆分策略 | 已采纳 `docs/decisions/0002-service-split.md` |
| 部署策略 | 已采纳 `docs/decisions/0003-deploy-strategy.md` |
| Maven 本地仓库决策 | 已创建 `docs/decisions/0004-maven-local-repository.md` |
| Git 手动提交策略 | 已采纳 `docs/decisions/0005-manual-git-commit.md` |
| 前端组件库策略 | 已采纳 `docs/decisions/0007-frontend-component-libraries.md` |
| 本地 JDK 目标版本 | 用户已升级为 JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| 本地 Maven 目标版本 | 用户已配置 Maven `3.9.16`，路径 `D:\software\apache-maven-3.9.16` |
| Maven 下载目录 | 已指定为 `D:\software\maven_download` |
| 本地 Node.js 目标版本 | 用户已在 Windows 11 配置 Node.js `22.x`，作为后续前端默认运行环境 |
| Git 初始化 | 已完成，用户已提交 GitHub |
| Maven Wrapper | 已完成，固定 Maven `3.9.16` |
| 后端项目骨架 | 已完成 `backend/app` 单体应用 |
| MySQL Docker 基础设施 | 已完成 `infra/docker-compose/mysql/docker-compose.yml` |
| v0.1 登录闭环 | 已完成注册、登录、JWT、当前用户接口 |
| v0.1 接口可视化 | 已集成 Springdoc OpenAPI `2.6.0` 和 Swagger UI |
| 代码注释规范 | 已要求生成和修改的代码必须补充详细中文注释 |
| v0.1 中文注释补全 | 已补充 Java、YAML、SQL、Docker Compose 中的中文说明 |
| v0.2 用户管理 CRUD | 已完成分页、详情、创建、更新、逻辑删除、修改密码 |
| v0.2 数据模型扩展 | 已新增 `role`、`deleted`、`last_login_at` 字段，并提供启动时轻量迁移 |
| v0.2 自动化测试 | 已新增 `UserManagementIntegrationTest` 覆盖用户管理验收链路 |

尚未完成：

| 项目 | 状态 |
|---|---|
| Git milestone commit、tag 和 push | 必须由用户手动执行，Codex 不自动提交、不自动打 tag、不自动推送 |
| v0.3 React 管理端 | 未开始 |
| Vue 前端项目骨架 | 未开始，计划在 `v0.4` 引入 |
| 网关和微服务基础设施 | 未开始，按后续 milestone 逐步引入 |

## 环境观察

| 项目 | 当前观察 |
|---|---|
| Java | 目标版本 JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | 目标版本 Maven `3.9.16`，路径 `D:\software\apache-maven-3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |
| Node.js | 目标版本 Node.js `22.x`，用于 `v0.3` React 和 `v0.4` Vue 前端开发 |
| Docker | Docker Desktop 可用，MySQL `8.4` 单节点容器启动验证通过 |
| Git | 已初始化 Git 仓库，用户已提交 GitHub；后续提交、tag 和推送由用户手动执行 |

当前会话验证说明：

| 检查项 | 结果 |
|---|---|
| 直接执行 `D:\software\jdk-17.0.19\bin\java.exe -version` | 已确认 JDK `17.0.19` 可用 |
| 直接执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -v` | 已确认 Maven `3.9.16` 可用 |
| 临时设置 `JAVA_HOME=D:\software\jdk-17.0.19` 后执行 Maven | 已确认 Maven 可使用 Java `17.0.19` |
| 执行 `.\mvnw.cmd test` | 已通过，`AuthFlowIntegrationTest` 和 `UserManagementIntegrationTest` 覆盖 v0.1/v0.2 核心链路 |
| 执行 `.\mvnw.cmd package` | 已通过，生成可执行 jar |
| 访问 `GET /v3/api-docs` | 已确认返回 OpenAPI JSON |
| 访问 `GET /swagger-ui.html` | 已确认返回 Swagger UI 页面 |
| Node.js 22 | 用户已配置为 Windows 11 本地前端环境；后续进入 `v0.3` 前在新终端执行 `node -v` 复核 |

注意：当前 Codex 进程的 PATH/JAVA_HOME/Node.js 路径可能仍是旧会话环境。若 `java -version`、`mvn -v` 或 `node -v` 未显示上述版本，重启终端或 Codex 会话后再验证。

## v0.1 验证记录

自动化验证：

| 命令 | 结果 |
|---|---|
| `docker compose -f infra\docker-compose\mysql\docker-compose.yml up -d` | MySQL 容器 `java-demo-mysql` 启动并进入 `healthy` |
| `.\mvnw.cmd test` | 通过，`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` |
| `.\mvnw.cmd package` | 通过，生成 `backend/app/target/java-demo-app-0.1.0-SNAPSHOT.jar` |

真实 MySQL 手动接口验收：

| 验收项 | 结果 |
|---|---|
| `GET /api/health` | `200` |
| `POST /api/auth/register` | `200` |
| 重复 `POST /api/auth/register` | `409` |
| `POST /api/auth/login` | `200`，成功签发 JWT |
| 携带 JWT 访问 `GET /api/users/me` | `200` |
| 不携带 JWT 访问 `GET /api/users/me` | `401` |
| 错误密码登录 | `401` |
| `GET /v3/api-docs` | `200`，包含注册、登录、当前用户接口和 `bearerAuth` |
| `GET /swagger-ui.html` | `200`，页面可访问 |

验证说明：

| 项目 | 说明 |
|---|---|
| 验证用户 | 使用临时用户 `codex_v01_20260520131949` 完成链路验证 |
| 应用进程 | 验证后已停止本次手动启动的 Java 进程 |
| Swagger 验证端口 | 因 `8080` 被 IntelliJ 调试进程占用，本次使用临时端口 `18080` 验证后已停止 |
| MySQL 容器 | 验证通过，可用 `docker compose -f infra\docker-compose\mysql\docker-compose.yml stop` 手动停止 |

## v0.2 验证记录

自动化验证：

| 命令 | 结果 |
|---|---|
| `.\mvnw.cmd -pl backend/app -Dtest=UserManagementIntegrationTest test` | 通过，覆盖 v0.2 用户管理链路 |
| `.\mvnw.cmd test` | 通过，`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` |
| `.\mvnw.cmd package` | 通过，生成 `backend/app/target/java-demo-app-0.2.0-SNAPSHOT.jar` |

真实 MySQL 手动接口验收：

| 验收项 | 结果 |
|---|---|
| `GET /api/health` | `200` |
| `POST /api/auth/register` | `200` |
| `POST /api/auth/login` | `200`，成功签发 JWT |
| 未登录访问 `GET /api/users` | `401` |
| 登录后 `POST /api/users` | `200`，成功创建用户 |
| 登录后 `GET /api/users` | `200`，分页查询可用 |
| 登录后 `GET /api/users/{id}` | `200`，详情可用 |
| 登录后 `PUT /api/users/{id}` | `200`，昵称、状态、角色更新可用 |
| 登录后 `PUT /api/users/{id}/password` | `200`，修改密码可用 |
| 修改密码后旧密码登录 | `401` |
| 修改密码后新密码登录 | `200` |
| 登录后 `DELETE /api/users/{id}` | `200`，逻辑删除成功 |
| 删除后 `GET /api/users/{id}` | `404` |
| `GET /v3/api-docs` | `200`，包含 v0.2 用户管理接口 |

验证说明：

| 项目 | 说明 |
|---|---|
| 验证端口 | 本次使用临时端口 `18080` 启动应用验证，验证后已停止 |
| Docker 权限 | 当前沙箱读取 Docker 状态需要授权，已获准执行 `docker ps` 并确认 `java-demo-mysql` 为 `healthy` |
| 数据库迁移 | 应用启动时会自动补齐 v0.2 新增用户字段，避免已有 v0.1 本地表缺列 |

## 当前 milestone

当前已完成：

```text
docs/milestones/v0.2-user-crud.md
```

建议下一步执行：

```text
docs/milestones/v0.3-react-frontend.md
```

## 已采纳执行路线

服务拆分路线：

| 阶段 | 服务形态 |
|---|---|
| `v0.1` - `v0.4` | 单体或简单多模块，先完成业务闭环 |
| `v0.5` | 引入 Gateway，统一外部入口 |
| `v0.6` | 接入 Nacos，进入服务注册和配置中心阶段 |
| `v1.5` 前后 | 根据 Seata 和链路追踪需要拆分更多服务 |

部署路线：

| 阶段 | 部署形态 |
|---|---|
| `v0.1` - `v0.4` | 本地进程 + Docker 基础设施 |
| `v0.5` - `v1.7` | Docker Compose 管理基础设施和部分应用 |
| `v1.8` | Docker Compose 与 Kubernetes 双部署 |
| `v1.9` | Jenkins CI/CD |

## 下一步建议

1. 如需保存当前稳定点，请用户手动提交 Git commit、手动打 tag 并手动推送 GitHub；Codex 不自动执行这些 Git 写操作。
2. 开始 `docs/milestones/v0.3-react-frontend.md`。
3. 在 v0.3 中使用 Node.js `22.x`、React 和 Ant Design 增加 React 管理端，调用当前后端登录和用户管理 API。
4. 保持当前部署路线：前端本地进程，MySQL 继续使用 Docker Desktop。

## 后续对 Codex 的推荐指令

```text
请读取 docs/ROADMAP.md、docs/DEVELOPMENT_RULES.md、docs/PROGRESS.md 和 docs/milestones/v0.1-mvp-login.md，然后实现第一个可运行版本。完成后运行验证，并更新 docs/PROGRESS.md。
```

## 完成记录

| 版本 | 状态 | 完成时间 | 备注 |
|---|---|---|---|
| `v0.1` | 已完成 | 2026-05-20 | 最小登录系统，已补充 Swagger UI 和中文注释；提交、tag 和推送由用户手动执行 |
| `v0.2` | 已完成 | 2026-05-22 | 用户管理 CRUD、分页、逻辑删除、改密、字段迁移；提交、tag 和推送由用户手动执行 |
| `v0.3` | 未开始 | - | React 管理端，默认使用 Ant Design |
| `v0.4` | 未开始 | - | Vue 管理端，默认使用 Element UI / Element Plus |
| `v0.5` | 未开始 | - | Spring Cloud Gateway |
| `v0.6` | 未开始 | - | Nacos |
| `v0.7` | 未开始 | - | Redis |
| `v0.8` | 未开始 | - | WebSocket |
| `v0.9` | 未开始 | - | MinIO |
| `v1.0` | 未开始 | - | RabbitMQ 与 Kafka |
| `v1.1` | 未开始 | - | Elasticsearch |
| `v1.2` | 未开始 | - | InfluxDB |
| `v1.3` | 未开始 | - | Neo4j |
| `v1.4` | 未开始 | - | MySQL 主从 |
| `v1.5` | 未开始 | - | Seata |
| `v1.6` | 未开始 | - | Nginx 与 HTTPS |
| `v1.7` | 未开始 | - | 可观测性 |
| `v1.8` | 未开始 | - | Docker 与 K8s |
| `v1.9` | 未开始 | - | Jenkins CI/CD |

## 已知风险

| 风险 | 影响 | 建议 |
|---|---|---|
| 当前 Codex 会话可能未刷新环境变量 | `java` 或 `mvn` 命令可能仍显示旧版本 | 重启终端或 Codex 会话后再验证 |
| Maven 直接运行时可能读取旧 `JAVA_HOME` | Maven 可能使用 Java 8 而不是 Java 17 | 确保 `JAVA_HOME=D:\software\jdk-17.0.19` |
| 端口 `3306` 可能与本机 MySQL 冲突 | MySQL Docker 容器无法启动 | 暂停本机 MySQL 或调整 compose 端口后记录变更 |
| 端口 `8080` 可能被其他应用占用 | 后端应用无法启动 | 启动前检查端口，必要时临时调整 `server.port` |
| PowerShell 旧版本不支持 `-SkipHttpErrorCheck` | 直接验证 4xx 状态时命令参数不可用 | 使用 `try/catch` 捕获 HTTP 4xx 状态 |
| 当前 Codex 会话可能未刷新 Node.js PATH | `node -v` 可能无法显示 `v22.x.x`，影响前端开发验证 | 进入 v0.3 前重启终端或 Codex 会话后再验证 Node.js 22 |
| 中间件范围很大 | 容易一次性复杂化 | 严格按 milestone 单步推进 |
