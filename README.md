# Java Demo

这是一个从 0 到 1 学习 Java 微服务开发的练习项目。业务范围刻意保持很小：围绕用户注册、登录、认证和用户管理逐步演进；技术范围会按 `docs/ROADMAP.md` 从单体 MVP 扩展到网关、注册中心、缓存、消息、搜索、可观测性、容器化、Kubernetes 和 Jenkins。

## 当前版本

当前已完成 `v0.7 Redis Cache And Rate Limit`。Gateway、`java-demo-app`、`task-service` 和 `notification-service` 继续使用 Nacos 服务注册发现与配置中心；`task-service -> java-demo-app` 的负责人用户校验主路径继续走 Dubbo RPC，`task-service -> notification-service` 的通知创建链路继续保留 OpenFeign。本版本在不改变前端操作路径的前提下新增 Redis 单节点基础设施，并把用户摘要、任务查询、通知未读数、登录失败计数、拼图验证码 challenge、一次性 captchaToken 和接口限流迁移到 Redis TTL。

当前直连与经 Gateway 访问健康检查时，`java-demo-app` 会返回 `redisEnabled`、`cacheEnabled`、`rateLimitEnabled`、`userValidationProviderMode=dubbo`、`dubboRegistryGroup=JAVA_DEMO_DUBBO` 和 `dubboProtocolPort=20881`；`task-service` 会额外返回 `serviceCallMode=mixed-dubbo-feign`、`userValidationMode=dubbo`、`notificationCallMode=openfeign`、`taskCacheTtlSeconds`、`userServiceName=java-demo-app` 和 `notificationServiceName=notification-service`；`notification-service` 会返回 `notificationUnreadCacheTtlSeconds`，便于联调时快速确认 Redis、缓存、限流和混合调用主路径。

下一步进入 `v0.8 WebSocket`，准备在当前任务和通知业务闭环上增加实时通知能力。

| 版本 | 规划内容 | 状态 |
|---|---|---|
| `v0.5.3` | React/Vue 任务管理和通知中心 | 已完成 |
| `v0.5.4` | 登录失败风险判断和滑块验证码 | 已完成 |
| `v0.5.5` | 固定背景图随机拼图滑块验证码 | 已完成 |
| `v0.6` | Nacos 服务注册发现和配置中心 | 已完成 |
| `v0.6.1` | `task-service` 使用 OpenFeign 调用用户服务和通知服务 | 已完成 |
| `v0.6.2` | `task-service -> java-demo-app` 用户校验链路改为 Dubbo RPC | 已完成 |
| `v0.7` | Redis 缓存、未读数缓存与接口限流 | 已完成 |
| `v0.8` | WebSocket 实时通知 | 下一步 |

补充说明：当前实际版本已更新为 `v0.7 Redis Cache And Rate Limit`，下一步进入 `v0.8 WebSocket`。

| 项目 | 内容 |
|---|---|
| 核心能力 | 注册、登录、登录失败风险判断、固定背景图随机拼图验证码、JWT 签发、网关 JWT 校验、获取当前登录用户、用户管理 CRUD、任务创建/分配/状态流转、通知创建/查询/未读数/已读标记、Redis 缓存、Redis TTL 登录风险状态、接口限流、后端运行日志、React 任务/通知管理端、Vue 任务/通知管理端 |
| 后端 | Spring Boot `3.3.5` |
| 网关 | Spring Cloud Gateway `2023.0.3`，默认端口 `8092` |
| 任务服务 | `task-service`，默认端口 `8093` |
| 通知服务 | `notification-service`，默认端口 `8094` |
| ORM | MyBatis Plus `3.5.7` |
| 数据库 | MySQL `8.4` Docker 单节点 |
| 认证 | JWT |
| 日志 | SLF4J + Logback，控制台日志、`logs/*.log` 文件日志、`requestId`、可配置级别 |
| 登录安全能力 | `v0.5.5` 已实现登录拼图滑块验证码：5 分钟内登录失败 3 次后要求账号密码 + 固定背景图随机拼图验证码；后端保存真实答案并校验坐标、耗时、基础轨迹和一次性状态 |
| 当前基础设施能力 | `v0.6` Nacos 服务注册发现和配置中心；`v0.7` Redis 单节点缓存与限流状态 |
| 当前服务调用能力 | `v0.6.2` 混合同步调用：`task-service -> java-demo-app` 使用 Dubbo RPC，`task-service -> notification-service` 使用 OpenFeign |
| 下一步演进重点 | `v0.8` WebSocket 实时通知 |
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
│  ├─ rpc-api
│  │  └─ src/main/java/com/example/javademo/rpc
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

