# Development Rules

本文档是本项目后续开发的执行约束。目标是让每一次增量开发都可运行、可验证、可回退，避免微服务学习项目变成“中间件堆叠展示柜”。

## 写入范围

1. 只允许写入和修改当前项目目录：`E:\Code\codex\java-demo`。
2. 不得修改用户主目录、系统目录、其他仓库或 Docker Desktop 配置文件。
3. 如需生成临时文件，优先放在当前项目内的 `tmp`、`target`、`build` 或工具默认构建目录。
4. 不得删除用户已有文件，除非用户明确要求。

## 版本原则

1. 每个 milestone 都必须是一个可运行版本。
2. 每个 milestone 只新增一个核心能力。
3. 新增能力必须围绕登录、认证、用户管理、审计、通知或运维可观测展开。
4. 不允许为了未来复杂架构破坏当前已运行功能。
5. 不允许一次性引入多个大型中间件，除非 milestone 明确要求。

## 服务拆分规则

已采纳 `docs/decisions/0002-service-split.md`。

1. `v0.1` 到 `v0.4` 优先使用单体或简单多模块，先完成登录、用户管理和前端访问闭环。
2. `v0.5` 引入 Spring Cloud Gateway 后，外部流量统一从网关进入。
3. `v0.6` 接入 Nacos 后，再逐步把服务发现和配置中心纳入主线。
4. `v1.5` 前后为了验证 Seata 和链路追踪，可以拆分更多业务服务。
5. 不为了“看起来像微服务”提前制造分布式复杂度。
6. 每次服务拆分后都必须保留一个可运行版本。

## 部署路线规则

已采纳 `docs/decisions/0003-deploy-strategy.md`。

1. `v0.1` 到 `v0.4` 使用本地进程运行后端和前端，中间件通过 Docker Desktop 运行。
2. `v0.5` 到 `v1.7` 使用 Docker Compose 管理基础设施和部分应用服务。
3. `v1.8` 同时提供 Docker Compose 和 Kubernetes 两套部署方式。
4. `v1.9` 引入 Jenkins 流水线。
5. 先单机可用，再集群演练。
6. 先 Docker Compose，再 Kubernetes。
7. Jenkins 最后引入，避免早期流水线掩盖基础问题。

## 开发前检查

每次开始实现前应检查：

1. 当前目录结构。
2. `docs/PROGRESS.md` 中记录的当前阶段。
3. 当前 milestone 文档中的目标、范围和验收标准。
4. 本地 Java、Maven Wrapper、Docker 是否满足本阶段要求；涉及前端 milestone 时还必须检查 Node.js 22 是否可用。
5. 如果项目已初始化 Git，检查工作区状态，避免覆盖用户改动。

## 项目构建规则

1. Java 目标基线为 JDK `17.0.19`，本机路径为 `D:\software\jdk-17.0.19`。
2. 本地 Maven 版本为 Apache Maven `3.9.16`，路径为 `D:\software\apache-maven-3.9.16`。
3. Maven 依赖下载和本地仓库路径统一使用 `D:\software\maven_download`。
4. 后端优先使用 Maven Wrapper：`.\mvnw.cmd`。
5. 不把用户全局 Maven PATH 作为唯一构建前提。
6. `mvn -v` 或 `.\mvnw.cmd -v` 必须显示 Java version 为 `17.0.19`，否则需要先修正 `JAVA_HOME` 或当前终端环境。
7. 如果引入 `.mvn/maven.config` 或 Maven `settings.xml` 示例，必须确保本地仓库指向 `D:\software\maven_download`。
8. 前端默认运行环境为 Node.js `22.x`，React、Vue、前端开发服务器和前端生产构建均以该版本为基线。
9. 进入前端 milestone 前必须执行 `node -v`，期望结果为 `v22.x.x`；如果旧终端未刷新 PATH，应重启终端或 Codex 会话后再验证。
10. 前端使用项目内 package manager 配置，不依赖全局脚手架生成不可追踪内容。
11. 前端默认优先使用项目内 `npm scripts`；如果后续改用 `pnpm`、`yarn` 或其他工具，必须补充决策记录并提交对应 lockfile。
12. React 管理端统一使用 TypeScript 开发，`v0.3` 已按 Vite + React + TypeScript 实现，后续 React 扩展继续保持 `.ts` / `.tsx` 和类型检查。
13. Vue 管理端统一使用 JavaScript 开发，`v0.4` 创建项目时不得选择 TypeScript 模板，除非后续新增决策记录明确变更。

