# Java Microservice Learning Roadmap

本项目用于从 0 到 1 学习、验证并逐步搭建 Java 微服务体系。业务范围刻意保持很小：只实现用户管理和系统登录。技术范围会逐步扩展，从最简单可运行版本演进到包含网关、服务注册、消息、缓存、搜索、图数据库、可观测性、容器化和 K8s 的复杂系统。

## 项目目标

1. 每个阶段都是一个可运行版本，而不是半成品。
2. 每个阶段只新增一个核心能力，便于理解、验证和回滚。
3. 业务始终围绕用户注册、登录、认证、用户管理、审计和通知展开。
4. Windows 11 作为开发机，Java、Git、Maven、Node.js 使用本地环境，基础设施优先使用 Docker Desktop。
5. 所有写入和修改都限制在当前项目目录下。
6. 生成和修改的代码必须补充详细中文注释，便于后续学习、复盘和逐步理解微服务技术栈。
7. 代码提交、tag 和推送必须由用户手动执行，Codex 不自动执行 Git 提交相关操作。

## 当前环境记录

记录时间：2026-05-23，时区：Asia/Shanghai。

当前目标开发环境：

| 项目 | 当前状态 | 影响 |
|---|---|---|
| Java | JDK `17.0.19`，路径 `D:\software\jdk-17.0.19` | 作为 Spring Boot、Spring Cloud 和完整微服务路线的目标 Java 基线 |
| Maven | Apache Maven `3.9.16`，路径 `D:\software\apache-maven-3.9.16` | 作为本地 Maven 环境，项目仍建议引入 Maven Wrapper 保持构建入口一致 |
| Maven 本地仓库 | `D:\software\maven_download` | Maven 依赖统一下载到该目录，避免进入默认用户目录 |
| Node.js | 用户已在 Windows 11 配置 Node.js `22.x` | 作为 React、Vue 等前端项目的默认本地开发和构建运行时 |
| Docker | Docker Desktop 可用，Server `29.2.1` | 适合承载 MySQL、Nacos、Redis、MQ 等基础设施 |
| Git | 已初始化 Git 仓库，用户已提交 GitHub | 后续由用户手动提交、打 tag 和推送，Codex 只做只读检查和建议 |
| 本机占用端口 | `7991-8090`、`8146-8245` | 后续项目端口必须避开这两个范围，当前后端使用 `8091` |

环境验证提示：

| 检查项 | 期望结果 |
|---|---|
| `java -version` | 显示 `17.0.19` |
| `mvn -v` | 显示 Maven `3.9.16`，并且 Java version 为 `17.0.19` |
| `node -v` | 显示 `v22.x.x` |
| `npm -v` | 显示 Node.js 22 随附或用户配置的 npm 版本 |
| Maven 本地仓库 | Maven settings 或命令参数指向 `D:\software\maven_download` |

如果刚修改过系统环境变量，旧终端或旧 Codex 会话可能仍读取旧 PATH、旧 `JAVA_HOME` 或旧 Node.js 路径。遇到这种情况，重启终端或 Codex 会话后再验证。

目标建议：

| 项目 | 建议 |
|---|---|
| Java | 使用 JDK `17.0.19` 作为项目基线 |
| Maven | 使用 Maven `3.9.16`，同时引入 `mvnw` 和 `mvnw.cmd` 固化构建入口 |
| Maven 本地仓库 | 统一配置为 `D:\software\maven_download` |
| Node.js | 使用 Node.js `22.x` 作为默认前端运行时，React、Vue 前端均以该版本为基线 |
| Docker | 基础设施全部通过 `infra/docker-compose` 管理，使用 Docker Desktop 运行容器；每个服务或集群节点独立容器 |
| Git | 每个可运行 milestone 完成后，由用户手动提交和打 tag；Codex 不自动提交、不自动打 tag、不自动推送 |
| 端口 | 后端默认 `8091`，React `5173`，Vue `5174`，后续新增端口避开 `7991-8090` 和 `8146-8245` |

具体版本号在开发对应 milestone 前再按官方兼容矩阵确认，避免文档长期存在后版本过时。

## 目标技术范围

前端：

| 技术 | 用途 |
|---|---|
| React | 第一套管理端 UI |
| Ant Design | React 管理端默认组件库，用于布局、表单、表格、分页、反馈组件 |
| Vue | 第二套管理端 UI，用于对比生态和实现方式 |
| Element UI / Element Plus | Vue 管理端默认组件库；若使用 Vue 3，则使用 Element Plus |
| Node.js 22 | 前端本地开发、依赖安装、开发服务器和生产构建的默认运行时 |
| IndexedDB | 保存离线缓存、最近登录信息、轻量客户端状态 |
| HTTP/HTTPS | 常规 API 调用与安全访问 |
| WebSocket | 在线状态、系统消息、通知推送 |
| Nginx 集群 | 静态资源托管、反向代理、负载均衡、HTTPS 入口 |