## 启动 Nacos

```powershell
docker compose -f infra\docker-compose\nacos\docker-compose.yml up -d
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\infra\docker-compose\nacos\import-configs.ps1
```

检查容器：

```powershell
docker ps --filter "name=java-demo-nacos-1"
```

Nacos 控制台：

```text
http://localhost:8848/nacos
```

`infra/docker-compose/nacos/configs/*.yml` 会被直接发布到 Nacos。当前 `spring-alibaba-nacos-config 2023.0.3.2` 在 Windows 上解析 YAML 时会把配置字符串按平台默认编码重新转回字节，因此这些待发布 YAML 需要保持 ASCII-only；中文说明请写在 README、milestone 或 `docs/PROGRESS.md` 中，导入脚本也会主动拒绝非 ASCII 内容。

如果需要停止 Nacos：

```powershell
docker compose -f infra\docker-compose\nacos\docker-compose.yml stop
```

## 启动 Redis

```powershell
docker compose -f infra\docker-compose\redis\docker-compose.yml up -d
```

检查容器：

```powershell
docker ps --filter "name=java-demo-redis-1"
docker exec java-demo-redis-1 redis-cli ping
```

默认连接信息：

| 项目 | 值 |
|---|---|
| Container | `java-demo-redis-1` |
| Image | `redis:7.2.5-alpine` |
| Host | `localhost` |
| Port | `6379` |
| Volume | `java-demo-redis-data` |
| Network | `java-demo-redis-net` |

如果本机 `6379` 被占用或被 Windows 端口策略拒绝，可以临时覆盖宿主机端口；后端服务同时设置 `JAVA_DEMO_REDIS_PORT` 指向该端口即可：

```powershell
$env:JAVA_DEMO_REDIS_HOST_PORT='16380'
docker compose -f infra\docker-compose\redis\docker-compose.yml up -d
$env:JAVA_DEMO_REDIS_PORT='16380'
```

如果需要停止 Redis：

```powershell
docker compose -f infra\docker-compose\redis\docker-compose.yml stop
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

当前 `v0.7` 实际验证使用 `D:\software\apache-maven-3.9.16\bin\mvn.cmd`，并统一复用项目既定的本地 Maven 仓库 `D:\software\maven_download` 完成，结果如下：

| 验证项 | 结果 |
|---|---|
| 后端 `test` | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；六个 Maven 模块测试均成功 |
| 后端 `-DskipTests package` | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -DskipTests package`，通过；生成四个 `0.7.0-SNAPSHOT` 可执行 jar 和一个 `java-demo-rpc-api-0.7.0-SNAPSHOT.jar` |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 已在 `frontend-vue` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| Redis 容器 | 已使用 `java-demo-redis-1` 完成验证；本机 `6379` 被 Windows 拒绝绑定，本次通过 `JAVA_DEMO_REDIS_HOST_PORT=16380` 覆盖宿主机端口，容器内 `redis-cli ping` 返回 `PONG` |
| 真实运行态联调 | 已通过 Docker MySQL + Docker Nacos + Docker Redis + 四个后端 jar 完成真实 Gateway 联调，并确认 Redis 缓存、Redis TTL 验证码状态、四类限流、Dubbo 用户校验、Feign 通知创建、Nacos 配置读取、拼图验证码回归和 requestId 日志串联均可用 |

当前集成测试代码覆盖注册、重复注册、登录、登录失败风险判断、拼图验证码触发、错误位置、过短耗时、异常轨迹、图片差分求解、一次性 token、验证码通过后登录、JWT 查询当前用户、无 token 拦截、错误密码拦截、用户分页、详情、创建、更新、逻辑删除、修改密码、任务创建/状态流转/逻辑删除、通知创建/未读数/已读标记和 OpenAPI JSON 生成；网关测试覆盖公开路径放行、验证码公开路径放行、无 token 拦截、有效 token 放行、无效 token 拦截以及任务/通知健康检查白名单。

## 启动后端

方式一：使用 Spring Boot Maven 插件。

```powershell
.\mvnw.cmd -pl backend/app spring-boot:run
```

