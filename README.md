# Java Demo

这是一个从 0 到 1 学习 Java 微服务开发的练习项目。业务范围刻意保持很小：围绕用户注册、登录、认证和用户管理逐步演进；技术范围会按 `docs/ROADMAP.md` 从单体 MVP 扩展到网关、注册中心、缓存、消息、搜索、可观测性、容器化、Kubernetes 和 Jenkins。

## 当前版本

当前已完成 `v0.5.3 Task And Notification Frontends`。在 `v0.5.1 Task And Notification Services` 和 `v0.5.2 Backend Runtime Logging` 的基础上，React 与 Vue 两套前端都已补齐“任务管理”和“通知中心”，可以通过浏览器完成任务查询、创建、编辑、状态流转、详情查看、逻辑删除、通知查询、未读数、单条已读和全部已读。

下一步规划为 `v0.6 Nacos`。该版本用于把 Gateway、用户服务、任务服务和通知服务接入 Nacos，验证服务注册发现和配置中心能力。

| 项目 | 内容 |
|---|---|
| 核心能力 | 注册、登录、JWT 签发、网关 JWT 校验、获取当前登录用户、用户管理 CRUD、任务创建/分配/状态流转、通知创建/查询/未读数/已读标记、后端运行日志、React 任务/通知管理端、Vue 任务/通知管理端 |
| 后端 | Spring Boot `3.3.5` |
| 网关 | Spring Cloud Gateway `2023.0.3`，默认端口 `8092` |
| 任务服务 | `task-service`，默认端口 `8093` |
| 通知服务 | `notification-service`，默认端口 `8094` |
| ORM | MyBatis Plus `3.5.7` |
| 数据库 | MySQL `8.4` Docker 单节点 |
| 认证 | JWT |
| 日志 | SLF4J + Logback，控制台日志、`logs/*.log` 文件日志、`requestId`、可配置级别 |
| 接口文档 | Springdoc OpenAPI `2.6.0`、Swagger UI |
| 前端 | React `18`、TypeScript、Ant Design `5`；Vue `3`、JavaScript、Element Plus |
| 前端缓存 | React 端使用 IndexedDB；Vue 端使用 localStorage |
| Java | JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | Maven Wrapper，发行版 `3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |
| Node.js | Node.js `22.x` |

## 项目结构

```text
E:\Code\codex\java-demo
├─ backend
│  ├─ app
│  │  ├─ src/main/java/com/example/javademo/app
│  │  ├─ src/main/resources
│  │  └─ src/test
│  ├─ gateway
│  │  ├─ src/main/java/com/example/javademo/gateway
│  │  ├─ src/main/resources
│  │  └─ src/test
│  ├─ task-service
│  │  ├─ src/main/java/com/example/javademo/task
│  │  ├─ src/main/resources
│  │  └─ src/test
│  └─ notification-service
│     ├─ src/main/java/com/example/javademo/notification
│     ├─ src/main/resources
│     └─ src/test
├─ frontend-react
│  ├─ src
│  ├─ package.json
│  └─ vite.config.ts
├─ frontend-vue
│  ├─ src
│  ├─ package.json
│  └─ vite.config.js
├─ docs
│  ├─ ROADMAP.md
│  ├─ DEVELOPMENT_RULES.md
│  ├─ PROGRESS.md
│  ├─ decisions
│  └─ milestones
├─ infra
│  └─ docker-compose
│     └─ mysql
├─ .mvn
├─ mvnw
├─ mvnw.cmd
└─ pom.xml
```

## 环境准备

在当前 Codex 或 PowerShell 会话中，如果系统环境变量还没有刷新，可以先临时设置：

```powershell
$env:JAVA_HOME='D:\software\jdk-17.0.19'
$env:Path='D:\software\jdk-17.0.19\bin;' + $env:Path
$env:MAVEN_USER_HOME=(Resolve-Path '.mvn').Path + '\user-home'
```

验证 Maven Wrapper：

```powershell
.\mvnw.cmd -v
```

验证 Node.js：

```powershell
node -v
npm.cmd -v
```

如果 PowerShell 提示 `npm.ps1 cannot be loaded because running scripts is disabled`，请使用 `npm.cmd`。本项目 README 和脚本示例统一使用 `npm.cmd`，避免依赖本机执行策略。

项目内 `.mvn/maven.config` 已配置：

```text
-Dmaven.repo.local=D:/software/maven_download
```

## 启动 MySQL

```powershell
docker compose -f infra\docker-compose\mysql\docker-compose.yml up -d
```

检查容器：

```powershell
docker ps --filter "name=java-demo-mysql"
```

默认连接信息：

| 项目 | 值 |
|---|---|
| Host | `localhost` |
| Port | `3306` |
| Database | `java_demo`、`java_demo_task`、`java_demo_notification` |
| Username | `java_demo` |
| Password | `java_demo_pwd` |
| Root Password | `root_pwd` |

`infra/docker-compose/mysql/init/01-create-v051-databases.sql` 会在 MySQL 数据卷首次创建时自动创建任务库和通知库。如果本地已经有旧的 `java_demo_mysql_data` 数据卷，MySQL 官方镜像不会重新执行初始化脚本，可以手动补齐：

```powershell
docker exec java-demo-mysql mysql -uroot -proot_pwd -e "CREATE DATABASE IF NOT EXISTS java_demo_task DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE DATABASE IF NOT EXISTS java_demo_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; GRANT ALL PRIVILEGES ON java_demo_task.* TO 'java_demo'@'%'; GRANT ALL PRIVILEGES ON java_demo_notification.* TO 'java_demo'@'%'; FLUSH PRIVILEGES;"
```

如果需要停止 MySQL：

```powershell
docker compose -f infra\docker-compose\mysql\docker-compose.yml stop
```

## 构建与测试

运行自动化测试：

```powershell
.\mvnw.cmd test
```

打包可执行 jar：

```powershell
.\mvnw.cmd package
```

当前集成测试代码覆盖注册、重复注册、登录、JWT 查询当前用户、无 token 拦截、错误密码拦截、用户分页、详情、创建、更新、逻辑删除、修改密码、任务创建/状态流转/逻辑删除、通知创建/未读数/已读标记和 OpenAPI JSON 生成；网关测试覆盖公开路径放行、无 token 拦截、有效 token 放行、无效 token 拦截以及任务/通知健康检查白名单。

`v0.5.3` 已使用本地 Maven `D:\software\apache-maven-3.9.16\bin\mvn.cmd` 执行 `test` 和 `package` 并通过；Maven 本地仓库继续使用 `D:\software\maven_download`。本版本未修改后端业务代码，因此后端 jar 版本仍为 `0.5.2-SNAPSHOT`。

## 启动后端

方式一：使用 Spring Boot Maven 插件。

```powershell
.\mvnw.cmd -pl backend/app spring-boot:run
```

方式二：运行已打包 jar。

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\app\target\java-demo-app-0.5.2-SNAPSHOT.jar
```

