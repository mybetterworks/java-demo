# 0014 Service Invocation Evolution

## 状态

已采纳。

## 日期

2026-05-31，Asia/Shanghai。

## 背景

`v0.5.1` 已经让项目具备用户、任务、通知三个业务服务，并通过 REST 完成最小跨服务调用。`v0.6` 将引入 Nacos 服务注册发现，此时服务间调用可以从静态地址迁移到服务名。

后续需要学习 OpenFeign 和 Dubbo，但不适合一次性把所有服务间调用都改成同一种技术。原因是不同调用方式适合不同边界：

| 调用方式 | 适合场景 |
|---|---|
| REST / Gateway | 外部 API、前端访问、Swagger/OpenAPI 调试 |
| OpenFeign | Spring Cloud 体系内的普通同步 HTTP 服务调用 |
| Dubbo RPC | 稳定、强类型、高内聚的后端内部同步调用 |
| MQ | 通知、审计、事件等不应阻塞主流程的副作用链路 |

## 决策

采用分阶段、混合式服务调用演进路线。

| 版本 | 调用策略 |
|---|---|
| `v0.5.1` | 先使用静态 REST 调用形成最小业务闭环 |
| `v0.6` | 接入 Nacos，服务地址从静态地址转为服务名 |
| `v0.6.1` | 使用 OpenFeign 改造 `task-service -> java-demo-app` 和 `task-service -> notification-service` |
| `v0.6.2` | 只把 `task-service -> java-demo-app` 用户校验链路改为 Dubbo RPC |
| `v1.0` | 把任务通知等副作用链路从同步 Feign 演进为 MQ 异步事件 |

## 调用边界

| 调用链路 | 推荐方式 | 说明 |
|---|---|---|
| 前端 -> Gateway | REST | 对外统一 HTTP 入口 |
| Gateway -> 后端服务 | REST 路由 + Nacos 服务发现 | 统一鉴权、路由和前端访问入口 |
| `task-service -> java-demo-app` 用户校验 | `v0.6.1` Feign，`v0.6.2` Dubbo | 适合先学习声明式 HTTP，再学习 RPC |
| `task-service -> notification-service` 通知创建 | `v0.6.1` Feign，`v1.0` MQ | 通知是副作用链路，长期不应阻塞任务主流程 |
| `java-demo-app -> notification-service` 欢迎通知 | 可选 Feign，长期 MQ | 如果实现欢迎通知，也应最终事件化 |

## 原则

1. 不把所有服务间调用一次性全部改为 Feign。
2. 不把所有服务间调用一次性全部改为 Dubbo。
3. 同一条主业务链路不要长期同时混用 Feign 和 Dubbo，除非是明确的对照实验或可配置回退。
4. Dubbo 只用于后端内部强类型 RPC，不替代前端/Gateway 的 REST API。
5. 通知、审计、事件等副作用链路长期优先 MQ 异步化，而不是 Dubbo 同步化。
6. 每次调用方式变化都必须保留可运行版本，并补充日志、注释、测试和文档。

## 影响

1. `v0.6.1` 成为 OpenFeign 专项学习版本，不和 Dubbo 混在一起。
2. `v0.6.2` 成为 Dubbo RPC 专项学习版本，只选择用户校验一条链路，避免过度改造。
3. `v1.0 MQ` 的定位更清晰：把任务通知链路从同步调用解耦为异步事件。
4. 后续面试讲解时，可以清晰说明 REST、Feign、Dubbo、MQ 在同一个业务系统中的边界和差异。
