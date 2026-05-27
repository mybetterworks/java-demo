# Project Progress

最后更新：2026-05-27，Asia/Shanghai。

## 当前状态

`v0.5 Gateway JWT` 已实现并完成 Spring Cloud Gateway 模块、网关 JWT 校验过滤器、React/Vue 代理切换、Maven reactor package、前端构建回归、Gateway 真实接口联调和文档更新。

`v0.5.1 Task And Notification Services` 已完成。当前已补齐 `task-service` 和 `notification-service` 的代码、配置、SQL 初始化脚本、Gateway 静态路由和集成测试代码，并已补跑 Maven `test`、Maven `package`、React/Vue 构建回归和真实 Gateway 任务通知链路联调。

`v0.5.2 Backend Runtime Logging` 已完成。当前 `java-demo-app`、`task-service`、`notification-service` 已支持控制台日志、项目文件日志、`requestId`、可配置日志级别、服务间 requestId 透传和敏感信息保护，便于后续前端联调、Nacos、缓存、消息和可观测性实验时观察运行状态。

已新增 `v0.5.3 Task And Notification Frontends` 规划，作为任务和通知两个后端微服务的前端承接版本。该版本要求 React 和 Vue 都补齐任务管理与通知中心，双端功能、布局、操作路径保持一致，但代码结构和开发风格继续保留各自框架特点。

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
| Docker 服务容器化策略 | 已采纳 `docs/decisions/0010-docker-service-containerization.md`，后续基础设施服务和集群节点均使用独立容器 |
| 任务和通知服务边界 | 已采纳 `docs/decisions/0011-task-notification-service-boundary.md`，`v0.5.1` 将新增 `task-service` 和 `notification-service` |
| Maven 本地仓库决策 | 已创建 `docs/decisions/0004-maven-local-repository.md` |
| Git 手动提交策略 | 已采纳 `docs/decisions/0005-manual-git-commit.md` |
| 前端组件库策略 | 已采纳 `docs/decisions/0007-frontend-component-libraries.md` |
| 本地端口规划 | 已采纳 `docs/decisions/0008-local-port-allocation.md` |
| 前端语言策略 | 已采纳 `docs/decisions/0009-frontend-language-strategy.md`，React 使用 TypeScript，Vue 使用 JavaScript |
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
| 代码注释规范 | 已要求生成和修改的代码必须补充详细中文注释；后端代码不仅需要类/方法注释，也需要方法内部关键代码块注释 |
| v0.1 中文注释补全 | 已补充 Java、YAML、SQL、Docker Compose 中的中文说明 |
| v0.2 用户管理 CRUD | 已完成分页、详情、创建、更新、逻辑删除、修改密码 |
| v0.2 数据模型扩展 | 已新增 `role`、`deleted`、`last_login_at` 字段，并提供启动时轻量迁移 |
| v0.2 自动化测试 | 已新增 `UserManagementIntegrationTest` 覆盖用户管理验收链路 |
| v0.3 React 管理端 | 已完成登录页、首页、用户列表、新增用户、编辑用户，技术语言为 TypeScript |
| v0.3 Ant Design | 已接入 Ant Design `5`，用于表单、布局、表格、弹窗和反馈 |
| v0.3 IndexedDB | 已保存 `auth_session` 和 `recent_users_query` |
| v0.3 前端构建 | 已通过 `npm.cmd run build` |
| v0.3 前后端联调 | 已通过 Vite 代理、真实后端和真实 MySQL 验证 |
| v0.4 Vue 管理端 | 已完成登录页、首页、用户列表、新增用户、编辑用户和逻辑删除入口，技术语言为 JavaScript |
| v0.4 Element Plus | 已接入 Element Plus，用于表单、布局、表格、分页、弹窗、提示和确认框 |
| v0.4 Vue 结构优化 | 已调整为 `App.vue`、`layouts`、`views`、`composables`、`api`、`storage` 和 `styles.css`，体现 Vue 常见开发习惯 |
| v0.4 本地状态 | 已使用 localStorage 保存 Vue 端登录会话和最近用户查询条件，用于和 React 端 IndexedDB 做学习对比 |
| v0.4 前端构建 | 已通过 `npm.cmd run build` |
| v0.4 前后端联调 | 已通过 Vite 代理、真实后端和真实 MySQL 验证 |
| v0.4 浏览器验证 | 已通过内置浏览器登录 Vue 管理端，并进入“当前用户”和“用户管理”工作区 |
| 本地后端端口调整 | 已将后端默认端口从占用范围内的 `8082` 调整为 `8091` |
| React 代理端口调整 | v0.5 已将 Vite 代理目标从后端 `8091` 调整为 Gateway `8092` |
| Vue 代理端口配置 | v0.5 已将 Vite 代理目标从后端 `8091` 调整为 Gateway `8092`，Vue 开发端口为 `5174` |
| v0.5 Spring Cloud Gateway | 已新增 `backend/gateway` 模块，默认端口为 `8092` |
| v0.5 Gateway 路由 | 已将登录、用户、OpenAPI JSON 和 Swagger UI 经 Gateway 转发到后端 `8091` |
| v0.5 Gateway JWT 校验 | 已新增 `JwtGatewayFilter`，白名单放行登录、注册、健康检查和接口文档，用户接口必须携带 Bearer token |
| v0.5 前端代理切换 | React 和 Vue 的 Vite proxy 已从后端 `8091` 调整为 Gateway `8092` |
| v0.5 自动化测试 | 已新增网关过滤器测试，覆盖公开路径、无 token、有效 token 和无效 token |
| v0.5 真实联调 | 已通过 Gateway 注册、登录、无 token 拦截、携带 token 查询当前用户和用户分页 |
| v0.5.1 milestone 规划 | 已新增 `docs/milestones/v0.5.1-task-notification-services.md`，明确任务服务、通知服务、接口、端口、数据库和后续技术结合点 |
| v0.5.1 task-service 代码 | 已新增 `backend/task-service`，包含任务 CRUD、我的任务、状态流转、逻辑删除、JWT 校验、Swagger、MyBatis Plus、H2 集成测试和静态 REST 下游调用 |
| v0.5.1 notification-service 代码 | 已新增 `backend/notification-service`，包含通知创建、我的通知、未读数、单条已读、全部已读、JWT 校验、Swagger、MyBatis Plus 和 H2 集成测试 |
| v0.5.1 Gateway 路由 | 已新增 `/api/tasks/**` -> `8093`、`/api/notifications/**` -> `8094`，并放行 `/api/tasks/health` 和 `/api/notifications/health` |
| v0.5.1 MySQL 初始化 | 已新增 `infra/docker-compose/mysql/init/01-create-v051-databases.sql`，用于首次初始化 `java_demo_task` 和 `java_demo_notification` |
| v0.5.1 自动化测试 | 已通过 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，四个 Maven 模块测试均成功 |
| v0.5.1 package | 已通过 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，四个 `0.5.1-SNAPSHOT` jar 均已生成 |
| v0.5.1 真实联调 | 已通过 Gateway `8092` 完成健康检查、JWT 拦截、任务创建、任务查询、状态流转、通知查询、未读数、已读标记和 OpenAPI 验证 |
| v0.5.1 前端构建回归 | React 和 Vue 均已执行 `npm.cmd run build` 并通过，保留既有 Vite chunk size warning |
| v0.5.2 milestone 规划 | 已新增 `docs/milestones/v0.5.2-backend-runtime-logging.md`，明确三个业务服务的控制台日志、文件日志、日志级别配置、关键日志点、敏感信息保护和后端关键代码块注释要求 |
| 后端运行日志策略 | 已采纳 `docs/decisions/0012-backend-runtime-logging.md`，后续后端 milestone 默认需要补充关键运行日志 |
| v0.5.2 运行日志配置 | 三个业务服务已配置 `logging.file.name`、滚动策略、root 和业务包日志级别环境变量、包含 `requestId` 的控制台/文件日志格式 |
| v0.5.2 请求日志 | 三个业务服务已新增请求日志过滤器，生成或复用 `X-Request-Id`，记录请求开始、结束、状态码和耗时，并在请求结束清理 MDC |
| v0.5.2 启动日志 | 三个业务服务已新增启动摘要日志，记录服务名、端口、profile、日志文件、日志级别和脱敏配置摘要 |
| v0.5.2 业务日志 | `java-demo-app` 已补充注册、登录、JWT、用户管理和异常日志；`task-service` 已补充任务、服务间调用和异常日志；`notification-service` 已补充通知、未读数、已读和异常日志 |
| v0.5.2 服务间 requestId | `task-service` 调用用户服务和通知服务时已透传 `X-Request-Id`，便于跨日志文件串联同一次业务链路 |
| v0.5.2 验证 | 已通过 Maven test/package、React/Vue 构建回归、真实 Gateway 联调、DEBUG/WARN 日志级别验证、文件日志验证和敏感信息检查 |
| v0.5.3 milestone 规划 | 已新增 `docs/milestones/v0.5.3-task-notification-frontends.md`，明确 React/Vue 双端任务管理和通知中心的开发范围、双端一致性和注释要求 |
| 前后端功能联动策略 | 已采纳 `docs/decisions/0013-frontend-backend-feature-sync.md`，后续后端用户可见能力变化默认需要评估并同步 React/Vue 前端 |