后端与网关：

| 技术 | 用途 |
|---|---|
| Spring Boot | 后端服务基础框架 |
| Spring Cloud | 服务治理、网关、配置、链路扩展 |
| Spring Cloud Gateway | 统一入口、JWT 校验、路由、限流 |
| MyBatis Plus | 数据访问、分页、通用 CRUD |

中间件：

| 技术 | 用途 |
|---|---|
| Nacos 集群 | 服务注册、配置管理 |
| RabbitMQ 集群 | 业务消息、通知、审计事件 |
| Kafka 集群 | 日志流、事件流、与 RabbitMQ 对比学习 |
| Elasticsearch | 用户、审计、日志检索 |
| Redis Cluster | 缓存、限流、会话辅助、验证码 |
| Seata 3 节点集群 | 分布式事务验证 |

数据库与存储：

| 技术 | 用途 |
|---|---|
| MySQL 主从集群 | 业务主数据库、读写分离验证 |
| InfluxDB | 时序指标、登录趋势、接口耗时 |
| Neo4j | 用户、角色、组织、关系图谱 |
| MinIO | 头像、附件、对象存储 |

运维采集：

| 技术 | 用途 |
|---|---|
| Prometheus | 指标采集 |
| Grafana | 指标看板 |
| SkyWalking | 分布式链路追踪 |
| ELK | 日志采集、检索、分析 |

部署：

| 技术 | 用途 |
|---|---|
| Docker | 本地基础设施与服务容器化 |
| Kubernetes | 集群部署、服务编排、滚动发布 |
| Jenkins | CI/CD 构建、测试、镜像和部署流水线 |

## 推荐项目结构

```text
E:\Code\codex\java-demo
├─ README.md
├─ mvnw
├─ mvnw.cmd
├─ backend
│  ├─ pom.xml
│  ├─ common
│  ├─ auth-service
│  ├─ user-service
│  └─ gateway
├─ frontend-react
├─ frontend-vue
├─ infra
│  └─ docker-compose
├─ deploy
│  ├─ nginx
│  ├─ k8s
│  └─ jenkins
└─ docs
   ├─ ROADMAP.md
   ├─ DEVELOPMENT_RULES.md
   ├─ PROGRESS.md
   ├─ decisions
   └─ milestones
```

早期版本可以比该结构更简单。项目结构应随着 milestone 自然演进，不要为了远期复杂架构过早拆分。

## 已采纳架构决策

本项目已正式采纳以下路线，后续 milestone 执行时应以此为准。

| 决策 | 文件 | 执行含义 |
|---|---|---|
| 服务拆分策略 | `docs/decisions/0002-service-split.md` | 先单体闭环，再模块化，再微服务拆分 |
| 部署策略 | `docs/decisions/0003-deploy-strategy.md` | 先本地进程 + Docker 基础设施，再 Docker Compose，最后 Kubernetes 和 Jenkins |
| Docker 服务容器化策略 | `docs/decisions/0010-docker-service-containerization.md` | Nacos、Redis、MQ、Elasticsearch、Seata、Jenkins 等服务使用独立容器，集群节点也独立容器 |
| Git 提交策略 | `docs/decisions/0005-manual-git-commit.md` | 用户手动提交、打 tag 和推送，Codex 不自动执行 Git 写操作 |
| 前端 Node 环境 | `docs/decisions/0006-node-frontend-environment.md` | Node.js 22 作为 React、Vue 前端默认运行时 |
| 前端组件库 | `docs/decisions/0007-frontend-component-libraries.md` | React 使用 Ant Design，Vue 使用 Element UI 系列组件库 |
| 本地端口规划 | `docs/decisions/0008-local-port-allocation.md` | 后端使用 `8091`，后续端口避开 `7991-8090` 和 `8146-8245` |
| 前端语言策略 | `docs/decisions/0009-frontend-language-strategy.md` | React 管理端使用 TypeScript，Vue 管理端使用 JavaScript |

服务拆分阶段：

| 阶段 | 服务形态 | 说明 |
|---|---|---|
| `v0.1` - `v0.4` | 单体或简单多模块 | 先跑通登录、用户管理、前端访问 |
| `v0.5` | 引入 Gateway | 外部流量统一进入网关 |
| `v0.6` | 接入 Nacos | 服务注册发现和配置中心进入主线 |
| `v1.5` 前后 | 拆分更多业务服务 | 为 Seata、链路追踪和真实微服务边界提供验证场景 |

部署阶段：

| 阶段 | 部署形态 | 说明 |
|---|---|---|
| `v0.1` - `v0.4` | 后端/前端本地进程，中间件 Docker | 降低调试成本，优先保证业务闭环 |
| `v0.5` - `v1.7` | Docker Compose 管理基础设施和部分应用 | 逐步模拟集群依赖；每个服务和集群节点保持独立容器 |
| `v1.8` | Docker Compose 与 Kubernetes 双形态 | 学习服务编排和集群部署 |
| `v1.9` | Jenkins 流水线 | 自动构建、测试、镜像和部署 |