方式二：运行已打包 jar。

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\app\target\java-demo-app-0.6.2-SNAPSHOT.jar
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
D:\software\jdk-17.0.19\bin\java.exe -jar backend\gateway\target\java-demo-gateway-0.6.2-SNAPSHOT.jar
```

Gateway 默认端口：

| 服务 | 地址 |
|---|---|
| 外部 API 统一入口 | `http://localhost:8092` |
| 健康检查 | `http://localhost:8092/api/health` |
| Swagger UI | `http://localhost:8092/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8092/v3/api-docs` |

Gateway 当前默认使用 `lb://java-demo-app`、`lb://task-service` 和 `lb://notification-service` 进行服务发现路由；如需临时回退、排查或绕过注册中心，也可以通过环境变量 `JAVA_DEMO_BACKEND_URI`、`JAVA_DEMO_TASK_SERVICE_URI`、`JAVA_DEMO_NOTIFICATION_SERVICE_URI` 覆盖为固定 URI。

## 启动任务服务和通知服务

先确认 MySQL 中已存在 `java_demo_task` 和 `java_demo_notification`，再启动两个新服务。

```powershell
.\mvnw.cmd -pl backend/task-service spring-boot:run
.\mvnw.cmd -pl backend/notification-service spring-boot:run
```

或运行已打包 jar：

```powershell
D:\software\jdk-17.0.19\bin\java.exe -jar backend\task-service\target\java-demo-task-service-0.6.2-SNAPSHOT.jar
D:\software\jdk-17.0.19\bin\java.exe -jar backend\notification-service\target\java-demo-notification-service-0.6.2-SNAPSHOT.jar
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
http://127.0.0.1:5320
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
http://127.0.0.1:5321
```

Vue 端使用 Vue `3`、JavaScript 和 Element Plus，不启用 TypeScript 模板。开发环境中，Vite 会把 `/api` 和 `/v3/api-docs` 代理到 Gateway `http://localhost:8092`，端口 `5321` 和 preview 端口 `4174` 均避开了本机占用范围和 Windows 当前保留端口段。

## 本地端口规划

当前本机 `7991-8090`、`8146-8245` 两段端口已被占用；Windows 当前还保留了 `5112-5311` 段，旧 React/Vue 开发端口 `5173/5174` 会因系统保留报 `EACCES`。项目当前和后续新增服务都必须避开这些范围。

| 服务 | 当前/建议端口 | 说明 |
|---|---|---|
| Spring Boot 后端 | `8091` | 后端固定端口，v0.5 后主要用于开发调试直连 |
| Spring Cloud Gateway | `8092` | v0.5 外部 API 统一入口，前端默认代理到该端口 |
| task-service | `8093` | v0.5.1 任务服务 |
| notification-service | `8094` | v0.5.1 通知服务 |
| React 开发服务器 | `5320` | `v0.3` React 管理端，替代被 Windows 保留的旧端口 `5173` |
| React Preview | `4173` | `npm.cmd run preview` |
| Vue 开发服务器 | `5321` | `v0.4` Vue 管理端，替代被 Windows 保留的旧端口 `5174` |
| Vue Preview | `4174` | `v0.4` Vue 生产构建预览 |
| 后续拆分服务 | `8095-8145` 或 `8246+` | 不使用 `8146-8245` |
| 本地 Nginx 非标准 HTTP/HTTPS | `8250` / `8251` | 如不使用 `80` / `443`，优先使用该范围 |
| MySQL Docker | `3306` | 当前 MySQL 单节点 |
| Redis Docker | `6379` | `v0.7` Redis 单节点；如本机端口不可用，可用 `JAVA_DEMO_REDIS_HOST_PORT` 临时覆盖宿主机端口 |

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
| Vue 任务管理 | 使用 JavaScript + Element Plus，已延续 `views`、`composables`、`api`、`storage` 分层，业务路径与 React 保持一致 |
| Vue 通知中心 | 使用 JavaScript + Element Plus，已支持与 React 一致的通知列表、未读数和已读操作 |
| 双端一致性 | React 和 Vue 菜单名称、页面布局、筛选项、表格字段、操作按钮、空数据与错误提示尽量保持一致 |
| 本地查询缓存 | React 使用 IndexedDB 保存任务/通知最近查询条件；Vue 使用 localStorage 保存同类查询条件 |
| 代码注释 | 新增前端代码已补充中文注释，说明 API 封装、页面状态、表单校验、表格分页、本地缓存和错误处理 |

`v0.5.4` 已完成的登录风险流程：