尚未完成：

| 项目 | 状态 |
|---|---|
| Git milestone commit、tag 和 push | 必须由用户手动执行，Codex 不自动提交、不自动打 tag、不自动推送 |
| v0.5.3 React/Vue 任务通知前端实现 | 未开始，需在 v0.5.2 日志基线完成后实施 |
| Nacos 和后续微服务基础设施 | 未开始，按后续 milestone 逐步引入 |

## 环境观察

| 项目 | 当前观察 |
|---|---|
| Java | 目标版本 JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | 目标版本 Maven `3.9.16`，路径 `D:\software\apache-maven-3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |
| Node.js | 目标版本 Node.js `22.x`，用于 React TypeScript 和 Vue JavaScript 前端开发 |
| Docker | Docker Desktop 可用，MySQL `8.4` 单节点容器启动验证通过；后续 Nacos、Redis、RabbitMQ、Kafka、Elasticsearch、Seata、Jenkins 等服务按“服务/节点独立容器”推进 |
| Git | 已初始化 Git 仓库，用户已提交 GitHub；后续提交、tag 和推送由用户手动执行 |
| 本机占用端口 | `7991-8090`、`8146-8245`；当前项目端口规划已避开 |
| Gateway | Spring Cloud Gateway `2023.0.3` 已接入，默认端口 `8092` |

当前会话验证说明：

| 检查项 | 结果 |
|---|---|
| 直接执行 `D:\software\jdk-17.0.19\bin\java.exe -version` | 已确认 JDK `17.0.19` 可用 |
| 直接执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -v` | 已确认 Maven `3.9.16` 可用 |
| 临时设置 `JAVA_HOME=D:\software\jdk-17.0.19` 后执行 Maven | 已确认 Maven 可使用 Java `17.0.19` |
| 本次执行 `.\mvnw.cmd test` | 当前 PowerShell 会话中 Maven Wrapper 启动失败，报 `Cannot start maven from wrapper`；已改用用户配置的本地 Maven 3.9.16 完成同等验证 |
| 执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test` | 已通过，`AuthFlowIntegrationTest` 和 `UserManagementIntegrationTest` 覆盖 v0.1/v0.2 核心链路 |
| 执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 已通过，生成 `backend/app/target/java-demo-app-0.5.0-SNAPSHOT.jar` 和 `backend/gateway/target/java-demo-gateway-0.5.0-SNAPSHOT.jar` |
| 访问 `GET /v3/api-docs` | 已确认返回 OpenAPI JSON |
| 访问 `GET /swagger-ui.html` | 已确认返回 Swagger UI 页面 |
| Node.js 22 | 已确认当前会话 `node -v` 为 `v22.22.3` |
| npm | PowerShell 执行 `npm.ps1` 受本机策略限制，已使用 `npm.cmd` 完成安装和构建 |
| 端口扫描 | 已确认当前显式配置端口中，只有旧后端 `8082`/历史文档 `8080` 落入占用范围；当前配置已改为 `8091` |
| 后端 `8091` 运行验证 | 已通过，`GET http://127.0.0.1:8091/api/health`、`/v3/api-docs`、`/swagger-ui.html` 均返回 `200` |
| React `5173` 代理验证 | 历史验证已通过；v0.5 当前 Vite 代理目标已调整为 Gateway `8092` |
| Vue `5174` 代理验证 | 已通过，`GET http://127.0.0.1:5174`、`/api/health`、登录、用户分页、新增、编辑和删除均可用 |
| Vue 浏览器登录验证 | 已通过，使用内置浏览器登录 Vue 管理端、进入首页，并切换到用户管理工作区 |
| Vue 结构优化构建验证 | 已通过，调整为 Vue 常见结构后再次执行 `frontend-vue` 的 `npm.cmd run build` 成功 |
| Vue 结构优化浏览器验证 | 已通过，调整为 `layouts`、`views`、`composables` 后仍可登录、查看首页并进入用户管理 |
| v0.5 网关模块测试 | 已通过，`JwtGatewayFilterTest` 4 个用例覆盖公开路径、无 token、有效 token、无效 token |
| v0.5 Maven reactor package | 已通过，`java-demo-app` 和 `java-demo-gateway` 均生成 `0.5.0-SNAPSHOT` jar |
| v0.5 Gateway `8092` 运行验证 | 已通过，`GET http://127.0.0.1:8092/api/health` 和 `/v3/api-docs` 均返回 `200` |
| v0.5 Gateway JWT 验证 | 已通过，注册/登录走 Gateway 成功，不带 token 访问 `/api/users` 返回 `401`，携带 token 可访问 `/api/users/me` 和用户分页 |
| v0.5 React/Vue 构建回归 | 已通过，`frontend-react` 和 `frontend-vue` 均执行 `npm.cmd run build` 成功 |
| v0.5 React/Vue 代理联调 | 已通过，React `5173` 和 Vue `5174` 均经 Vite proxy 访问 Gateway `8092`，并完成注册、登录、当前用户和无 token 拦截验证 |
| v0.5.1 Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；`java-demo-app` 2 个测试、`java-demo-gateway` 6 个测试、`task-service` 1 个测试、`notification-service` 1 个测试均成功 |
| v0.5.1 Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；已生成 `java-demo-app`、`java-demo-gateway`、`java-demo-task-service`、`java-demo-notification-service` 四个 `0.5.1-SNAPSHOT` jar |
| v0.5.1 MySQL 容器状态 | 已执行 `docker ps --filter "name=java-demo-mysql"`，确认 `java-demo-mysql` 为 `healthy` |
| v0.5.1 MySQL database | 已确认 `java_demo`、`java_demo_task`、`java_demo_notification` 均存在，且 `java_demo` 用户具备任务库和通知库权限 |
| v0.5.1 Gateway 健康检查 | 已通过 Gateway `8092` 访问 `/api/health`、`/api/tasks/health`、`/api/notifications/health` |
| v0.5.1 Gateway JWT 拦截 | 不带 token 访问 `GET /api/tasks` 和 `GET /api/notifications/my` 均返回 `401` |
| v0.5.1 Gateway 任务通知链路 | 已通过 Gateway 注册/登录测试用户、创建任务、查询我的任务、更新任务状态、查询通知、查询未读数、单条已读和全部已读 |
| v0.5.1 Gateway OpenAPI | 已通过 Gateway 访问 `/v3/api-docs`，返回 `200` |
| v0.5.1 React 构建回归 | 已执行 `frontend-react` 的 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| v0.5.1 Vue 构建回归 | 已执行 `frontend-vue` 的 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| v0.5.1 当前验证状态 | 已完成自动化测试、package、真实 Gateway 联调、前端构建回归和文档更新，本版本可标记为完成 |
| v0.5.2 Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；四个后端模块测试均成功 |
| v0.5.2 Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；已生成四个 `0.5.2-SNAPSHOT` jar |
| v0.5.2 React 构建回归 | 已执行 `frontend-react` 的 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| v0.5.2 Vue 构建回归 | 已执行 `frontend-vue` 的 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| v0.5.2 真实 Gateway 联调 | 已使用临时端口 `8252-8255` 启动四个 `0.5.2` jar，经 Gateway 完成注册、登录、任务创建、任务查询、状态流转、通知查询、未读数和 OpenAPI 验证 |
| v0.5.2 文件日志验证 | 已验证 `logs/v052-java-demo-app-debug.log`、`logs/v052-task-service-debug.log`、`logs/v052-notification-service-debug.log` 均包含启动摘要、请求日志、`requestId` 和关键业务日志 |
| v0.5.2 DEBUG 级别验证 | 已验证三个业务服务业务包设置为 `DEBUG` 后可看到当前用户、我的任务、未读通知数等调试日志 |
| v0.5.2 WARN 级别验证 | 已验证 `JAVA_DEMO_LOG_LEVEL_ROOT=WARN` 和 `JAVA_DEMO_APP_LOG_LEVEL=WARN` 下普通 INFO 请求日志不输出，认证失败 WARN 日志仍输出 |
| v0.5.2 敏感信息检查 | 已确认本次日志文件中未出现登录密码、完整 JWT 或 `Authorization` 字样 |
| v0.5.2 临时端口 | 验证结束后已停止本次临时启动进程，`8252-8255` 无监听进程；未停止用户 IntelliJ 正在占用的 `8091-8094` 进程 |

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

