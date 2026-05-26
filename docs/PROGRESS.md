# Project Progress

最后更新：2026-05-26，Asia/Shanghai。

## 当前状态

`v0.4 Vue Frontend` 已实现并完成 Vue 常见项目结构优化、Vue 前端构建、React 构建回归、后端 Maven package、Vue Vite 代理联调、真实浏览器登录验证和文档更新。

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
| 代码注释规范 | 已要求生成和修改的代码必须补充详细中文注释 |
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
| React 代理端口调整 | 已将 Vite 代理目标调整为 `http://localhost:8091` |
| Vue 代理端口配置 | 已将 Vite 代理目标配置为 `http://localhost:8091`，Vue 开发端口为 `5174` |

尚未完成：

| 项目 | 状态 |
|---|---|
| Git milestone commit、tag 和 push | 必须由用户手动执行，Codex 不自动提交、不自动打 tag、不自动推送 |
| 网关和微服务基础设施 | 未开始，按后续 milestone 逐步引入 |

## 环境观察

| 项目 | 当前观察 |
|---|---|
| Java | 目标版本 JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | 目标版本 Maven `3.9.16`，路径 `D:\software\apache-maven-3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |
| Node.js | 目标版本 Node.js `22.x`，用于 React TypeScript 和 Vue JavaScript 前端开发 |
| Docker | Docker Desktop 可用，MySQL `8.4` 单节点容器启动验证通过 |
| Git | 已初始化 Git 仓库，用户已提交 GitHub；后续提交、tag 和推送由用户手动执行 |
| 本机占用端口 | `7991-8090`、`8146-8245`；当前项目端口规划已避开 |

当前会话验证说明：

| 检查项 | 结果 |
|---|---|
| 直接执行 `D:\software\jdk-17.0.19\bin\java.exe -version` | 已确认 JDK `17.0.19` 可用 |
| 直接执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -v` | 已确认 Maven `3.9.16` 可用 |
| 临时设置 `JAVA_HOME=D:\software\jdk-17.0.19` 后执行 Maven | 已确认 Maven 可使用 Java `17.0.19` |
| 本次执行 `.\mvnw.cmd test` | 当前 PowerShell 会话中 Maven Wrapper 启动失败，报 `Cannot start maven from wrapper`；已改用用户配置的本地 Maven 3.9.16 完成同等验证 |
| 执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test` | 已通过，`AuthFlowIntegrationTest` 和 `UserManagementIntegrationTest` 覆盖 v0.1/v0.2 核心链路 |
| 执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package` | 已通过，生成 `backend/app/target/java-demo-app-0.4.0-SNAPSHOT.jar` |
| 访问 `GET /v3/api-docs` | 已确认返回 OpenAPI JSON |
| 访问 `GET /swagger-ui.html` | 已确认返回 Swagger UI 页面 |
| Node.js 22 | 已确认当前会话 `node -v` 为 `v22.22.3` |
| npm | PowerShell 执行 `npm.ps1` 受本机策略限制，已使用 `npm.cmd` 完成安装和构建 |
| 端口扫描 | 已确认当前显式配置端口中，只有旧后端 `8082`/历史文档 `8080` 落入占用范围；当前配置已改为 `8091` |
| 后端 `8091` 运行验证 | 已通过，`GET http://127.0.0.1:8091/api/health`、`/v3/api-docs`、`/swagger-ui.html` 均返回 `200` |
| React `5173` 代理验证 | 已通过，`GET http://127.0.0.1:5173`、`/api/health`、`/v3/api-docs` 均返回 `200`，确认 Vite 已代理到后端 `8091` |
| Vue `5174` 代理验证 | 已通过，`GET http://127.0.0.1:5174`、`/api/health`、登录、用户分页、新增、编辑和删除均可用 |
| Vue 浏览器登录验证 | 已通过，使用内置浏览器登录 Vue 管理端、进入首页，并切换到用户管理工作区 |
| Vue 结构优化构建验证 | 已通过，调整为 Vue 常见结构后再次执行 `frontend-vue` 的 `npm.cmd run build` 成功 |
| Vue 结构优化浏览器验证 | 已通过，调整为 `layouts`、`views`、`composables` 后仍可登录、查看首页并进入用户管理 |

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

## 当前 milestone

当前已完成：

```text
docs/milestones/v0.4-vue-frontend.md
```

建议下一步执行：