| 能力 | 说明 |
|---|---|
| 登录失败计数 | 同一登录主体 5 分钟内登录失败达到 3 次后进入风险验证状态；未达到阈值时仍按普通账号密码错误处理 |
| 学习型滑块验证码 | 后续登录必须先获取 challenge、完成滑块校验并拿到一次性验证码 token，再携带账号密码和验证码 token 登录 |
| 统计维度 | 使用 `username + clientIp`，兼顾账号级风险和共享 IP 场景 |
| 前端联动 | React 和 Vue 登录页均已支持验证码触发、展示、校验、错误提示和登录重试，业务流程保持一致 |
| 状态存储 | `v0.5.4` 使用单机内存保存失败计数、challenge 和一次性 token，`v0.7 Redis` 再迁移到 Redis TTL |
| 安全日志 | 记录失败计数、验证码触发和校验结果，但禁止打印密码、完整 JWT、Authorization header、验证码答案或验证码 token |

`v0.5.5` 已完成的登录验证码安全增强：

| 能力 | 说明 |
|---|---|
| 固定背景图 | 后端内置固定验证码背景图，生成 challenge 时基于该图片生成缺口背景和拼图块 |
| 随机拼图位置 | 每次 challenge 随机生成缺口位置，前端不能再通过固定公式计算答案 |
| 服务端答案 | 正确 `targetX` 只保存在服务端，challenge 响应只返回背景图、拼图块、尺寸和提示 |
| 基础轨迹校验 | verify 阶段除校验滑动坐标外，还校验耗时、轨迹点数量和基础移动方向 |
| 双端联动 | React 和 Vue 登录页均已升级为拼图滑块交互，并保持提示语义和接口字段一致 |
| 安全边界 | 本版本提升对普通自动化脚本的抵御能力，但仍不承诺抵御专业识图、打码平台或人工绕过 |

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
| `POST` | `/api/auth/captcha/slider` | 生成拼图滑块验证码 challenge | 否 |
| `POST` | `/api/auth/captcha/slider/verify` | 校验拼图滑块验证码，返回一次性验证码 token | 否 |
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

`POST /api/auth/login` 在 `v0.5.4` 起支持风险验证语义：5 分钟内登录失败 3 次后，如果请求未携带有效验证码 token，后端返回业务码 `4601`；验证码错误、过期、轨迹异常或 token 无效时返回业务码 `4602`。为了保持现有统一响应结构兼容，当前仍使用数字业务码。`v0.5.5` 继续复用验证码接口路径，但 challenge 响应已经从简单滑块参数升级为背景缺口图、拼图块图和基础尺寸信息，真实缺口坐标只保存在服务端。

验证码必需响应示例：