## v0.3 验证记录

自动化和构建验证：

| 命令 | 结果 |
|---|---|
| `node -v` | 通过，输出 `v22.22.3` |
| `npm.cmd install` | 通过，生成 `frontend-react/package-lock.json` |
| `npm.cmd run build` | 通过，React TypeScript 类型检查和 Vite 生产构建均成功 |
| `npm.cmd audit --audit-level=high` | 通过，无 high/critical 漏洞；存在 Vite/esbuild 相关 moderate 提示 |
| `.\mvnw.cmd test` | 通过，`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` |
| `.\mvnw.cmd package` | 通过，生成 `backend/app/target/java-demo-app-0.3.0-SNAPSHOT.jar` |

真实联调验收：

| 验收项 | 结果 |
|---|---|
| React 应用访问 `http://127.0.0.1:5173` | `200` |
| 通过 Vite 代理访问 `GET /api/health` | `200` |
| 通过 Vite 代理注册测试用户 | `200` |
| 通过 Vite 代理登录测试用户 | `200`，成功签发 JWT |
| 通过 Vite 代理访问用户分页 | `200` |
| 浏览器登录 React 管理端 | 通过 |
| 刷新页面后从 IndexedDB 恢复登录态 | 通过 |
| 浏览器查看用户列表 | 通过 |
| 浏览器新增用户 | 通过 |
| 浏览器编辑用户 | 通过 |