## 演进路线

| 版本 | 新增核心能力 | 主要技术 | 可运行验收 |
|---|---|---|---|
| `v0.1` | 最小登录系统 | Spring Boot、MyBatis Plus、MySQL、JWT、Springdoc OpenAPI | 注册、登录、获取当前用户、查看 Swagger UI |
| `v0.2` | 用户管理 CRUD | MyBatis Plus、分页、角色字段 | 增删改查、分页查询可用 |
| `v0.3` | React 管理端 | Node.js 22、React、TypeScript、Ant Design、HTTP、IndexedDB | 浏览器完成登录和用户列表查看 |
| `v0.4` | Vue 管理端 | Node.js 22、Vue、JavaScript、Element UI / Element Plus、HTTP | 第二套前端访问同一后端 API，业务行为与 React 端一致，项目结构体现 Vue 开发习惯 |
| `v0.5` | 网关入口 | Spring Cloud Gateway、JWT 校验 | 请求统一经过网关 |
| `v0.6` | 注册与配置中心 | Nacos | 服务注册、配置读取、配置刷新 |
| `v0.7` | 缓存与限流 | Redis、Redis Cluster 预研 | 登录缓存、验证码或限流生效 |
| `v0.8` | 实时通信 | WebSocket | 在线通知可推送到前端 |
| `v0.9` | 文件对象存储 | MinIO | 用户头像上传和访问 |
| `v1.0` | 异步消息 | RabbitMQ、Kafka 对照 | 登录审计或通知异步消费 |
| `v1.1` | 全文检索 | Elasticsearch | 用户和审计记录可搜索 |
| `v1.2` | 时序统计 | InfluxDB | 登录次数、接口耗时趋势入库 |
| `v1.3` | 图关系查询 | Neo4j | 用户、角色、组织关系可查询 |
| `v1.4` | MySQL 主从 | MySQL Replication、读写分离 | 写主库、读从库、故障演练 |
| `v1.5` | 分布式事务 | Seata 3 节点集群 | 跨服务操作可提交或回滚 |
| `v1.6` | Nginx 与 HTTPS | Nginx 集群、证书、本地域名 | 前端和网关统一 HTTPS 入口 |
| `v1.7` | 可观测性 | Prometheus、Grafana、SkyWalking、ELK | 指标、链路、日志可查询 |
| `v1.8` | 容器与 K8s | Docker Compose、Kubernetes | 本地 compose 与 K8s 部署可用 |
| `v1.9` | CI/CD | Jenkins | 自动构建、测试、镜像、部署 |

## 开发节奏

每个 milestone 遵循固定流程：

1. 读取 `docs/ROADMAP.md`、`docs/DEVELOPMENT_RULES.md`、`docs/PROGRESS.md` 和当前 milestone 文档。
2. 确认当前环境和上一版本是否可运行；涉及前端 milestone 时确认 `node -v` 显示 `v22.x.x`。
3. 实现本 milestone 的唯一核心新增能力。
4. 为新增或修改的代码补充详细中文注释，重点说明类职责、关键流程、配置含义、异常处理和测试意图。
5. 运行自动化测试、启动验证和接口验证。
6. 更新 `README.md`、`docs/PROGRESS.md`，必要时补充 `docs/decisions`。
7. 如果需要保存版本，Codex 只提示建议提交信息和 tag 名称，由用户手动提交、打 tag 和推送。

涉及 Nacos、Redis、RabbitMQ、Kafka、Elasticsearch、Seata、Jenkins 等基础设施时，优先新增 Docker Compose；镜像由 Docker Desktop 拉取并运行，每个服务或节点使用独立容器。

## 后续对 Codex 的推荐指令

继续开发下一个版本：

```text
请读取 docs/ROADMAP.md、docs/DEVELOPMENT_RULES.md、docs/PROGRESS.md 和当前 milestone 文档，然后实现下一个可运行版本。完成后运行验证，并更新进度文档。
```

开发指定版本：

```text
请按照 docs/milestones/v0.1-mvp-login.md 实现第一个可运行版本。只修改当前项目目录下的文件，完成后更新 docs/PROGRESS.md。
```

做技术实验：

```text
请在不影响主线的前提下，验证某个技术方案，并把结论写入 docs/decisions/。
```

## 分叉使用建议

分叉适合高风险或不确定方案验证，不适合作为主线进度管理工具。

适合分叉的场景：

| 场景 | 示例 |
|---|---|
| 技术选型比较 | RabbitMQ 与 Kafka 的学习对照 |
| 架构路线比较 | 单体先行与直接微服务拆分 |
| 高风险中间件验证 | Seata、SkyWalking、K8s 集群方案 |
| UI 方案探索 | React 管理端的信息架构或视觉方案 |

主线事实来源始终是项目内文档、代码和 Git 历史。
