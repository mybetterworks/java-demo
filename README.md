# Java Demo

这是一个从 0 到 1 学习 Java 微服务开发的练习项目。业务范围刻意保持很小：围绕用户注册、登录、认证和用户管理逐步演进；技术范围会按 `docs/ROADMAP.md` 从单体 MVP 扩展到网关、注册中心、缓存、消息、搜索、可观测性、容器化、Kubernetes 和 Jenkins。

## 当前版本

当前已完成 `v0.3 React Frontend`。

| 项目 | 内容 |
|---|---|
| 核心能力 | 注册、登录、JWT 签发、获取当前登录用户、用户管理 CRUD、React 管理端、IndexedDB 登录态恢复 |
| 后端 | Spring Boot `3.3.5` |
| ORM | MyBatis Plus `3.5.7` |
| 数据库 | MySQL `8.4` Docker 单节点 |
| 认证 | JWT |
| 接口文档 | Springdoc OpenAPI `2.6.0`、Swagger UI |
| 前端 | React `18`、Vite、TypeScript、Ant Design `5` |
| 前端缓存 | IndexedDB |
| Java | JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | Maven Wrapper，发行版 `3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |
| Node.js | Node.js `22.x` |

## 项目结构

```text
E:\Code\codex\java-demo
├─ backend
│  └─ app
│     ├─ src/main/java/com/example/javademo/app
│     ├─ src/main/resources
│     └─ src/test
├─ frontend-react
│  ├─ src
│  ├─ package.json
│  └─ vite.config.ts
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
| Database | `java_demo` |
| Username | `java_demo` |
| Password | `java_demo_pwd` |
| Root Password | `root_pwd` |

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

当前集成测试覆盖注册、重复注册、登录、JWT 查询当前用户、无 token 拦截、错误密码拦截、用户分页、详情、创建、更新、逻辑删除、修改密码和 OpenAPI JSON 生成。

## 启动后端

方式一：使用 Spring Boot Maven 插件。

```powershell
.\mvnw.cmd -pl backend/app spring-boot:run
```

方式二：运行已打包 jar。

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\app\target\java-demo-app-0.3.0-SNAPSHOT.jar
```

应用默认端口：

| 服务 | 地址 |
|---|---|
| 后端 API | `http://localhost:8091` |
| 健康检查 | `http://localhost:8091/api/health` |
| Swagger UI | `http://localhost:8091/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8091/v3/api-docs` |

## 启动 React 管理端

先确认 MySQL 和后端已启动，再启动前端：

```powershell
cd frontend-react
npm.cmd install
npm.cmd run dev
```

React 管理端默认地址：

```text
http://127.0.0.1:5173
```

开发环境中，Vite 会把 `/api` 和 `/v3/api-docs` 代理到 `http://localhost:8091`，因此浏览器访问前端时不会遇到跨域问题。

## 本地端口规划

当前本机 `7991-8090`、`8146-8245` 两段端口已被占用，项目当前和后续新增服务都必须避开这两个范围。

| 服务 | 当前/建议端口 | 说明 |
|---|---|---|
| Spring Boot 后端 | `8091` | 当前后端固定端口，Swagger UI 和 OpenAPI JSON 同端口 |
| React 开发服务器 | `5173` | `v0.3` React 管理端 |
| React Preview | `4173` | `npm.cmd run preview` |
| Vue 开发服务器 | `5174` | `v0.4` 建议端口，避开 React 端口 |
| Vue Preview | `4174` | `v0.4` 建议预览端口 |
| Spring Cloud Gateway | `8092` | `v0.5` 建议端口，避开占用范围 |
| 后续拆分服务 | `8093-8145` 或 `8246+` | 不使用 `8146-8245` |
| 本地 Nginx 非标准 HTTP/HTTPS | `8250` / `8251` | 如不使用 `80` / `443`，优先使用该范围 |
| MySQL Docker | `3306` | 当前 MySQL 单节点 |

前端生产构建：

