# 0008 Local Port Allocation

## 状态

已采纳。

## 背景

本地 Windows 11 开发环境中，以下端口范围已被其他服务占用：

| 起始端口 | 结束端口 |
|---|---|
| `7991` | `8090` |
| `8146` | `8245` |

项目早期后端曾使用 `8080`，当前 `application.yml` 曾出现 `8082`，两者都落在 `7991-8090` 占用范围内。为了避免后续每个 milestone 反复遇到端口冲突，需要固定新的本地端口规划。

## 决策

1. 当前 Spring Boot 后端默认端口改为 `8091`。
2. React 管理端继续使用 `5173`，React preview 使用 `4173`。
3. `v0.4` Vue 管理端使用 `5174`，Vue preview 使用 `4174`。
4. `v0.5` Spring Cloud Gateway 建议使用 `8092`。
5. `v0.5.1` `task-service` 使用 `8093`。
6. `v0.5.1` `notification-service` 使用 `8094`。
7. 后续拆分出的本地业务服务优先使用 `8095-8145` 或 `8246+`。
8. 如果 Nginx 本地学习阶段不使用 `80` / `443`，非标准 HTTP/HTTPS 建议使用 `8250` / `8251`。
9. 后续新增端口必须避开 `7991-8090` 和 `8146-8245`。

## 当前端口表

| 服务 | 端口 | 说明 |
|---|---|---|
| Spring Boot 后端 | `8091` | 当前后端 API、Swagger UI、OpenAPI JSON |
| React 开发服务器 | `5173` | `frontend-react` 本地开发 |
| React Preview | `4173` | `frontend-react` 生产构建预览 |
| Vue 开发服务器 | `5174` | `frontend-vue` 本地开发 |
| Vue Preview | `4174` | `frontend-vue` 生产构建预览 |
| Spring Cloud Gateway | `8092` | `v0.5` 外部 API 统一入口 |
| task-service | `8093` | `v0.5.1` 任务服务 |
| notification-service | `8094` | `v0.5.1` 通知服务 |
| MySQL Docker | `3306` | 当前 MySQL 单节点 |

## 验证记录

2026-05-23 已完成本地运行验证：

| 验证项 | 结果 |
|---|---|
| `http://127.0.0.1:8091/api/health` | `200` |
| `http://127.0.0.1:8091/v3/api-docs` | `200`，OpenAPI 标题为 `Java Demo API` |
| `http://127.0.0.1:8091/swagger-ui.html` | `200` |
| `http://127.0.0.1:5173` | `200` |
| `http://127.0.0.1:5173/api/health` | `200`，确认 Vite 代理已转发到后端 `8091` |
| `http://127.0.0.1:5173/v3/api-docs` | `200`，确认 OpenAPI 代理可用 |

2026-05-26 已完成 Vue 端口验证：

| 验证项 | 结果 |
|---|---|
| `http://127.0.0.1:5174` | `200` |
| `http://127.0.0.1:5174/api/health` | `200`，确认 Vue Vite 代理已转发到后端 `8091` |
| `http://127.0.0.1:5174/api/auth/login` | `200`，确认 Vue 端口可完成登录链路 |

## 影响

1. 后端地址从 `http://localhost:8080` 调整为 `http://localhost:8091`。
2. React Vite 代理目标从 `http://localhost:8080` 调整为 `http://localhost:8091`。
3. Vue Vite 代理目标统一指向 `http://localhost:8091`。
4. `v0.5` 后前端默认代理目标应指向 Gateway `http://localhost:8092`，后端 `8091` 主要保留为开发调试直连入口。
5. `v0.5.1` 新增任务和通知服务后，Gateway 需要把 `/api/tasks/**` 路由到 `8093`，把 `/api/notifications/**` 路由到 `8094`。
6. README、PROGRESS 和 milestone 文档中的当前接口示例必须同步改为当前规划端口。
7. 历史验证记录如果提到旧端口，应明确其为历史记录；当前启动和后续开发以本决策为准。