后端默认端口：

| 服务 | 地址 |
|---|---|
| 后端 API 调试入口 | `http://localhost:8091` |
| 健康检查 | `http://localhost:8091/api/health` |
| Swagger UI | `http://localhost:8091/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8091/v3/api-docs` |

后端 `8091` 从 v0.5 开始主要作为开发调试直连入口。前端和外部 API 验收应优先访问 Gateway `8092`。

## 启动 Gateway

先确认 MySQL、后端、任务服务和通知服务已启动，再启动 Gateway。如果只验证登录和用户管理，任务服务与通知服务可以暂时不启动；但访问 `/api/tasks/**` 或 `/api/notifications/**` 时必须启动对应服务。

方式一：使用 Spring Boot Maven 插件。

```powershell
.\mvnw.cmd -pl backend/gateway spring-boot:run
```

方式二：运行已打包 jar。

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\gateway\target\java-demo-gateway-0.5.2-SNAPSHOT.jar
```

Gateway 默认端口：

| 服务 | 地址 |
|---|---|
| 外部 API 统一入口 | `http://localhost:8092` |
| 健康检查 | `http://localhost:8092/api/health` |
| Swagger UI | `http://localhost:8092/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8092/v3/api-docs` |

Gateway 当前使用静态地址转发到用户服务 `8091`、任务服务 `8093` 和通知服务 `8094`，可通过环境变量 `JAVA_DEMO_BACKEND_URI`、`JAVA_DEMO_TASK_SERVICE_URI`、`JAVA_DEMO_NOTIFICATION_SERVICE_URI` 覆盖。后续接入 Nacos 后，这些地址会演进为服务发现路由。

## 启动任务服务和通知服务