```powershell
cd frontend-react
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

## API

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

注册请求：

```powershell
$body = @{ username = "alice"; password = "secret123"; nickname = "Alice" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8091/api/auth/register -ContentType "application/json" -Body $body
```

登录请求：

```powershell
$body = @{ username = "alice"; password = "secret123" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8091/api/auth/login -ContentType "application/json" -Body $body
$token = $login.data.accessToken
```

查询当前用户：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8091/api/users/me -Headers @{ Authorization = "Bearer $token" }
```

用户分页查询：

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8091/api/users?current=1&size=10&username=alice&status=1" -Headers @{ Authorization = "Bearer $token" }
```

创建用户：

```powershell
$body = @{ username = "bob"; password = "secret123"; nickname = "Bob"; status = 1; role = "USER" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8091/api/users -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

更新用户：

```powershell
$body = @{ nickname = "Bobby"; status = 1; role = "ADMIN" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri http://localhost:8091/api/users/2 -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

修改密码：

```powershell
$body = @{ password = "newSecret123" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri http://localhost:8091/api/users/2/password -Headers @{ Authorization = "Bearer $token" } -ContentType "application/json" -Body $body
```

逻辑删除用户：

```powershell
Invoke-RestMethod -Method Delete -Uri http://localhost:8091/api/users/2 -Headers @{ Authorization = "Bearer $token" }
```

## Swagger UI

启动后端后访问：

```text
http://localhost:8091/swagger-ui.html
```

Swagger UI 中可以直接查看并调试当前 v0.3 的所有接口。访问 `/api/users/me`、`/api/users` 这类需要登录的接口时，先调用登录接口拿到 `accessToken`，再点击页面右上角 `Authorize`，在 `bearerAuth` 中填入登录返回的 token。

OpenAPI JSON 地址：

```text
http://localhost:8091/v3/api-docs
```

## 配置项

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `JAVA_DEMO_DB_USERNAME` | `java_demo` | MySQL 用户名 |
| `JAVA_DEMO_DB_PASSWORD` | `java_demo_pwd` | MySQL 密码 |
| `JAVA_DEMO_JWT_SECRET` | `java-demo-v0-1-local-secret-change-me-32chars` | JWT 签名密钥，至少 32 字节 |
| `JAVA_DEMO_JWT_EXPIRATION_SECONDS` | `7200` | JWT 有效期，单位秒 |

## 数据库升级

`v0.2` 在 `sys_user` 表上新增了 `role`、`deleted`、`last_login_at` 字段。新库会通过 `schema.sql` 直接创建完整表结构；如果本地已经存在 `v0.1` 表，应用启动时会通过轻量迁移器自动检查并补齐缺失字段。

当前仍未引入 Flyway 或 Liquibase，数据库迁移先保持最小实现；后续里程碑如果迁移脚本变复杂，再单独引入专业迁移工具。

## 前端验证记录

本次 `v0.3` 验证内容：

| 验证项 | 结果 |
|---|---|
| `npm.cmd install` | 通过，生成 `frontend-react/package-lock.json` |
| `npm.cmd run build` | 通过 |
| `npm.cmd audit --audit-level=high` | 通过，无 high/critical 漏洞；仍存在 Vite/esbuild 相关 moderate 提示 |
| 前端页面访问 | `http://127.0.0.1:5173` 返回 `200` |
| Vite 代理 | 通过前端端口访问 `/api/health`、登录、用户分页均返回 `200` |
| 浏览器登录 | 通过 |
| 刷新后恢复登录态 | 通过，IndexedDB 会话可恢复 |
| 用户列表 | 通过 |
| 新增用户 | 通过 |
| 编辑用户 | 通过 |

构建提示：Ant Design 进入主包后，Vite 提示单个 chunk 超过 500KB。这不影响 `v0.3` 可运行版本；后续页面增多后可以再通过路由懒加载或 `manualChunks` 优化。

## 下一步

下一个 milestone 是 `v0.4 Vue Frontend`，目标是新增 Vue 管理端，复用当前后端登录和用户管理接口，并与 React 管理端做实现方式对比。