验证说明：

| 项目 | 说明 |
|---|---|
| 后端验证端口 | 历史验证使用 `8080`；当前端口规划已改为 `8091` |
| 前端验证端口 | `5173` |
| 浏览器验证用户 | `react_v03_20260523030103` |
| 浏览器新增用户 | `ui_v03_1779505466368` |
| 截图记录 | `target/run/react-v0.3-users.png`，仅作为本地验证产物，不提交 |
| 构建提示 | Ant Design 主包导致 Vite chunk size warning，不影响当前版本运行 |
| 安全提示 | `npm audit` 仍提示 Vite/esbuild moderate 风险，修复需要强制升级到破坏性版本，暂不执行 `npm audit fix --force` |

## v0.4 验证记录

自动化和构建验证：

| 命令 | 结果 |
|---|---|
| `node -v` | 通过，输出 `v22.22.3` |
| `npm.cmd install` | 通过，生成 `frontend-vue/package-lock.json` |
| `npm.cmd run build` | 通过，Vue 3、JavaScript、Element Plus 和 Vite 生产构建成功 |
| `npm.cmd audit --audit-level=high --cache E:\Code\codex\java-demo\.npm-cache` | 通过，无 high/critical 漏洞；存在 Vite/esbuild moderate 提示 |
| `frontend-react` 执行 `npm.cmd run build` | 通过，确认 React 管理端未回归 |
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd test` | 通过，`Tests run: 2, Failures: 0, Errors: 0, Skipped: 0` |
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 通过，生成 `backend/app/target/java-demo-app-0.4.0-SNAPSHOT.jar` |

真实联调验收：

| 验收项 | 结果 |
|---|---|
| Vue 应用访问 `http://127.0.0.1:5174` | `200` |
| 通过 Vue Vite 代理访问 `GET /api/health` | `200` |
| 通过 Vue Vite 代理注册测试用户 | `200` |
| 通过 Vue Vite 代理登录测试用户 | `200`，成功签发 JWT |
| 通过 Vue Vite 代理访问当前用户 | `200` |
| 通过 Vue Vite 代理新增用户 | `200` |
| 通过 Vue Vite 代理分页查询用户 | `200` |
| 通过 Vue Vite 代理编辑用户 | `200` |
| 通过 Vue Vite 代理逻辑删除用户 | `200` |
| 浏览器登录 Vue 管理端 | 通过，进入首页并切换到“用户管理”工作区 |
| Vue 结构优化后的构建验证 | 通过，调整为 `layouts`、`views`、`composables` 后再次执行 `npm.cmd run build` 成功 |

验证说明：

