# Java Demo React Admin

`frontend-react` 是 `v0.3 React Frontend` 的管理端项目，用于通过浏览器验证后端登录、JWT、当前用户和用户管理 CRUD。

## 技术栈

| 类别 | 技术 |
|---|---|
| 运行环境 | Node.js `22.x` |
| 构建工具 | Vite |
| 前端框架 | React + TypeScript |
| UI 组件库 | Ant Design |
| 本地缓存 | IndexedDB |
| API 通信 | Fetch + Vite Proxy，经 Spring Cloud Gateway 转发 |

## 启动

先启动 MySQL、后端和 Gateway，再启动前端：

```powershell
cd frontend-react
npm.cmd install
npm.cmd run dev
```

浏览器访问：

```text
http://localhost:5173
```

开发环境中 `/api` 会由 Vite 代理到：

```text
http://localhost:8092
```

端口说明：本机 `7991-8090`、`8146-8245` 已被占用，React 开发端口 `5173`、Gateway 端口 `8092` 和后端调试端口 `8091` 均避开了这些范围。v0.5 开始，React 端的外部 API 请求统一先进入 Gateway，再由 Gateway 转发到后端。

## 构建

```powershell
cd frontend-react
npm.cmd run build
```

构建产物位于 `frontend-react/dist`，该目录不提交到 Git。

## IndexedDB

当前只保存两类学习用数据：

| 数据 | 说明 |
|---|---|
| `auth_session` | 当前 token、用户名、登录时间和当前用户信息 |
| `recent_users_query` | 最近一次用户列表查询条件 |

真实生产系统对 token 存储要更谨慎，本项目 v0.3 先以学习登录态恢复为主。