先确认 MySQL 中已存在 `java_demo_task` 和 `java_demo_notification`，再启动两个新服务。

```powershell
.\mvnw.cmd -pl backend/task-service spring-boot:run
.\mvnw.cmd -pl backend/notification-service spring-boot:run
```

或运行已打包 jar：

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\task-service\target\java-demo-task-service-0.5.2-SNAPSHOT.jar
D:\software\jdk-17.0.19\bin\java.exe -jar backend\notification-service\target\java-demo-notification-service-0.5.2-SNAPSHOT.jar
```

服务地址：

| 服务 | 地址 |
|---|---|
| task-service 健康检查 | `http://localhost:8093/api/tasks/health` |
| task-service Swagger UI | `http://localhost:8093/swagger-ui.html` |
| notification-service 健康检查 | `http://localhost:8094/api/notifications/health` |
| notification-service Swagger UI | `http://localhost:8094/swagger-ui.html` |

## 启动 React 管理端

先确认 MySQL、后端和 Gateway 已启动，再启动前端：

```powershell
cd frontend-react
npm.cmd install
npm.cmd run dev
```

React 管理端默认地址：

```text
http://127.0.0.1:5173
```

开发环境中，Vite 会把 `/api` 和 `/v3/api-docs` 代理到 Gateway `http://localhost:8092`，因此浏览器访问前端时不会遇到跨域问题，并且所有外部 API 请求都会先经过网关 JWT 校验。

## 启动 Vue 管理端

先确认 MySQL、后端和 Gateway 已启动，再启动 Vue 前端：

```powershell
cd frontend-vue
npm.cmd install
npm.cmd run dev
```

Vue 管理端默认地址：

```text
http://127.0.0.1:5174
```

Vue 端使用 Vue `3`、JavaScript 和 Element Plus，不启用 TypeScript 模板。开发环境中，Vite 会把 `/api` 和 `/v3/api-docs` 代理到 Gateway `http://localhost:8092`，端口 `5174` 和 preview 端口 `4174` 均避开了本机占用范围。

## 本地端口规划

当前本机 `7991-8090`、`8146-8245` 两段端口已被占用，项目当前和后续新增服务都必须避开这两个范围。

| 服务 | 当前/建议端口 | 说明 |
|---|---|---|
| Spring Boot 后端 | `8091` | 后端固定端口，v0.5 后主要用于开发调试直连 |
| Spring Cloud Gateway | `8092` | v0.5 外部 API 统一入口，前端默认代理到该端口 |
| task-service | `8093` | v0.5.1 任务服务 |
| notification-service | `8094` | v0.5.1 通知服务 |
| React 开发服务器 | `5173` | `v0.3` React 管理端 |
| React Preview | `4173` | `npm.cmd run preview` |
| Vue 开发服务器 | `5174` | `v0.4` Vue 管理端，避开 React 端口 |
| Vue Preview | `4174` | `v0.4` Vue 生产构建预览 |
| 后续拆分服务 | `8095-8145` 或 `8246+` | 不使用 `8146-8245` |
| 本地 Nginx 非标准 HTTP/HTTPS | `8250` / `8251` | 如不使用 `80` / `443`，优先使用该范围 |
| MySQL Docker | `3306` | 当前 MySQL 单节点 |

React 前端生产构建：

```powershell
cd frontend-react
npm.cmd run build
```

Vue 前端生产构建：

```powershell
cd frontend-vue
npm.cmd run build
```

`v0.3` 的 React 管理端已实现：

| 页面/能力 | 说明 |
|---|---|
| 登录页 | 调用 `/api/auth/login` 获取 JWT |
| 首页 | 展示当前登录用户，并验证 `/api/users/me` |
| 登录态恢复 | token、用户和登录时间保存到 IndexedDB 的 `auth_session` |
| 用户列表 | 调用 `/api/users` 分页查询用户 |
| 最近查询 | 用户列表查询条件保存到 IndexedDB 的 `recent_users_query` |
| 新增用户 | 调用 `POST /api/users` |
| 编辑用户 | 调用 `PUT /api/users/{id}` |
| 逻辑删除 | 调用 `DELETE /api/users/{id}` |

`v0.4` 的 Vue 管理端已实现：