| 项目 | 说明 |
|---|---|
| 后端验证端口 | `8091` |
| Vue 前端验证端口 | `5174` |
| 浏览器验证用户 | 初始验证用户 `vue_browser_20260526023310`；结构优化前验证用户 `vue_refactor_20260526032112`；结构优化后验证用户 `vue_struct_20260526063858` |
| HTTP 联调验证用户 | `vue_v04_20260526102255` |
| HTTP 联调新增用户 | `vue_managed_20260526102255`，验证后已逻辑删除 |
| Vue 结构优化 | 已将 Vue 端从 React 镜像式组件目录调整为 `layouts`、`views`、`composables` 分层，页面布局、操作入口和功能保持不变；优化后已通过构建和浏览器登录验证 |
| 构建提示 | Element Plus 主包导致 Vite chunk size warning，不影响当前版本运行 |
| 安全提示 | `npm audit` 仍提示 Vite/esbuild moderate 风险，修复需要强制升级到破坏性版本，暂不执行 `npm audit fix --force` |
| 会话提示 | 当前 PowerShell 环境同时存在 `PATH` 和 `Path`，会影响部分 `Start-Process`/Maven Wrapper 场景；本次验证通过单进程脚本和本地 Maven 完成 |

v0.4 Vue 项目结构记录：

| Vue 目录/文件 | 职责 | 说明 |
|---|---|---|
| `frontend-vue/src/App.vue` | 应用入口接线 | 负责启动恢复、登录态判断和当前 view 渲染 |
| `frontend-vue/src/layouts/AppLayout.vue` | 管理端外壳 | 负责侧边菜单、顶部用户信息和内容插槽 |
| `frontend-vue/src/views` | 页面级组件 | 放置登录页、首页和用户管理页 |
| `frontend-vue/src/composables` | 组合式业务逻辑 | 放置登录会话和用户管理状态逻辑 |
| `frontend-vue/src/api` | 后端请求封装 | 统一处理 API 调用和错误 |
| `frontend-vue/src/storage` | 本地持久化 | 封装 localStorage，保存登录态和最近查询条件 |

## v0.5 验证记录

自动化和构建验证：

| 命令 | 结果 |
|---|---|
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd -pl backend/gateway test` | 通过，`JwtGatewayFilterTest` 4 个用例成功 |
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 通过，后端 2 个集成测试和网关 4 个测试全部成功 |
| `frontend-react` 执行 `npm.cmd run build` | 通过，React TypeScript 类型检查和 Vite 生产构建成功 |
| `frontend-vue` 执行 `npm.cmd run build` | 通过，Vue 3、JavaScript、Element Plus 和 Vite 生产构建成功 |

真实联调验收：

| 验收项 | 结果 |
|---|---|
| 后端 `http://127.0.0.1:8091/api/health` | `200` |
| Gateway `http://127.0.0.1:8092/api/health` | `200`，确认 Gateway 可转发到后端 |
| Gateway `http://127.0.0.1:8092/v3/api-docs` | `200`，确认 OpenAPI JSON 经 Gateway 可访问 |
| 通过 Gateway 注册测试用户 | `200`，验证用户 `gateway_v05_20260526153518` |
| 通过 Gateway 登录测试用户 | `200`，成功签发 JWT |
| 通过 Gateway 不带 token 访问 `GET /api/users` | `401`，由网关拦截 |
| 通过 Gateway 携带 token 访问 `GET /api/users/me` | `200`，返回当前登录用户 |
| 通过 Gateway 携带 token 访问用户分页 | `200`，分页查询可用 |
| 直连后端不带 token 访问 `GET /api/users` | `401`，确认后端自身认证防线仍保留 |
| React 通过 Vite proxy 访问 Gateway | `200`，验证用户 `react_v05_20260526155149`，无 token 访问用户接口返回 `401` |
| Vue 通过 Vite proxy 访问 Gateway | `200`，验证用户 `vue_v05_20260526155150`，无 token 访问用户接口返回 `401` |

验证说明：

| 项目 | 说明 |
|---|---|
| 后端端口 | `8091`，v0.5 后主要作为开发调试直连端口 |
| Gateway 端口 | `8092`，v0.5 外部 API 统一入口 |
| MySQL | 复用 Docker Desktop 中已运行且 healthy 的 `java-demo-mysql` 容器 |
| 启动方式 | 本次运行时联调用 `D:\software\jdk-17.0.19\bin\java.exe -jar` 临时启动后端和 Gateway |
| 临时进程 | 验证结束后已停止本次临时启动的后端、Gateway、React dev server 和 Vue dev server 进程 |
| PowerShell 环境 | 当前会话仍存在 `PATH` / `Path` 重复导致 `Start-Process` 异常的问题；本次改用 `.NET ProcessStartInfo` 完成临时进程启动 |
| 构建提示 | React 和 Vue 均保留既有 Vite chunk size warning，不影响当前版本运行 |

## v0.5.1 验证记录

自动化和构建验证：

| 命令 | 结果 |
|---|---|
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd test` | 通过；`java-demo-app` 2 个测试、`java-demo-gateway` 6 个测试、`task-service` 1 个测试、`notification-service` 1 个测试均成功 |
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 通过；生成 `java-demo-app-0.5.1-SNAPSHOT.jar`、`java-demo-gateway-0.5.1-SNAPSHOT.jar`、`java-demo-task-service-0.5.1-SNAPSHOT.jar`、`java-demo-notification-service-0.5.1-SNAPSHOT.jar` |
| `frontend-react` 执行 `npm.cmd run build` | 通过，保留既有 Vite chunk size warning |
| `frontend-vue` 执行 `npm.cmd run build` | 通过，保留既有 Vite chunk size warning 和 VueUse 注释提示 |

真实 Gateway 联调验收：

| 验收项 | 结果 |
|---|---|
| MySQL 容器 | `java-demo-mysql` 为 `healthy` |
| MySQL database | 已确认 `java_demo`、`java_demo_task`、`java_demo_notification` 均存在 |
| 直连 `java-demo-app` 健康检查 | `http://127.0.0.1:8091/api/health` 返回 `200` |
| 直连 `task-service` 健康检查 | `http://127.0.0.1:8093/api/tasks/health` 返回 `200` |
| 直连 `notification-service` 健康检查 | `http://127.0.0.1:8094/api/notifications/health` 返回 `200` |
| Gateway 健康检查 | `http://127.0.0.1:8092/api/health`、`/api/tasks/health`、`/api/notifications/health` 均返回 `200` |
| Gateway 无 token 拦截 | 不带 token 访问 `GET /api/tasks` 和 `GET /api/notifications/my` 均返回 `401` |
| Gateway 登录链路 | 通过 Gateway 注册并登录测试用户 `v051_20260527115111`，成功签发 JWT |
| Gateway 任务链路 | 携带 token 创建任务成功，任务 ID 为 `1`；查询我的任务返回 `total=1`；更新状态为 `IN_PROGRESS` 成功 |
| Gateway 通知链路 | 查询我的通知返回 `total=2`；未读数为 `2`；单条已读和全部已读均成功 |
| Gateway OpenAPI | `http://127.0.0.1:8092/v3/api-docs` 返回 `200` |

