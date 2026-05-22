# Java Demo

这是一个从 0 到 1 学习 Java 微服务开发的练习项目。业务范围刻意保持很小：围绕用户注册、登录、认证和用户管理逐步演进；技术范围会按 `docs/ROADMAP.md` 从单体 MVP 扩展到网关、注册中心、缓存、消息、搜索、可观测性、容器化、Kubernetes 和 Jenkins。

## 当前版本

当前已完成 `v0.1 MVP Login`。

| 项目 | 内容 |
|---|---|
| 核心能力 | 注册、登录、JWT 签发、获取当前登录用户 |
| 后端 | Spring Boot `3.3.5` |
| ORM | MyBatis Plus `3.5.7` |
| 数据库 | MySQL `8.4` Docker 单节点 |
| 认证 | JWT |
| 接口文档 | Springdoc OpenAPI `2.6.0`、Swagger UI |
| Java | JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` |
| Maven | Maven Wrapper，发行版 `3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |

## 项目结构

```text
E:\Code\codex\java-demo
├─ backend
│  └─ app
│     ├─ src/main/java/com/example/javademo/app
│     ├─ src/main/resources
│     └─ src/test
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

当前集成测试覆盖注册、重复注册、登录、JWT 查询当前用户、无 token 拦截、错误密码拦截和 OpenAPI JSON 生成。

## 启动后端

方式一：使用 Spring Boot Maven 插件。

```powershell
.\mvnw.cmd -pl backend/app spring-boot:run
```

方式二：运行已打包 jar。

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\app\target\java-demo-app-0.1.0-SNAPSHOT.jar
```

应用默认端口：

| 服务 | 地址 |
|---|---|
| 后端 API | `http://localhost:8080` |
| 健康检查 | `http://localhost:8080/api/health` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

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

注册请求：

```powershell
$body = @{ username = "alice"; password = "secret123"; nickname = "Alice" } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/register -ContentType "application/json" -Body $body
```

登录请求：

```powershell
$body = @{ username = "alice"; password = "secret123" } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/auth/login -ContentType "application/json" -Body $body
$token = $login.data.accessToken
```

查询当前用户：

```powershell
Invoke-RestMethod -Method Get -Uri http://localhost:8080/api/users/me -Headers @{ Authorization = "Bearer $token" }
```

## Swagger UI

启动后端后访问：

```text
http://localhost:8080/swagger-ui.html
```

Swagger UI 中可以直接查看并调试当前 v0.1 的所有接口。访问 `/api/users/me` 这类需要登录的接口时，先调用登录接口拿到 `accessToken`，再点击页面右上角 `Authorize`，在 `bearerAuth` 中填入登录返回的 token。

OpenAPI JSON 地址：

```text
http://localhost:8080/v3/api-docs
```

## 配置项

| 环境变量 | 默认值 | 说明 |
|---|---|---|
| `JAVA_DEMO_DB_USERNAME` | `java_demo` | MySQL 用户名 |
| `JAVA_DEMO_DB_PASSWORD` | `java_demo_pwd` | MySQL 密码 |
| `JAVA_DEMO_JWT_SECRET` | `java-demo-v0-1-local-secret-change-me-32chars` | JWT 签名密钥，至少 32 字节 |
| `JAVA_DEMO_JWT_EXPIRATION_SECONDS` | `7200` | JWT 有效期，单位秒 |

## 下一步

下一个 milestone 是 `v0.2 User CRUD`，目标是在当前登录闭环基础上增加用户管理 CRUD、分页查询和更完整的用户字段。