## 前端 UI 规则

已采纳 `docs/decisions/0007-frontend-component-libraries.md`。

1. React 管理端默认使用 Ant Design 组件库。
2. Vue 管理端默认使用 Element UI 系列组件库；如果使用 Vue 3，则使用 Element Plus。
3. React 管理端代码使用 TypeScript，Vue 管理端代码使用 JavaScript，便于学习时对比两种前端语言体验。
4. 登录页、首页、用户列表、用户编辑表单应优先使用对应组件库的布局、表单、表格、分页、消息提示和弹窗能力。
5. 不在同一套前端中混用多个同类大型 UI 组件库，除非新增决策记录说明原因。
6. 前端组件封装、表单校验、表格分页、接口错误处理和登录态恢复逻辑需要补充中文注释，便于学习对比。

## 前端对比学习规则

1. React 管理端和 Vue 管理端用于学习两套前端技术栈，因此业务页面、操作路径和处理逻辑应尽量保持一致。
2. React 与 Vue 的结构不要求文件一一镜像；React 可以保持 `components` 组织页面，Vue 应优先使用更常见的 `views`、`layouts`、`composables`、`api`、`storage` 分层。
3. React 与 Vue 可以使用不同组件库、样式、配色和本地存储底层实现，但不要因为视觉差异改变登录、退出、查询、重置、新增、编辑、删除和分页的业务语义。
4. 新增或重构 Vue 页面时，应优先检查 React 端同类页面的业务行为，再使用 Vue 项目习惯组织代码：页面级 SFC 放入 `views`，应用外壳放入 `layouts`，可复用状态逻辑放入 `composables`。
5. 如果后续 React 端新增用户相关页面能力，Vue 端在对应 milestone 中应复刻相同业务闭环；如果 Vue 端先新增能力，也需要记录是否同步 React 端。
6. 对比学习说明应记录在对应 README、milestone 或 `docs/PROGRESS.md` 中，避免后续维护者只看到代码差异而不知道设计意图。

## Docker 规则

1. 所有基础设施 compose 文件放在 `infra/docker-compose` 下。
2. 单机版 compose 与集群版 compose 分开维护。
3. 默认优先提供轻量单机版本，确认可用后再扩展为集群。
4. Docker volume 命名要带项目名前缀，避免污染其他项目。
5. 对外端口必须记录在对应 README 或 milestone 文档中。

## 端口规则

已采纳 `docs/decisions/0008-local-port-allocation.md`。

1. 本地环境中 `7991-8090`、`8146-8245` 已被占用，当前项目和后续新增服务不得使用这两个范围内的端口。
2. Spring Boot 后端默认端口为 `8091`，配置在 `backend/app/src/main/resources/application.yml`。
3. React 管理端开发端口为 `5173`，preview 端口为 `4173`。
4. `v0.4` Vue 管理端使用 `5174`，preview 端口使用 `4174`。
5. `v0.5` Spring Cloud Gateway 建议使用 `8092`。
6. 后续拆分业务服务优先使用 `8093-8145` 或 `8246+`。
7. 如果 Nginx 本地学习阶段不使用 `80` / `443`，非标准 HTTP/HTTPS 建议使用 `8250` / `8251`。
8. 每次新增 Docker Compose、前端 dev server、后端服务、网关或运维组件端口时，都必须先检查是否落入占用范围，并同步更新 README 和 `docs/PROGRESS.md`。

## API 规则

1. 后端接口统一使用 `/api` 前缀。
2. 登录认证接口集中在 `/api/auth`。
3. 用户管理接口集中在 `/api/users`。
4. 返回结构统一，至少包含 `code`、`message`、`data`。
5. 错误码、异常处理、参数校验要随业务复杂度逐步完善。
6. 后端接口应保持 OpenAPI JSON 和 Swagger UI 可访问，便于学习、调试和验收。
7. 需要 JWT 的接口应在 Swagger UI 中标注 Bearer 认证方案。

## 代码注释规则