验证说明：

| 项目 | 说明 |
|---|---|
| 启动方式 | 本次运行时联调用 `D:\software\jdk-17.0.19\bin\java.exe -jar` 临时启动四个后端 jar |
| 端口 | `java-demo-app` 使用 `8091`，Gateway 使用 `8092`，`task-service` 使用 `8093`，`notification-service` 使用 `8094` |
| 临时进程 | 验证结束后已停止本次临时启动的四个 Java 进程，并确认 `8091-8094` 无监听进程 |

## v0.5.2 验证记录

自动化和构建验证：

| 命令 | 结果 |
|---|---|
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd test` | 通过；四个后端模块测试均成功 |
| `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 通过；生成 `java-demo-app-0.5.2-SNAPSHOT.jar`、`java-demo-gateway-0.5.2-SNAPSHOT.jar`、`java-demo-task-service-0.5.2-SNAPSHOT.jar`、`java-demo-notification-service-0.5.2-SNAPSHOT.jar` |
| `frontend-react` 执行 `npm.cmd run build` | 通过，保留既有 Vite chunk size warning |
| `frontend-vue` 执行 `npm.cmd run build` | 通过，保留既有 Vite chunk size warning 和 VueUse 注释提示 |

真实 Gateway 和日志验收：

| 验收项 | 结果 |
|---|---|
| 临时端口 | 因用户 IntelliJ 进程正在占用 `8091-8094`，本次使用 `8252-8255` 启动四个 `0.5.2` jar；未停止用户进程 |
| Gateway 业务链路 | 通过临时 Gateway `8253` 完成注册、登录、当前用户、任务创建、我的任务、状态流转、通知查询、未读数和 OpenAPI 验证 |
| 文件日志 | 已验证 `logs/v052-java-demo-app-debug.log`、`logs/v052-task-service-debug.log`、`logs/v052-notification-service-debug.log` 均生成并写入关键日志 |
| requestId | 已通过外部请求头 `X-Request-Id: v052-debug-chain` 验证三个业务服务日志都包含同一 `requestId`；`task-service` 服务间调用会继续透传该值 |
| DEBUG 级别 | 已验证三个业务服务业务包 DEBUG 日志可输出当前用户、我的任务和未读通知数等调试信息 |
| WARN 级别 | 已单独启动 `java-demo-app`，设置 `JAVA_DEMO_LOG_LEVEL_ROOT=WARN` 和 `JAVA_DEMO_APP_LOG_LEVEL=WARN`，确认普通 INFO 请求日志不输出，认证失败 WARN 日志仍输出 |
| 敏感信息检查 | 已检查本次日志文件，不包含登录密码、完整 JWT 或 `Authorization` 字样 |
| 进程清理 | 本次临时启动的 Java 进程已停止，`8252-8255` 无监听进程 |

前端联动判断：

| 项目 | 结论 |
|---|---|
| React/Vue 页面改动 | 不需要；`v0.5.2` 是后端内部运行日志能力，不新增或改变用户可见接口、字段、状态或页面操作 |
| 前端回归验证 | 已执行 React 和 Vue 生产构建，确认现有前端不受影响 |

## 当前 milestone

当前已完成：

```text
docs/milestones/v0.5.2-backend-runtime-logging.md
```

下一步尚未开始：

```text
docs/milestones/v0.5.3-task-notification-frontends.md
```

`v0.5.3` 完成后的下一步：

```text
docs/milestones/v0.6-nacos.md
```

`v0.6` 完成后的下一步：

```text
docs/milestones/v0.7-redis-cache-rate-limit.md
```

## 端口调整记录

调整时间：2026-05-23，Asia/Shanghai。

本机占用端口范围：

| 起始端口 | 结束端口 | 处理规则 |
|---|---|---|
| `7991` | `8090` | 项目当前和后续服务不得使用 |
| `8146` | `8245` | 项目当前和后续服务不得使用 |

当前端口规划：

| 服务 | 端口 | 状态 |
|---|---|---|
| Spring Boot 后端 | `8091` | 已配置 |
| Swagger UI / OpenAPI JSON | `8091` | 跟随后端端口 |
| React 开发服务器 | `5173` | 已配置，未落入占用范围 |
| React Preview | `4173` | 已配置，未落入占用范围 |
| Vue 开发服务器 | `5174` | 已配置，未落入占用范围 |
| Vue Preview | `4174` | 已配置，未落入占用范围 |
| Spring Cloud Gateway | `8092` | 已配置，v0.5 外部 API 统一入口 |
| task-service | `8093` | 已配置，v0.5.1 任务服务端口 |
| notification-service | `8094` | 已配置，v0.5.1 通知服务端口 |
| 后续拆分业务服务 | `8095-8145` 或 `8246+` | 后续按需分配 |
| 本地 Nginx 非标准 HTTP/HTTPS | `8250` / `8251` | 后续 `v1.6` 如不使用 `80` / `443` 时优先使用 |
| MySQL Docker | `3306` | 已配置，未落入占用范围 |