| 页面/能力 | 说明 |
|---|---|
| 登录页 | 调用 `/api/auth/login` 获取 JWT，并把登录结果交给根组件保存到 localStorage |
| 首页 | 展示当前登录用户，并验证 `/api/users/me` |
| 用户列表 | 调用 `/api/users` 分页查询用户，并保存最近查询条件 |
| 新增用户 | 调用 `POST /api/users` |
| 编辑用户 | 调用 `PUT /api/users/{id}` |
| 逻辑删除 | 调用 `DELETE /api/users/{id}` |

Vue 管理端保持与 React 管理端一致的业务功能和操作路径，但目录组织采用更常见的 Vue 分层：

| Vue 文件/目录 | 职责 |
|---|---|
| `frontend-vue/src/App.vue` | 应用入口接线，负责启动恢复、登录态判断和当前 view 渲染 |
| `frontend-vue/src/layouts/AppLayout.vue` | 管理端外壳，负责侧边菜单、顶部用户信息和内容插槽 |
| `frontend-vue/src/views` | 页面级组件，包含登录页、首页和用户管理页 |
| `frontend-vue/src/composables` | 组合式业务逻辑，包含登录会话和用户管理状态逻辑 |
| `frontend-vue/src/api` | 后端请求封装 |
| `frontend-vue/src/storage` | localStorage 持久化封装 |

`v0.5.3` 已完成的前端扩展：

| 前端能力 | 说明 |
|---|---|
| React 任务管理 | 使用 TypeScript + Ant Design，已支持任务范围筛选、状态筛选、负责人筛选、分页查询、创建、编辑、详情、状态流转和逻辑删除 |
| React 通知中心 | 使用 TypeScript + Ant Design，已支持通知筛选、分页查询、未读数、单条已读和全部已读 |
| Vue 任务管理 | 使用 JavaScript + Element Plus，延续 `views`、`composables`、`api`、`storage` 分层，业务路径与 React 保持一致 |
| Vue 通知中心 | 使用 JavaScript + Element Plus，已支持与 React 一致的通知列表、未读数和已读操作 |
| 双端一致性 | React 和 Vue 菜单名称、页面布局、筛选项、表格字段、操作按钮、空数据与错误提示尽量保持一致 |
| 本地查询缓存 | React 使用 IndexedDB 保存任务/通知最近查询条件；Vue 使用 localStorage 保存同类查询条件 |
| 代码注释 | 新增前端代码已补充中文注释，说明 API 封装、页面状态、表单校验、表格分页、本地缓存和错误处理 |

## API

以下示例默认访问 Gateway `8092`。如果只是调试后端单体应用，也可以临时把地址改为后端直连端口 `8091`。

统一响应结构：

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

接口列表：

| 方法 | 路径 | 说明 | 是否需要 JWT |
|---|---|---|---|
| `GET` | `/api/health` | 健康检查 | 否 |
| `POST` | `/api/auth/register` | 注册用户 | 否 |
| `POST` | `/api/auth/login` | 登录并返回 JWT | 否 |
| `GET` | `/api/users/me` | 获取当前用户 | 是 |
| `GET` | `/api/users` | 用户分页查询，支持 `current`、`size`、`username`、`status` | 是 |
| `GET` | `/api/users/{id}` | 用户详情 | 是 |
| `POST` | `/api/users` | 创建用户 | 是 |
| `PUT` | `/api/users/{id}` | 更新用户昵称、状态、角色 | 是 |
| `DELETE` | `/api/users/{id}` | 逻辑删除用户 | 是 |
| `PUT` | `/api/users/{id}/password` | 修改用户密码 | 是 |
| `GET` | `/api/tasks/health` | 任务服务健康检查 | 否 |
| `POST` | `/api/tasks` | 创建任务并生成通知 | 是 |
| `GET` | `/api/tasks/my` | 查询当前用户创建或负责的任务 | 是 |
| `GET` | `/api/tasks` | 任务分页查询，支持 `current`、`size`、`status`、`assigneeUserId` | 是 |
| `GET` | `/api/tasks/{id}` | 任务详情 | 是 |
| `PUT` | `/api/tasks/{id}` | 更新任务标题、描述、负责人、优先级和截止时间 | 是 |
| `PUT` | `/api/tasks/{id}/status` | 修改任务状态并生成通知 | 是 |
| `DELETE` | `/api/tasks/{id}` | 逻辑删除任务 | 是 |
| `GET` | `/api/notifications/health` | 通知服务健康检查 | 否 |
| `POST` | `/api/notifications` | 创建通知，主要供服务间调用和 Swagger 调试 | 是 |
| `GET` | `/api/notifications/my` | 查询当前用户收到的通知 | 是 |
| `GET` | `/api/notifications/my/unread-count` | 查询当前用户未读通知数 | 是 |
| `PUT` | `/api/notifications/{id}/read` | 标记单条通知已读 | 是 |
| `PUT` | `/api/notifications/read-all` | 当前用户通知全部已读 | 是 |

