# 0003 Deploy Strategy

状态：已采纳。

日期：2026-05-20。

## 背景

开发环境是 Windows 11，本地 Java、Git、Maven 负责开发构建，其他基础设施通过 Docker Desktop 运行。目标最终包含 Docker、Kubernetes 集群和 Jenkins。

## 决策

部署路线采用三层递进：

1. 本地进程 + Docker 基础设施。
2. Docker Compose 一键启动应用和中间件。
3. Kubernetes 集群部署，Jenkins 做 CI/CD。

## 阶段安排

| 阶段 | 部署形态 | 说明 |
|---|---|---|
| `v0.1` - `v0.4` | 后端/前端本地进程，中间件 Docker | 降低调试成本 |
| `v0.5` - `v1.7` | Docker Compose 管理基础设施和部分应用 | 便于模拟集群依赖 |
| `v1.8` | Docker Compose 与 K8s 双形态 | 学习服务编排和集群部署 |
| `v1.9` | Jenkins 流水线 | 自动构建、测试、镜像、部署 |

## 文件组织

```text
infra
└─ docker-compose
   ├─ base
   ├─ mysql
   ├─ nacos
   ├─ redis
   ├─ mq
   ├─ observability
   └─ full

deploy
├─ nginx
├─ k8s
└─ jenkins
```

## 原则

1. 先单机可用，再集群演练。
2. 先 Docker Compose，再 Kubernetes。
3. Compose 文件按能力拆分，避免一个巨型 compose 难以维护。
4. K8s YAML 与 Compose 同步描述相同能力，便于对照学习。
5. Jenkins 最后引入，避免早期流水线掩盖基础问题。

## 验收

每个部署阶段必须提供：

1. 启动命令。
2. 停止命令。
3. 健康检查方式。
4. 对外端口说明。
5. 常见故障排查。