```json
{
  "code": 4601,
  "message": "登录失败次数过多，请完成滑块验证",
  "data": {
    "captchaRequired": true
  }
}
```

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
| `JAVA_DEMO_NACOS_SERVER_ADDR` | `127.0.0.1:8848` | Nacos 服务地址 |
| `JAVA_DEMO_NACOS_NAMESPACE` | `public` | Nacos 命名空间 |
| `JAVA_DEMO_NACOS_DISCOVERY_GROUP` | `DEFAULT_GROUP` | Nacos 服务注册分组 |
| `JAVA_DEMO_NACOS_CONFIG_GROUP` | `DEFAULT_GROUP` | Nacos 配置分组 |
| `JAVA_DEMO_NACOS_DISCOVERY_ENABLED` | `true` | 是否启用 Nacos 服务发现 |
| `JAVA_DEMO_NACOS_REGISTER_ENABLED` | `true` | 是否向 Nacos 注册当前服务 |
| `JAVA_DEMO_NACOS_CONFIG_ENABLED` | `true` | 是否启用 Nacos 配置中心 |
| `JAVA_DEMO_BACKEND_URI` | `lb://java-demo-app` | Gateway 转发到用户/认证服务的默认路由 |
| `JAVA_DEMO_TASK_SERVICE_URI` | `lb://task-service` | Gateway 转发到任务服务的默认路由 |
| `JAVA_DEMO_NOTIFICATION_SERVICE_URI` | `lb://notification-service` | Gateway 转发到通知服务的默认路由 |
| `JAVA_DEMO_USER_SERVICE_NAME` | `java-demo-app` | task-service 记录用户服务逻辑目标名，并用于 Dubbo 用户校验链路的运行摘要 |
| `JAVA_DEMO_NOTIFICATION_SERVICE_NAME` | `notification-service` | task-service 通过 OpenFeign 调用通知服务时使用的 Nacos 服务名 |
| `JAVA_DEMO_USER_VALIDATION_MODE` | `dubbo` | task-service 健康检查中展示的用户校验主路径 |
| `JAVA_DEMO_NOTIFICATION_MODE` | `openfeign` | task-service 健康检查中展示的通知调用主路径 |
| `JAVA_DEMO_USER_PROVIDER_MODE` | `dubbo` | java-demo-app 健康检查中展示的用户 Dubbo provider 模式 |
| `JAVA_DEMO_DUBBO_APPLICATION_NAME` | 当前 `spring.application.name` | Dubbo 应用名，provider 与 consumer 默认沿用各自 Spring 应用名 |
| `JAVA_DEMO_DUBBO_NACOS_GROUP` | `JAVA_DEMO_DUBBO` | Dubbo 在 Nacos 中使用的独立注册分组，与 Spring Cloud 发现分组隔离 |
| `JAVA_DEMO_DUBBO_USER_PROVIDER_PORT` | `20881` | java-demo-app 暴露用户 Dubbo 服务时使用的协议端口 |
| `JAVA_DEMO_DUBBO_USER_TIMEOUT` | `3000` | task-service 默认 Dubbo 用户校验超时，单位毫秒 |
| `JAVA_DEMO_DUBBO_USER_RETRIES` | `0` | task-service 默认 Dubbo 用户校验重试次数 |
| `JAVA_DEMO_DUBBO_CONSUMER_CHECK` | `false` | task-service 启动时是否强制校验 Dubbo provider 可达 |
| `JAVA_DEMO_FEIGN_CONNECT_TIMEOUT` | `3000` | task-service 调用 notification-service 时使用的 Feign 连接超时，单位毫秒 |
| `JAVA_DEMO_FEIGN_READ_TIMEOUT` | `5000` | task-service 调用 notification-service 时使用的 Feign 读取超时，单位毫秒 |
| `JAVA_DEMO_FEIGN_LOGGER_LEVEL` | `basic` | task-service 调用 notification-service 时使用的 Feign 日志级别 |
| `JAVA_DEMO_REDIS_HOST` | `127.0.0.1` | 后端连接 Redis 的主机 |
| `JAVA_DEMO_REDIS_PORT` | `6379` | 后端连接 Redis 的端口；如果 compose 用 `JAVA_DEMO_REDIS_HOST_PORT=16380` 覆盖宿主机端口，这里也要设为 `16380` |
| `JAVA_DEMO_REDIS_DATABASE` | `0` | Redis database index |
| `JAVA_DEMO_REDIS_PASSWORD` | 空 | Redis 密码；默认单节点学习环境不设置密码，日志不会打印该值 |
| `JAVA_DEMO_REDIS_TIMEOUT` | `2s` | Redis 命令超时时间 |
| `JAVA_DEMO_REDIS_ENABLED` | `true` | 是否启用 Redis 主存储；关闭后缓存和限流会走进程内存降级 |
| `JAVA_DEMO_REDIS_KEY_PREFIX` | `java-demo:v0_7` | Redis key 前缀，用于隔离本项目 milestone 数据 |
| `JAVA_DEMO_CACHE_ENABLED` | `true` | 是否启用业务缓存 |
| `JAVA_DEMO_USER_CACHE_TTL_SECONDS` | `300` | 用户摘要缓存 TTL |
| `JAVA_DEMO_TASK_CACHE_TTL_SECONDS` | `60` | 任务列表/详情缓存 TTL |
| `JAVA_DEMO_NOTIFICATION_UNREAD_CACHE_TTL_SECONDS` | `60` | 通知未读数缓存 TTL |
| `JAVA_DEMO_RATE_LIMIT_ENABLED` | `true` | 是否启用接口限流 |
| `JAVA_DEMO_RATE_LIMIT_WINDOW_SECONDS` | `60` | 限流窗口秒数 |
| `JAVA_DEMO_LOGIN_RATE_LIMIT` | `20` | 登录接口单窗口限制 |
| `JAVA_DEMO_USER_QUERY_RATE_LIMIT` | `120` | 用户查询接口单窗口限制 |
| `JAVA_DEMO_TASK_QUERY_RATE_LIMIT` | `120` | 任务查询接口单窗口限制 |
| `JAVA_DEMO_NOTIFICATION_QUERY_RATE_LIMIT` | `120` | 通知查询接口单窗口限制 |
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
| `java-demo-app` | `logs/java-demo-app.log` | 服务启动摘要、请求入口/完成、注册、登录、JWT 解析、用户管理、验证码状态、用户缓存、登录/用户查询限流、异常处理 |
| `task-service` | `logs/task-service.log` | 服务启动摘要、请求入口/完成、任务创建、查询、状态流转、任务缓存、任务查询限流、服务间调用、异常处理 |
| `notification-service` | `logs/notification-service.log` | 服务启动摘要、请求入口/完成、通知创建、查询、未读数缓存、通知查询限流、已读标记、异常处理 |