端口调整验证：

| 验证项 | 结果 |
|---|---|
| 后端 `http://127.0.0.1:8091/api/health` | `200` |
| 后端 `http://127.0.0.1:8091/v3/api-docs` | `200`，标题为 `Java Demo API` |
| 后端 `http://127.0.0.1:8091/swagger-ui.html` | `200` |
| React `http://127.0.0.1:5173` | `200` |
| React 代理 `http://127.0.0.1:5173/api/health` | `200`，历史验证可转发到后端；v0.5 当前代理目标已调整为 Gateway `8092` |
| React 代理 `http://127.0.0.1:5173/v3/api-docs` | `200`，标题为 `Java Demo API` |
| Vue `http://127.0.0.1:5174` | `200` |
| Vue 代理 `http://127.0.0.1:5174/api/health` | `200`，历史验证可转发到后端；v0.5 当前代理目标已调整为 Gateway `8092` |
| Vue 代理登录和用户管理接口 | `200`，已验证登录、当前用户、分页、新增、编辑和逻辑删除 |
| Gateway `http://127.0.0.1:8092/api/health` | `200`，确认 Gateway 可转发到后端 `8091` |
| Gateway `http://127.0.0.1:8092/v3/api-docs` | `200`，确认接口文档可经 Gateway 访问 |
| Gateway JWT 校验 | 不带 token 访问 `/api/users` 返回 `401`，携带 token 可访问 `/api/users/me` 和用户分页 |
| React/Vue 经 Gateway 联调 | React `5173` 和 Vue `5174` 均已通过 Vite proxy 经 Gateway 完成登录态接口验证 |
| v0.5.1 端口规划 | task-service 使用 `8093`，notification-service 使用 `8094`；后续其他服务从 `8095-8145` 或 `8246+` 分配 |
| v0.5.1 端口配置 | `backend/task-service` 已使用 `8093`，`backend/notification-service` 已使用 `8094`，Gateway 已配置对应静态路由 |
| 验证进程 | 验证结束后已停止本次临时启动的后端、Gateway 和前端进程 |

## 已采纳执行路线

服务拆分路线：

| 阶段 | 服务形态 |
|---|---|
| `v0.1` - `v0.4` | 单体或简单多模块，先完成业务闭环 |
| `v0.5` | 引入 Gateway，统一外部入口 |
| `v0.5.1` | 新增任务和通知服务，形成跨服务业务闭环 |
| `v0.5.2` | 为用户、任务、通知三个业务服务建立控制台日志、文件日志和日志级别配置基线 |
| `v0.5.3` | React 和 Vue 都补齐任务管理与通知中心，承接任务/通知微服务的用户可见能力 |
| `v0.6` | 接入 Nacos，进入服务注册和配置中心阶段 |
| `v1.5` 前后 | 基于已有用户、任务、通知服务验证 Seata 和链路追踪 |

部署路线：

| 阶段 | 部署形态 |
|---|---|
| `v0.1` - `v0.4` | 本地进程 + Docker 基础设施 |
| `v0.5` - `v1.7` | Docker Compose 管理基础设施和部分应用；基础设施服务和集群节点保持独立容器 |
| `v1.8` | Docker Compose 与 Kubernetes 双部署 |
| `v1.9` | Jenkins CI/CD |

## 下一步建议

1. 如需保存当前 `v0.5.2` 稳定点，请用户手动提交 Git commit、手动打 tag 并手动推送 GitHub；Codex 不自动执行这些 Git 写操作。
2. 下一次开发从 `docs/milestones/v0.5.3-task-notification-frontends.md` 开始，在 React 和 Vue 两套前端中补齐任务管理与通知中心。
3. v0.5.3 完成后再进入 `docs/milestones/v0.6-nacos.md`，把 Gateway、用户、任务、通知服务接入 Nacos。
4. v0.6 完成后进入 `docs/milestones/v0.7-redis-cache-rate-limit.md`，验证用户校验、任务列表、通知未读数缓存和接口限流。
5. 保持当前部署路线：后端、网关、任务服务、通知服务和前端先用本地进程；MySQL 和后续 Nacos、Redis、RabbitMQ、Kafka、Elasticsearch、Seata、Jenkins 等服务使用 Docker Desktop 独立容器。

## 后续对 Codex 的推荐指令

```text
请读取 docs/ROADMAP.md、docs/DEVELOPMENT_RULES.md、docs/PROGRESS.md 和当前 milestone 文档。当前 v0.5.2 已完成，请从 docs/milestones/v0.5.3-task-notification-frontends.md 开始实现任务和通知前端 milestone；不要重复实现 v0.5.2。React 和 Vue 两套前端都需要补齐任务管理与通知中心，双端功能、布局、操作路径、筛选项、表格字段、操作按钮和错误提示尽量保持一致，但代码结构继续保留各自框架特点。新增前端代码必须补充详细中文注释，说明 API 封装、页面状态、表单校验、表格分页、本地缓存和错误处理。完成后运行前端构建、必要后端验证和真实联调，并更新文档。
```

## 完成记录