```text
docs/milestones/v0.5-gateway-jwt.md
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
| Spring Cloud Gateway | `8092` | 后续 `v0.5` 建议端口 |
| 后续拆分业务服务 | `8093-8145` 或 `8246+` | 后续按需分配 |
| 本地 Nginx 非标准 HTTP/HTTPS | `8250` / `8251` | 后续 `v1.6` 如不使用 `80` / `443` 时优先使用 |
| MySQL Docker | `3306` | 已配置，未落入占用范围 |

端口调整验证：

| 验证项 | 结果 |
|---|---|
| 后端 `http://127.0.0.1:8091/api/health` | `200` |
| 后端 `http://127.0.0.1:8091/v3/api-docs` | `200`，标题为 `Java Demo API` |
| 后端 `http://127.0.0.1:8091/swagger-ui.html` | `200` |
| React `http://127.0.0.1:5173` | `200` |
| React 代理 `http://127.0.0.1:5173/api/health` | `200`，确认请求可转发到后端 `8091` |
| React 代理 `http://127.0.0.1:5173/v3/api-docs` | `200`，标题为 `Java Demo API` |
| Vue `http://127.0.0.1:5174` | `200` |
| Vue 代理 `http://127.0.0.1:5174/api/health` | `200`，确认请求可转发到后端 `8091` |
| Vue 代理登录和用户管理接口 | `200`，已验证登录、当前用户、分页、新增、编辑和逻辑删除 |
| 验证进程 | 验证结束后已停止本次临时启动的后端和前端进程 |

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
2. 开始 `docs/milestones/v0.5-gateway-jwt.md`。
3. 在 v0.5 中引入 Spring Cloud Gateway，建议端口使用 `8092`，继续避开本机占用范围 `7991-8090`、`8146-8245`。
4. 保持当前部署路线：后端、网关和前端先用本地进程，MySQL 继续使用 Docker Desktop。

## 后续对 Codex 的推荐指令

```text
请读取 docs/ROADMAP.md、docs/DEVELOPMENT_RULES.md、docs/PROGRESS.md 和当前 milestone 文档，然后实现下一个可运行版本。完成后运行验证，并更新文档。
```

## 完成记录

| 版本 | 状态 | 完成时间 | 备注 |
|---|---|---|---|
| `v0.1` | 已完成 | 2026-05-20 | 最小登录系统，已补充 Swagger UI 和中文注释；提交、tag 和推送由用户手动执行 |
| `v0.2` | 已完成 | 2026-05-22 | 用户管理 CRUD、分页、逻辑删除、改密、字段迁移；提交、tag 和推送由用户手动执行 |
| `v0.3` | 已完成 | 2026-05-23 | React 管理端、TypeScript、Ant Design、IndexedDB 登录态恢复；提交、tag 和推送由用户手动执行 |
| `v0.4` | 已完成 | 2026-05-26 | Vue 管理端、JavaScript、Element Plus、localStorage 登录态和最近查询条件；已优化为 Vue 常见项目结构；提交、tag 和推送由用户手动执行 |
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
| 端口 `8091` 可能被其他应用占用 | 后端应用无法启动 | 启动前检查端口，必要时临时调整到 `8250` 等非占用范围端口 |
| 端口 `5173` 可能被其他前端项目占用 | React 管理端开发服务器无法启动 | 停止占用进程或通过 Vite 参数临时调整端口 |
| 端口 `5174` 可能被其他前端项目占用 | Vue 管理端开发服务器无法启动 | 停止占用进程或通过 Vite 参数临时调整端口，并同步更新文档 |
| 占用端口范围 `7991-8090`、`8146-8245` | 新增服务若误用这些端口会启动失败或冲突 | 后续新增服务必须按 `docs/decisions/0008-local-port-allocation.md` 分配端口 |
| PowerShell 旧版本不支持 `-SkipHttpErrorCheck` | 直接验证 4xx 状态时命令参数不可用 | 使用 `try/catch` 捕获 HTTP 4xx 状态 |
| 当前 Codex 会话可能未刷新 Node.js PATH | `node -v` 可能无法显示 `v22.x.x`，影响前端开发验证 | 进入前端 milestone 前重启终端或 Codex 会话后再验证 Node.js 22 |
| PowerShell 执行策略阻止 `npm.ps1` | 直接运行 `npm` 可能失败 | 使用 `npm.cmd` 执行安装、启动和构建 |
| Vite/esbuild moderate audit 提示 | 开发服务器场景存在中等级别安全提示 | 当前不强制破坏性升级，后续可在前端依赖维护任务中处理 |
| 后续维护 Vue 端时误引入 TypeScript 模板 | Vue 管理端语言策略与学习目标不一致 | `frontend-vue` 继续保持 JavaScript，不生成 `tsconfig` 或 `.ts` / `.tsx` 业务代码 |
| 当前会话存在 `PATH` / `Path` 重复环境变量 | Maven Wrapper 或 `Start-Process` 可能启动失败 | 本次已使用本地 Maven 3.9.16 完成验证；后续可重启 Codex 会话或清理当前进程环境后再试 Wrapper |
| 中间件范围很大 | 容易一次性复杂化 | 严格按 milestone 单步推进 |