日志格式包含服务名、线程和 `requestId`。外部请求可以传入 `X-Request-Id`；未传时业务服务会自动生成。`v0.6.2` 以后，`task-service` 调用 `java-demo-app` 时会通过 Dubbo attachment 透传 `requestId`，调用 `notification-service` 时继续通过 Feign 请求头透传 `X-Request-Id`，便于在多个日志文件中串起同一次任务/通知链路。`v0.7` 启动摘要会额外打印 `redisEnabled`、Redis 地址、`cacheEnabled`、缓存 TTL 和 `rateLimitEnabled`，限流触发会以 WARN 级别输出；缓存命中/未命中默认是 DEBUG，调试时可临时提高业务包日志级别。

本项目日志规则要求：不打印明文密码、密码哈希、完整 JWT、Authorization header、数据库密码、验证码答案、验证码 token 或真实密钥。调试时可以临时开启业务包 DEBUG：

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

本次 `v0.5.5` 验证内容：

| 项目 | 状态 |
|---|---|
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；四个后端模块测试均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd package`，通过；已生成四个 `0.5.5-SNAPSHOT` 可执行 jar |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 已在 `frontend-vue` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| 前端浏览器检查 | 已使用当前 `5320/5321` dev server 检查 React/Vue 登录页，确认两端均显示 `v0.5.5` 登录页和拼图安全说明 |
| 真实 Gateway 登录风控联调 | Docker Desktop 已启动，`java-demo-mysql` 容器为 `healthy`；本次使用真实 MySQL，临时启动 `java-demo-app:8252` 和 Gateway `8253`，并设置 `JAVA_DEMO_BACKEND_URI=http://localhost:8252`；经 Gateway 完成注册、前两次错误密码、第三次触发 `4601/captchaRequired=true`、正确密码无验证码仍返回 `4601`、错误拼图位置返回 `4602`、图片差分求解正确位置后 verify 成功、一次性 token 复用被拒绝、携带新 token 登录成功、状态清理后再次普通登录成功 |
| Gateway 白名单 | 已验证 `/api/auth/captcha/slider` 和 `/api/auth/captcha/slider/verify` 可通过 Gateway 公开访问，无需 JWT |
| 临时进程清理 | 验证结束后已停止临时启动的 `8252` 和 `8253` Java 进程，端口无监听 |

本次 `v0.6.1` 验证内容：

