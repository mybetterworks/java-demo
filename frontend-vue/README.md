# Java Demo Vue Admin

这是 `v0.4 Vue Frontend` 的管理端实现，用来和 `v0.3 React Frontend` 做学习对比。它复用同一套 Spring Boot 后端 API，只新增 Vue 前端能力，不修改后端业务接口。

## 技术栈

| 项目 | 说明 |
|---|---|
| 运行环境 | Node.js `22.x` |
| 前端框架 | Vue `3` |
| 开发语言 | JavaScript，不启用 TypeScript 模板 |
| UI 组件库 | Element Plus |
| 开发服务器 | Vite |
| 开发端口 | `5174` |
| Preview 端口 | `4174` |
| 后端代理 | `/api` 与 `/v3/api-docs` 转发到 `http://localhost:8091` |

## 启动方式

先确认 MySQL 和后端已启动：

```powershell
docker compose -f infra\docker-compose\mysql\docker-compose.yml up -d
.\mvnw.cmd -pl backend/app spring-boot:run
```

再启动 Vue 管理端：

```powershell
cd frontend-vue
npm.cmd install
npm.cmd run dev
```

浏览器访问：

```text
http://127.0.0.1:5174
```

## 构建验证

```powershell
cd frontend-vue
npm.cmd run build
```

## 已实现页面

| 页面/能力 | 说明 |
|---|---|
| 登录页 | 调用 `/api/auth/login` 获取 JWT，并把登录结果交给根组件保存 |
| 首页 | 展示当前登录用户，并通过 `/api/users/me` 刷新登录态 |
| 用户列表 | 调用 `/api/users` 分页查询用户，并恢复最近查询条件 |
| 新增用户 | 调用 `POST /api/users` |
| 编辑用户 | 调用 `PUT /api/users/{id}` |
| 逻辑删除 | 调用 `DELETE /api/users/{id}` |

## Vue 项目结构

Vue 端保持和 React 端相同的业务功能与操作路径，但目录组织采用更常见的 Vue 写法：

| 目录/文件 | 职责 | 说明 |
|---|---|---|
| `src/App.vue` | 应用入口接线 | 负责启动恢复、登录态判断和当前 view 渲染 |
| `src/layouts/AppLayout.vue` | 管理端外壳 | 负责侧边菜单、顶部用户信息和内容插槽 |
| `src/views` | 页面级组件 | 放置登录页、首页和用户管理页 |
| `src/composables` | 组合式业务逻辑 | 放置 `useAuthSession`、`useUserManagement` 等状态逻辑 |
| `src/api` | 后端请求封装 | 统一处理 API 调用和错误 |
| `src/storage` | 本地持久化 | 封装 localStorage，保存登录态和最近查询条件 |
| `src/styles.css` | 全局样式 | 保持当前视觉风格和响应式布局 |

## 与 React 端的对比

| 对比项 | React 管理端 | Vue 管理端 |
|---|---|---|
| 开发语言 | TypeScript | JavaScript |
| UI 组件库 | Ant Design | Element Plus |
| 状态组织 | React hooks + 组件状态 | Vue Composition API + composables |
| 本地登录态 | IndexedDB | localStorage |
| 最近查询条件 | IndexedDB | localStorage |
| 目录组织 | `components` 承载页面组件 | `views`、`layouts`、`composables` 分层 |
| 目的 | 练习类型约束和大型组件生态 | 对比 Vue 单文件组件、模板语法和组合式逻辑 |

Vue 端刻意不引入 Pinia、Vue Router 或 TypeScript，避免在本阶段把“第二套前端实现”扩展成多项新技术同时学习。当前使用与 React 端相同的轻量 view state 完成首页和用户管理切换；后续如果页面数量增多，再单独通过 milestone 引入路由或状态管理。