1. 生成和修改的代码必须补充详细中文注释，项目以学习和验证为目标，注释应帮助理解“为什么这样做”。
2. Java 代码优先使用中文 JavaDoc 说明类职责、公开方法用途、重要参数、返回值和异常语义。
3. 关键业务流程必须补充中文注释，包括注册、登录、JWT 签发与解析、认证拦截、异常转换、数据库初始化和测试验收链路。
4. 配置文件、SQL、Docker Compose 等非 Java 代码也应补充中文注释，说明端口、账号、环境变量、数据卷、初始化策略等含义。
5. 注释应详细但不堆砌，不为简单 getter/setter 或显而易见的单行赋值重复解释。
6. 修改已有代码时，应同步更新附近注释，避免注释与实现不一致。
7. Swagger/OpenAPI 注解不能完全替代代码注释；接口注解用于文档展示，代码注释用于解释实现和学习思路。

## 安全规则

1. 密码必须哈希存储，不允许明文入库。
2. JWT secret 不应硬编码在业务代码中。
3. 开发环境可以使用 `.env.example` 说明配置，但不要提交真实密钥。
4. 管理接口必须逐步接入认证和权限校验。
5. HTTPS 在 Nginx milestone 中引入，早期本地 HTTP 可接受。

## 测试和验收

每个 milestone 完成时至少执行：

1. 构建验证。
2. 单元测试或最小接口测试。
3. 服务启动验证。
4. 当前 milestone 文档中的验收清单。
5. 前端 milestone 还必须执行前端启动或构建验证，并确认命令运行在 Node.js `22.x` 环境下。
6. 关键命令记录到 `README.md` 或 `docs/PROGRESS.md`。

验收失败时：

1. 优先修复。
2. 如果受环境限制无法验证，必须在 `docs/PROGRESS.md` 中记录原因。
3. 不得把未验证版本标记为完成。

## 文档更新规则

每个 milestone 完成后必须更新：

| 文件 | 更新内容 |
|---|---|
| `README.md` | 当前版本启动方式、端口、验收命令 |
| `docs/PROGRESS.md` | 当前阶段、完成项、下一步、已知问题 |
| 当前 milestone 文档 | 如实际实现与计划不同，补充实现记录 |
| `docs/decisions/*.md` | 技术路线发生选择时新增或更新决策记录 |

文档更新时应同步记录重要注释规范变化，确保后续开发继续遵守中文注释要求。

## Git 规则

如果项目已初始化 Git：

1. 项目已经提交到 GitHub，后续所有代码提交、tag 和推送必须由用户手动执行。
2. Codex 不允许自动执行 `git commit`、`git tag`、`git push`、`git merge`、`git rebase` 等会改变 Git 历史或远端状态的操作。
3. Codex 可以执行 `git status`、`git diff`、`git log` 等只读检查，用于确认工作区状态和避免覆盖用户修改。
4. 每个 milestone 完成后，Codex 可以提示建议提交信息，例如 `feat: complete v0.2 user crud`，但不得代替用户提交。
5. 每个可运行版本可以建议 tag，例如 `v0.2-user-crud`，但不得代替用户打 tag 或推送 tag。
6. 不使用 `git reset --hard`、`git checkout --` 等破坏性命令，除非用户明确要求且风险已说明。
7. 不覆盖用户未提交修改。

## 分叉规则

分叉用于实验，不用于替代主线文档。

适合分叉：

1. 技术选型验证。
2. 高风险中间件接入。
3. 架构拆分方式探索。
4. UI 方案比较。

分叉实验完成后，应把结论写回 `docs/decisions`，主线再决定是否采纳。

## 完成定义

一个 milestone 只有同时满足以下条件才算完成：

1. 新增能力已实现。
2. 当前版本可启动。
3. 关键接口或页面可验证。
4. 必要测试已运行或明确记录无法运行原因。
5. 文档已更新。
6. 新增和修改代码已补充必要的详细中文注释。
7. 未留下明显破坏下一阶段的临时设计。

## 推荐工作提示词

继续下一个 milestone：

```text
请读取 docs/ROADMAP.md、docs/DEVELOPMENT_RULES.md、docs/PROGRESS.md 和当前 milestone 文档，然后实现下一个可运行版本。完成后运行验证，并更新文档。
```

只做规划，不写代码：

```text
请基于 docs/ROADMAP.md 细化下一个 milestone 的开发计划，不修改业务代码。
```

做实验：

```text
请在不影响主线的前提下验证一个技术方案，并将结论写入 docs/decisions/。
```