| 项目 | 状态 |
|---|---|
| Nacos 容器 | 已执行 `docker compose -f infra\docker-compose\nacos\docker-compose.yml up -d`；`java-demo-nacos-1` 当前为 `healthy`，控制台可经 `http://localhost:8848/nacos` 访问 |
| Nacos 配置导入 | 已执行 `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\infra\docker-compose\nacos\import-configs.ps1`，五份配置均已导入到 `DEFAULT_GROUP` |
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；五个 Maven 模块均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -DskipTests package`，通过；已生成四个 `0.6.1-SNAPSHOT` 可执行 jar |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 标准 `npm.cmd run build` 因 `frontend-vue/dist/assets/index-DYq5bwgT.js` 被环境锁定返回 `EPERM`；改用 `npm.cmd run build -- --outDir dist-v061-verify --emptyOutDir false` 后通过，临时目录已清理 |
| 四服务注册发现 | 使用临时端口 `8252-8255` 启动四个后端服务后，Nacos `service/list` 可见 `java-demo-app`、`java-demo-gateway`、`task-service`、`notification-service` 四个服务各 1 个实例 |
| 配置中心与 Feign 主路径 | 直连和经 Gateway 访问健康检查均返回 `configSource=nacos`、`configLabel=v0.6-default`；`task-service` 额外返回 `serviceCallMode=openfeign`、`userServiceName=java-demo-app`、`notificationServiceName=notification-service` |
| `v0.5.5` 登录安全回归 | 经 Gateway 完成错误密码三次触发 `4601/captchaRequired=true`、验证码公开接口放行、错误滑块位置返回 `4602`、图片差分求解后 verify 成功、一次性 token 复用被拒绝、验证码登录成功和风险状态清理后的普通登录成功 |
| 真实 Gateway 业务联调 | 经 Gateway 完成注册、登录、`/api/users/me`、任务创建、我的任务查询、任务状态从 `TODO` 到 `IN_PROGRESS`、通知分页与未读数验证；本次联调用户 `v061_20260604104905`，任务 ID `10`，未读数 `2` |
| requestId 透传 | 已在 `logs/v061-verify-20260604104816-task.log`、`logs/v061-verify-20260604104816-app.log`、`logs/v061-verify-20260604104816-notification.log` 中确认同一 `requestId=v061-create-190ec9dd0604402d95966573a43dcd38` 贯穿任务创建链路 |
| 回归说明 | 本版本只改造后端内部同步调用方式，不新增用户可见能力；React/Vue 无需代码修改，但两端构建已完成回归 |
| Windows 编码约束 | 已继续确认 `spring-alibaba-nacos-config 2023.0.3.2` 在 Windows 上存在平台默认编码限制，因此 `infra/docker-compose/nacos/configs/*.yml` 仍需保持 ASCII-only |
| 临时进程清理 | 验证结束后已停止本次临时启动的 `8252-8255` Java 进程，端口无监听 |

本次 `v0.6.2` 验证内容：

| 验证项 | 结果 |
|---|---|
| Nacos 配置导入 | 已执行 `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\infra\docker-compose\nacos\import-configs.ps1`，确认最新 Dubbo + Feign 混合配置已导入 |
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；五个 Maven 模块均成功 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -DskipTests package`，通过；已生成四个 `0.6.2-SNAPSHOT` 可执行 jar 和一个 `rpc-api` 契约 jar |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 标准 `npm.cmd run build` 因 `frontend-vue/dist/assets/index-DYq5bwgT.js` 被环境锁定返回 `EPERM`；改用 `npm.cmd run build -- --outDir dist-v0_6_2_check --emptyOutDir false` 后通过 |
| 四服务注册发现 | 使用真实后端 jar 联调时，Nacos `service/list` 可见 `java-demo-app`、`java-demo-gateway`、`task-service`、`notification-service` 各 1 个实例 |
| Dubbo Provider/Consumer 摘要 | 直连与经 Gateway 访问健康检查时，`java-demo-app` 返回 `userValidationProviderMode=dubbo`、`dubboRegistryGroup=JAVA_DEMO_DUBBO`，`task-service` 返回 `serviceCallMode=mixed-dubbo-feign`、`userValidationMode=dubbo`、`notificationCallMode=openfeign` |
| `v0.5.5` 登录安全回归 | 经 Gateway 验证错误密码第三次触发 `4601`，并成功访问 `/api/auth/captcha/slider` 完成拼图 challenge |
| 真实 Gateway 业务联调 | 经 Gateway 完成注册、登录、`/api/users/me`、任务创建、通知查询；本次联调 `requestId=v062-smoke-20260605-212340`、任务 ID `12`、创建人 `31`、负责人 `32` |
| 混合调用日志链路 | 已在 `logs/v062-20260605-212340-task-service.log`、`logs/v062-20260605-212340-java-demo-app.log`、`logs/v062-20260605-212340-notification-service.log` 中确认 `Calling user service via Dubbo`、`Received Dubbo user validation request` 和 `Calling notification service via OpenFeign` |
| 前端联动判断 | 本版本只改造后端内部同步调用方式，不新增用户可见能力，因此 React/Vue 无需代码修改；双端构建已完成回归 |
| Windows 编码约束 | 已继续确认 Nacos 待发布 YAML 必须保持 ASCII-only，Dubbo 注册分组仅放在配置文档中说明，不写入中文注释 |
| 临时进程清理 | 真实联调结束后已停止本次临时启动的 Java 进程，未保留额外监听端口 |

本次 `v0.7` 验证内容：

| 验证项 | 结果 |
|---|---|
| Redis 容器 | 已新增 `infra/docker-compose/redis/docker-compose.yml`；默认宿主端口 `6379`，本机验证因 Windows 拒绝绑定 `6379`，临时使用 `JAVA_DEMO_REDIS_HOST_PORT=16380` 启动，`docker exec java-demo-redis-1 redis-cli ping` 返回 `PONG` |
| Nacos 配置导入 | 已执行 `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\infra\docker-compose\nacos\import-configs.ps1`，确认 `java-demo-common.yml` 中的 Redis、缓存和限流配置已导入 |
| Maven test | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd test`，通过；包含新增 `RateLimitIntegrationTest`，测试环境默认关闭 Redis 并验证内存降级限流 |
| Maven package | 已执行 `D:\software\apache-maven-3.9.16\bin\mvn.cmd -DskipTests package`，通过；生成 `0.7.0-SNAPSHOT` 后端 jar 和 `rpc-api` 契约 jar |
| React 构建 | 已在 `frontend-react` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning |
| Vue 构建 | 已在 `frontend-vue` 执行 `npm.cmd run build`，通过；保留既有 Vite chunk size warning 和 VueUse 注释提示 |
| 真实 Gateway 健康检查 | 使用临时端口 `8252-8255` 启动四个 `0.7.0-SNAPSHOT` jar，并用 `JAVA_DEMO_V07_CHECK` / `JAVA_DEMO_DUBBO_V07_CHECK` 隔离 Nacos 与 Dubbo；直连和经 Gateway 的 health 均返回 `redisEnabled=true`、`cacheEnabled=true`、`rateLimitEnabled=true` 和 `configLabel=v0.7-redis-real-check` |
| Redis key 验证 | 经 Gateway 联调后确认存在 `java-demo:v0_7:user:summary:34`、`java-demo:v0_7:task:list:version`、`java-demo:v0_7:task:my:v1:34:1:10:ALL`、`java-demo:v0_7:task:detail:13`、`java-demo:v0_7:notification:unread:34`、`java-demo:v0_7:captcha:token:*`、`java-demo:v0_7:login:fail:*` 和 `java-demo:v0_7:rate:*` |
| `v0.5.5` 拼图验证码回归 | 经 Gateway 创建 challenge，响应不包含 `targetX`；使用原始背景图与缺口背景图做像素差分求解 `sliderX=100`，verify 成功并写入 Redis captcha token TTL key |
| `v0.6` Nacos 注册发现回归 | Nacos `JAVA_DEMO_V07_CHECK` 分组可见 `java-demo-app:8252`、`task-service:8254`、`notification-service:8255` 健康实例；Gateway `lb://` 路由可访问三类 health |
| `v0.6.2` Dubbo + Feign 回归 | 任务创建前主动删除用户摘要缓存，确认 `task-service` 日志出现 `Calling user service via Dubbo`，`java-demo-app` 日志出现 `Received Dubbo user validation request`，随后 `task-service` 日志出现 `Calling notification service via OpenFeign` |
| 限流验证 | 临时把用户/任务/通知查询 limit 设为 `2`，第三次分别返回 `429`；登录接口用独立 `X-Forwarded-For` 验证第三次错误登录返回 `429`，四类限流均生成 Redis `rate:*` key |
| 临时进程清理 | 真实联调结束后已停止本次临时启动的 `8252-8255` Java 进程；Docker MySQL、Nacos、Redis 容器保留运行，便于继续手动复查 |

## 下一步

下一步进入 `v0.8 WebSocket`，准备基于当前任务与通知业务补齐实时通知链路。基础设施服务继续按当前规则使用 Docker Desktop 独立容器运行，并且后续回归验证仍需确认 `v0.5.5` 拼图验证码链路、`v0.6` 的 Nacos 注册发现能力、`v0.6.2` 的 Dubbo + Feign 混合主路径以及 `v0.7` Redis 缓存/限流能力保持可用。

| 重点 | 说明 |
|---|---|
| WebSocket 能力 | 为通知中心增加实时推送入口，优先承接任务通知创建后的用户可见提醒 |
| 调用边界 | 保持 `task-service -> java-demo-app` 继续走 Dubbo，`task-service -> notification-service` 继续走 OpenFeign，不在 `v0.8` 顺手改动同步调用主路径 |
| 请求链路 | 继续保留 `requestId`、JWT 语义、Dubbo 附件透传、Feign 头透传、Redis key 前缀和关键调用日志可观察 |
| 回归验证 | 保持注册登录、拼图验证码、Redis 缓存/限流、用户管理、任务通知链路、Gateway 白名单、Nacos 配置加载和 React/Vue 构建可用 |