注册请求：

```powershell
$body = @{ username = "alice"; password = "secret123"; nickname = "Alice" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8092/api/auth/register -ContentType "application/json" -Body $body
```

登录请求：

```powershell
$body = @{ username = "alice"; password = "secret123" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8092/api/auth/login -ContentType "application/json" -Body $body
$token = $login.data.accessToken
```

查询当前用户：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8092/api/users/me -Headers @{ Authorization = "Bearer $token" }
```

用户分页查询：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8092/api/users?current=1&size=10&username=alice&status=1" -Headers @{ Authorization = "Bearer $token" }
```

创建用户：

```powershell
$body = @{ username = "bob"; password = "secret123"; nickname = "Bob"; status = 1; role = "USER" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8092/api/users -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

更新用户：

```powershell
$body = @{ nickname = "Bobby"; status = 1; role = "ADMIN" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri http://localhost:8092/api/users/2 -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

修改密码：

```powershell
$body = @{ password = "newSecret123" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri http://localhost:8092/api/users/2/password -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

逻辑删除用户：

```powershell
Invoke-RestMethod -Method Delete -Uri http://localhost:8092/api/users/2 -Headers @{ Authorization = "Bearer $token" }
```

创建任务：

```powershell
$body = @{ title = "学习 v0.5.1 微服务拆分"; description = "创建任务并验证通知生成"; assigneeUserId = 1; priority = "HIGH" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8092/api/tasks -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

查询我的任务和通知：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8092/api/tasks/my?current=1&size=10" -Headers @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Method Get -Uri "http://localhost:8092/api/notifications/my?current=1&size=10" -Headers @{ Authorization = "Bearer $token" }
Invoke-RestMethod -Method Get -Uri http://localhost:8092/api/notifications/my/unread-count -Headers @{ Authorization = "Bearer $token" }
```

## Swagger UI

启动后端后访问：

```text
http://localhost:8092/swagger-ui.html
```

Gateway 当前默认转发 `java-demo-app` 的 Swagger UI。任务服务和通知服务也各自提供独立 Swagger UI，分别访问 `http://localhost:8093/swagger-ui.html` 和 `http://localhost:8094/swagger-ui.html`。访问需要登录的接口时，先调用登录接口拿到 `accessToken`，再点击页面右上角 `Authorize`，在 `bearerAuth` 中填入登录返回的 token。

OpenAPI JSON 地址：

```text
http://localhost:8092/v3/api-docs
```

## 配置项

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `JAVA_DEMO_DB_USERNAME` | `java_demo` | MySQL 用户名 |
| `JAVA_DEMO_DB_PASSWORD` | `java_demo_pwd` | MySQL 密码 |
| `JAVA_DEMO_JWT_SECRET` | `java-demo-v0-1-local-secret-change-me-32chars` | JWT 签名密钥，至少 32 字节 |
| `JAVA_DEMO_JWT_EXPIRATION_SECONDS` | `7200` | JWT 有效期，单位秒 |
| `SERVER_PORT` | `8091` | java-demo-app 本地启动端口 |
| `GATEWAY_SERVER_PORT` | `8092` | Gateway 本地启动端口 |
| `TASK_SERVER_PORT` | `8093` | task-service 本地启动端口 |
| `NOTIFICATION_SERVER_PORT` | `8094` | notification-service 本地启动端口 |
| `JAVA_DEMO_TASK_DB_USERNAME` | `java_demo` | task-service MySQL 用户名 |
| `JAVA_DEMO_TASK_DB_PASSWORD` | `java_demo_pwd` | task-service MySQL 密码 |
| `JAVA_DEMO_NOTIFICATION_DB_USERNAME` | `java_demo` | notification-service MySQL 用户名 |
| `JAVA_DEMO_NOTIFICATION_DB_PASSWORD` | `java_demo_pwd` | notification-service MySQL 密码 |
| `JAVA_DEMO_BACKEND_URI` | `http://localhost:8091` | Gateway 转发到用户/认证服务的静态地址 |
| `JAVA_DEMO_TASK_SERVICE_URI` | `http://localhost:8093` | Gateway 转发到任务服务的静态地址 |
| `JAVA_DEMO_NOTIFICATION_SERVICE_URI` | `http://localhost:8094` | Gateway 转发到通知服务的静态地址 |
| `JAVA_DEMO_USER_SERVICE_URL` | `http://localhost:8091` | task-service 调用用户服务的静态地址 |
| `JAVA_DEMO_NOTIFICATION_SERVICE_URL` | `http://localhost:8094` | task-service 调用通知服务的静态地址 |
| `JAVA_DEMO_LOG_LEVEL_ROOT` | `INFO` | 三个业务服务的 root 日志级别 |
| `JAVA_DEMO_APP_LOG_LEVEL` | `INFO` | `java-demo-app` 业务包日志级别 |
| `JAVA_DEMO_TASK_LOG_LEVEL` | `INFO` | `task-service` 业务包日志级别 |
| `JAVA_DEMO_NOTIFICATION_LOG_LEVEL` | `INFO` | `notification-service` 业务包日志级别 |
| `JAVA_DEMO_APP_LOG_FILE` | `logs/java-demo-app.log` | `java-demo-app` 文件日志路径 |
| `JAVA_DEMO_TASK_LOG_FILE` | `logs/task-service.log` | `task-service` 文件日志路径 |
| `JAVA_DEMO_NOTIFICATION_LOG_FILE` | `logs/notification-service.log` | `notification-service` 文件日志路径 |
| `JAVA_DEMO_LOG_MAX_FILE_SIZE` | `10MB` | 单个滚动日志文件最大体积 |
| `JAVA_DEMO_LOG_MAX_HISTORY` | `7` | 滚动日志保留数量 |

## 后端日志

`v0.5.2` 已为三个业务服务建立本地日志基线：

| 服务 | 默认日志文件 | 关键日志 |
|---|---|---|
| `java-demo-app` | `logs/java-demo-app.log` | 服务启动摘要、请求入口/完成、注册、登录、JWT 解析、用户管理、异常处理 |
| `task-service` | `logs/task-service.log` | 服务启动摘要、请求入口/完成、任务创建、查询、状态流转、服务间调用、异常处理 |
| `notification-service` | `logs/notification-service.log` | 服务启动摘要、请求入口/完成、通知创建、查询、未读数、已读标记、异常处理 |

日志格式包含服务名、线程和 `requestId`。外部请求可以传入 `X-Request-Id`；未传时业务服务会自动生成。`task-service` 调用 `java-demo-app` 和 `notification-service` 时会透传当前 `requestId`，便于在多个日志文件中串起同一次任务/通知链路。

本项目日志规则要求：不打印明文密码、密码哈希、完整 JWT、Authorization header、数据库密码或真实密钥。调试时可以临时开启业务包 DEBUG：

```powershell
$env:JAVA_DEMO_APP_LOG_LEVEL='DEBUG'
$env:JAVA_DEMO_TASK_LOG_LEVEL='DEBUG'
$env:JAVA_DEMO_NOTIFICATION_LOG_LEVEL='DEBUG'
```

如果只想观察告警和错误，可以设置：

```powershell
$env:JAVA_DEMO_LOG_LEVEL_ROOT='WARN'
```

## 数据库升级

`v0.2` 在 `sys_user` 表上新增了 `role`、`deleted`、`last_login_at` 字段。新库会通过 `schema.sql` 直接创建完整表结构；如果本地已经存在 `v0.1` 表，应用启动时会通过轻量迁移器自动检查并补齐缺失字段。

当前仍未引入 Flyway 或 Liquibase，数据库迁移先保持最小实现；后续里程碑如果迁移脚本变复杂，再单独引入专业迁移工具。

## 前端验证记录

本次 `v0.3` 验证内容：

| 验证项 | 结果 |
|---|---|
| `npm.cmd install` | 通过，生成 `frontend-react/package-lock.json` |
| `npm.cmd run build` | 通过，Vue 习惯结构优化后已再次验证 |
| `npm.cmd audit --audit-level=high` | 通过，无 high/critical 漏洞；仍存在 Vite/esbuild 相关 moderate 提示 |
| 前端页面访问 | `http://127.0.0.1:5173` 返回 `200` |
| Vite 代理 | 通过前端端口访问 `/api/health`、登录、用户分页均返回 `200` |
| 浏览器登录 | 通过 |
| 刷新后恢复登录态 | 通过，IndexedDB 会话可恢复 |
| 用户列表 | 通过 |
| 新增用户 | 通过 |
| 编辑用户 | 通过 |

构建提示：Ant Design 进入主包后，Vite 提示单个 chunk 超过 500KB。这不影响 `v0.3` 可运行版本；后续页面增多后可以再通过路由懒加载或 `manualChunks` 优化。

本次 `v0.4` 验证内容：

| 验证项 | 结果 |
|---|---|
| `node -v` | 通过，输出 `v22.22.3` |
| `npm.cmd install` | 通过，生成 `frontend-vue/package-lock.json` |
| `npm.cmd run build` | 通过 |
| `npm.cmd audit --audit-level=high --cache E:\Code\codex\java-demo\.npm-cache` | 通过，无 high/critical 漏洞；仍存在 Vite/esbuild moderate 提示 |
| React 构建回归 | 通过，`frontend-react` 执行 `npm.cmd run build` 成功 |
| 后端 Maven package | 通过，生成 `backend/app/target/java-demo-app-0.4.0-SNAPSHOT.jar` |
| Vue 页面访问 | `http://127.0.0.1:5174` 返回 `200` |
| Vue Vite 代理 | 通过前端端口访问 `/api/health`、登录、用户分页、新增、编辑和删除均成功 |
| 浏览器登录 Vue 管理端 | 通过，进入“当前用户”和“用户管理”工作区 |
| Vue 结构优化 | 通过，已调整为 `layouts`、`views`、`composables`、`api`、`storage` 的 Vue 常见结构，功能和样式保持不变；结构优化后浏览器验证用户为 `vue_struct_20260526063858` |

构建提示：Element Plus 进入主包后，Vite 提示单个 chunk 超过 500KB；同时 `npm audit` 提示 Vite/esbuild moderate 风险。两者都不影响当前 `v0.4` 可运行版本，后续可在前端依赖维护或路由拆分任务中处理。

本次 `v0.5` 验证内容：

| 验证项 | 结果 |
|---|---|
| Maven reactor package | 通过，`java-demo-app` 和 `java-demo-gateway` 均生成 `0.5.0-SNAPSHOT` jar |
| 网关过滤器测试 | 通过，覆盖公开路径、无 token、有效 token 和无效 token |
| React 构建回归 | 通过，`npm.cmd run build` 成功 |
| Vue 构建回归 | 通过，`npm.cmd run build` 成功 |
| Gateway 健康检查转发 | `http://127.0.0.1:8092/api/health` 返回 `200` |
| Gateway OpenAPI 转发 | `http://127.0.0.1:8092/v3/api-docs` 返回 `200` |
| Gateway 登录链路 | 通过 Gateway 注册并登录用户 `gateway_v05_20260526153518` |
| Gateway JWT 拦截 | 不带 token 访问 `GET /api/users` 返回 `401` |
| Gateway JWT 放行 | 携带 token 访问 `/api/users/me` 和用户分页成功 |
| React Gateway 代理联调 | React `5173` 经 Vite proxy 访问 Gateway 成功，验证用户 `react_v05_20260526155149` |
| Vue Gateway 代理联调 | Vue `5174` 经 Vite proxy 访问 Gateway 成功，验证用户 `vue_v05_20260526155150` |

本次 `v0.5.1` 验证内容：

| 项目 | 状态 |
|---|---|
| `task-service` 模块 | 已新增代码、配置、SQL 和集成测试 |
| `notification-service` 模块 | 已新增代码、配置、SQL 和集成测试 |
| Gateway 静态路由 | 已新增 `/api/tasks/**` -> `8093`、`/api/notifications/**` -> `8094` |
| Gateway JWT 白名单 | 已放行 `/api/tasks/health` 和 `/api/notifications/health` |
| MySQL 初始化 | 已新增 `java_demo_task`、`java_demo_notification` 初始化脚本 |
| MySQL 容器状态 | 已确认 `java-demo-mysql` 当前为 `healthy` |
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；`java-demo-app` 2 个测试、`java-demo-gateway` 6 个测试、`task-service` 1 个测试、`notification-service` 1 个测试均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；已生成四个 `0.5.1-SNAPSHOT` 可执行 jar |
| Gateway 健康检查 | 已通过 `8092` 访问 `/api/health`、`/api/tasks/health`、`/api/notifications/health` |
| Gateway JWT 拦截 | 不带 token 访问 `GET /api/tasks` 和 `GET /api/notifications/my` 均返回 `401` |
| Gateway 任务通知链路 | 已通过 Gateway 注册/登录用户，创建任务、查询我的任务、更新任务状态、查询通知、查询未读数、单条已读和全部已读均成功 |
| Gateway OpenAPI | 已通过 Gateway 访问 `/v3/api-docs`，返回 `200` |
| React 构建回归 | 已执行 `npm.cmd run build`，通过；保留既有 chunk size warning |
| Vue 构建回归 | 已执行 `npm.cmd run build`，通过；保留既有 chunk size warning 和 VueUse 注释提示 |

本次 `v0.5.2` 验证内容：

| 项目 | 状态 |
|---|---|
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；四个后端模块测试均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；已生成四个 `0.5.2-SNAPSHOT` 可执行 jar |
| React 构建回归 | 已执行 `npm.cmd run build`，通过；保留既有 chunk size warning |
| Vue 构建回归 | 已执行 `npm.cmd run build`，通过；保留既有 chunk size warning 和 VueUse 注释提示 |
| 真实 Gateway 联调 | 使用临时端口 `8252-8255` 启动四个 `0.5.2` jar，避免影响 IntelliJ 中占用 `8091-8094` 的服务；通过 Gateway 完成注册、登录、任务创建、任务查询、状态流转、通知查询、未读数和 OpenAPI 验证 |
| 文件日志 | 已验证 `logs/v052-java-demo-app-debug.log`、`logs/v052-task-service-debug.log`、`logs/v052-notification-service-debug.log` 均写入启动摘要、请求日志、`requestId` 和关键业务日志 |
| DEBUG 级别 | 已验证 `JAVA_DEMO_APP_LOG_LEVEL=DEBUG`、`JAVA_DEMO_TASK_LOG_LEVEL=DEBUG`、`JAVA_DEMO_NOTIFICATION_LOG_LEVEL=DEBUG` 下可看到当前用户、我的任务、未读通知数等调试日志 |
| WARN 级别 | 已验证 `JAVA_DEMO_LOG_LEVEL_ROOT=WARN` 和 `JAVA_DEMO_APP_LOG_LEVEL=WARN` 下，普通 INFO 请求日志不输出，认证失败 WARN 日志仍输出 |
| 敏感信息检查 | 已确认本次日志文件中未出现登录密码、完整 JWT 或 `Authorization` 字样 |
| 临时端口释放 | 验证结束后已停止本次临时启动的 Java 进程，`8252-8255` 无监听进程 |

本次 `v0.5.3` 验证内容：

| 项目 | 状态 |
|---|---|
| Node.js | 已执行 `node -v`，当前为 `v22.22.3` |
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；四个后端模块测试均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；本版本未改后端业务代码，生成 jar 仍为 `0.5.2-SNAPSHOT` |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 已在 `frontend-vue` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| 真实 Gateway API 联调 | 使用临时端口 `8252-8255` 启动四个后端 jar，经 Gateway `8253` 完成健康检查、JWT 拦截、注册登录、任务创建、任务详情、状态流转、通知查询、未读数、单条已读和全部已读 |
| React 浏览器联调 | 使用 `VITE_API_BASE_URL=http://localhost:8253` 启动 React `5173`，已登录测试用户 `v053_20260527151910`，进入任务管理并通过页面创建任务，通知中心可看到通知和已读操作 |
| Vue 浏览器联调 | 使用 `VITE_API_BASE_URL=http://localhost:8253` 启动 Vue `5174`，已登录同一测试用户，进入任务管理并通过页面创建任务，通知中心可看到通知和已读操作 |
| 临时进程清理 | 验证结束后已停止本次临时启动的后端和前端进程，`8252-8255`、`5173`、`5174` 无监听进程 |

## 下一步

下一步进入 `v0.6 Nacos`，把 Gateway、用户服务、任务服务和通知服务接入 Nacos，验证服务注册发现和配置中心能力。
