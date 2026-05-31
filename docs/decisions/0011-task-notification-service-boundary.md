# 0011 Task And Notification Service Boundary

## 状态

已采纳。

## 日期

2026-05-27，Asia/Shanghai。

## 背景

`v0.5 Gateway JWT` 已经把外部请求统一收敛到 Spring Cloud Gateway，但业务后端仍主要集中在 `java-demo-app`。如果直接进入 Nacos、Redis、MQ、WebSocket、Seata 和链路追踪，系统缺少足够自然的跨服务业务场景，很多技术会变成“为了接入而接入”。

因此需要在 `v0.6 Nacos` 前增加一个轻量业务拆分版本，先形成真实但不复杂的微服务交互闭环。

## 决策

在 `v0.5.1` 新增两个业务微服务：

| 服务 | 职责 | MVP 范围 |
|---|---|---|
| `task-service` | 任务、任务分配、任务状态流转 | 创建任务、查询我的任务、分页查询、修改状态、逻辑删除 |
| `notification-service` | 站内信、未读数、已读状态 | 创建通知、查询我的通知、查询未读数量、单条已读、全部已读 |

当前已有 `java-demo-app` 继续承载用户、认证、JWT 和用户管理职责。`gateway` 继续作为统一入口和基础 JWT 校验层。

## 服务关系

```text
frontend-react / frontend-vue
  -> gateway
    -> java-demo-app
    -> task-service
    -> notification-service
```

本阶段采用静态地址 REST 调用：

| 调用方 | 被调用方 | 业务目的 |
|---|---|---|
| `task-service` | `java-demo-app` | 校验任务负责人用户是否存在 |
| `task-service` | `notification-service` | 任务创建、分配或状态变化后创建通知 |
| `java-demo-app` | `notification-service` | 用户注册或管理员创建用户后创建欢迎通知，可作为可选能力 |

## 数据边界

本阶段复用本地 MySQL 单实例，但按服务拆分 database：

| 服务 | database |
|---|---|
| `java-demo-app` | `java_demo` |
| `task-service` | `java_demo_task` |
| `notification-service` | `java_demo_notification` |

这样既避免过早引入多个 MySQL 容器，也能让后续主从、读写分离、分布式事务和数据同步实验具备清晰服务边界。

## 端口规划

遵守 `docs/decisions/0008-local-port-allocation.md`。

| 服务 | 端口 |
|---|---|
| `java-demo-app` | `8091` |
| `gateway` | `8092` |
| `task-service` | `8093` |
| `notification-service` | `8094` |

## 后续演进

| 后续版本 | 演进方向 |
|---|---|
| `v0.5.2` | 为用户、任务、通知三个业务服务建立控制台日志、文件日志和日志级别配置基线 |
| `v0.5.3` | React 和 Vue 都新增任务管理与通知中心，承接任务/通知服务的用户可见能力 |
| `v0.6` | 三个业务服务和 Gateway 接入 Nacos，静态地址改为服务发现 |
| `v0.6.1` | `task-service` 通过 OpenFeign 调用用户服务和通知服务，替代手写 REST |
| `v0.6.2` | `task-service -> java-demo-app` 用户校验链路改为 Dubbo RPC，通知链路继续保留 Feign |
| `v0.7` | 用 Redis 缓存用户校验结果、任务列表和通知未读数 |
| `v0.8` | 通知服务通过 WebSocket 实时推送任务和通知变化 |
| `v1.0` | 任务事件通过 MQ 异步驱动通知创建 |
| `v1.5` | 基于用户、任务、通知验证跨服务事务边界 |
| `v1.7` | 观察 Gateway、任务服务、通知服务之间的完整调用链 |

## 影响

1. `v0.5.1` 成为 `v0.5 Gateway` 和 `v0.6 Nacos` 之间的业务拆分版本。
2. 后续技术 milestone 应优先围绕任务、通知和用户三个业务域设计实验场景。
3. 不再等到 `v1.5` 才拆出更多业务服务；`v1.5` 重点转为在已有服务边界上验证 Seata。
4. 本阶段仍不引入 Nacos、Redis、MQ 等新中间件，避免一次性复杂化。