| 版本 | 状态 | 完成时间 | 备注 |
|---|---|---|---|
| `v0.1` | 已完成 | 2026-05-20 | 最小登录系统，已补充 Swagger UI 和中文注释；提交、tag 和推送由用户手动执行 |
| `v0.2` | 已完成 | 2026-05-22 | 用户管理 CRUD、分页、逻辑删除、改密、字段迁移；提交、tag 和推送由用户手动执行 |
| `v0.3` | 已完成 | 2026-05-23 | React 管理端、TypeScript、Ant Design、IndexedDB 登录态恢复；提交、tag 和推送由用户手动执行 |
| `v0.4` | 已完成 | 2026-05-26 | Vue 管理端、JavaScript、Element Plus、localStorage 登录态和最近查询条件；已优化为 Vue 常见项目结构；提交、tag 和推送由用户手动执行 |
| `v0.5` | 已完成 | 2026-05-26 | Spring Cloud Gateway、网关 JWT 校验、React/Vue 代理切换到 Gateway；提交、tag 和推送由用户手动执行 |
| `v0.5.1` | 已完成 | 2026-05-27 | 任务服务和通知服务 MVP；已通过 Maven test/package、React/Vue 构建回归和真实 Gateway 任务通知链路联调；提交、tag 和推送由用户手动执行 |
| `v0.5.2` | 已完成 | 2026-05-27 | 后端运行日志基线；三个业务服务已支持控制台日志、文件日志、requestId、日志级别配置和敏感信息保护；提交、tag 和推送由用户手动执行 |
| `v0.5.3` | 未开始 | - | 任务和通知前端；已完成 milestone 与前后端联动策略文档规划 |
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
| 端口 `8091` 可能被其他应用占用 | 后端应用无法启动 | 启动前检查端口，必要时临时调整到 `8250` 等非占用范围端口 |
| 端口 `8092` 可能被其他应用占用 | Gateway 无法启动，前端默认代理无法访问 API | 启动前检查端口，必要时临时调整 `GATEWAY_SERVER_PORT` 并同步更新前端代理和文档 |
| 端口 `8093` 可能被其他应用占用 | v0.5.1 task-service 无法启动，Gateway `/api/tasks/**` 路由不可用 | 启动前检查端口，必要时临时调整任务服务端口并同步 Gateway、前端代理和文档 |
| 端口 `8094` 可能被其他应用占用 | v0.5.1 notification-service 无法启动，任务通知链路不可用 | 启动前检查端口，必要时临时调整通知服务端口并同步 Gateway、服务调用配置和文档 |
| 端口 `5173` 可能被其他前端项目占用 | React 管理端开发服务器无法启动 | 停止占用进程或通过 Vite 参数临时调整端口 |
| 端口 `5174` 可能被其他前端项目占用 | Vue 管理端开发服务器无法启动 | 停止占用进程或通过 Vite 参数临时调整端口，并同步更新文档 |
| 占用端口范围 `7991-8090`、`8146-8245` | 新增服务若误用这些端口会启动失败或冲突 | 后续新增服务必须按 `docs/decisions/0008-local-port-allocation.md` 分配端口，`8093` 和 `8094` 已预留给 v0.5.1 |
| PowerShell 旧版本不支持 `-SkipHttpErrorCheck` | 直接验证 4xx 状态时命令参数不可用 | 使用 `try/catch` 捕获 HTTP 4xx 状态 |
| 当前 Codex 会话可能未刷新 Node.js PATH | `node -v` 可能无法显示 `v22.x.x`，影响前端开发验证 | 进入前端 milestone 前重启终端或 Codex 会话后再验证 Node.js 22 |
| PowerShell 执行策略阻止 `npm.ps1` | 直接运行 `npm` 可能失败 | 使用 `npm.cmd` 执行安装、启动和构建 |
| Vite/esbuild moderate audit 提示 | 开发服务器场景存在中等级别安全提示 | 当前不强制破坏性升级，后续可在前端依赖维护任务中处理 |
| 后续维护 Vue 端时误引入 TypeScript 模板 | Vue 管理端语言策略与学习目标不一致 | `frontend-vue` 继续保持 JavaScript，不生成 `tsconfig` 或 `.ts` / `.tsx` 业务代码 |
| 当前会话存在 `PATH` / `Path` 重复环境变量 | Maven Wrapper 或 `Start-Process` 可能启动失败 | 本次已使用本地 Maven 3.9.16 完成验证；后续可重启 Codex 会话或清理当前进程环境后再试 Wrapper |
| 后端日志误打印敏感信息 | 如果把密码、完整 JWT、Authorization header 或数据库密码写入日志，会造成安全风险 | 遵守 `docs/decisions/0012-backend-runtime-logging.md`，日志只打印 userId、业务 ID、错误码、耗时和脱敏摘要 |
| 后端能力已完成但前端未同步 | 用户只能通过 Swagger 或脚本验证任务/通知，影响全栈学习闭环 | 遵守 `docs/decisions/0013-frontend-backend-feature-sync.md`，后端用户可见能力变化时自动评估并补齐 React/Vue 前端 |
| React 和 Vue 功能漂移 | 两套前端如果功能、布局或操作路径不一致，会降低对比学习价值 | v0.5.3 开始要求双端菜单、页面结构、字段、操作和错误提示保持一致，代码结构仍保留各自框架特点 |
| 后续中间件容器边界混乱 | 如果多个服务共用一个容器，后续难以单独扩缩容或集群化 | 遵守 `docs/decisions/0010-docker-service-containerization.md`，每个服务和每个集群节点独立容器 |
| 中间件范围很大 | 容易一次性复杂化 | 严格按 milestone 单步推进 |
