# 0010 Docker Service Containerization

## 状态

已采纳。

## 日期

2026-05-26，Asia/Shanghai。

## 背景

项目后续会逐步引入 Nacos、Redis、RabbitMQ、Kafka、Elasticsearch、Seata、Jenkins、MinIO、InfluxDB、Neo4j、Prometheus、Grafana、SkyWalking、ELK、Nginx 等基础设施和运维服务。用户的开发环境是 Windows 11，本地 Java、Git、Maven、Node.js 用本机环境，其他服务统一通过 Docker Desktop 下载镜像并运行容器。

为了后续某一个服务从单节点扩展为多节点集群时不需要推翻早期部署方式，必须从单节点阶段就保持容器边界清晰。

## 决策

1. 后续新增的基础设施服务统一通过 Docker Desktop 运行容器，镜像通过 `docker compose pull`、`docker compose up` 或 Docker Desktop 自动拉取。
2. 每个独立服务必须对应独立容器，不允许把多个不同中间件塞进同一个容器。
3. 每个集群节点也必须对应独立容器，例如 Nacos 3 节点、Redis Cluster 6 节点、Seata 3 节点都应由多个同类容器组成。
4. 如果某个服务依赖辅助组件，辅助组件也应是独立容器。例如 Kafka 如果需要控制器或协调组件，应拆成独立容器；ELK 中 Elasticsearch、Logstash、Kibana 也应分别使用独立容器。
5. 单节点 compose 与集群 compose 分开维护。单节点用于当前 milestone 最小可运行验证，集群 compose 用于后续扩展或演练。
6. 容器名称、网络、volume、端口和健康检查必须清晰命名，并带 `java-demo` 项目前缀，避免污染其他本地项目。
7. 不修改 Docker Desktop 全局配置。所有可追踪配置都放在当前项目目录下，例如 `infra/docker-compose`、`deploy` 或 docs。

## 服务容器边界示例

| 服务 | 单节点容器示例 | 集群化方式 |
|---|---|---|
| Nacos | `java-demo-nacos-1` | `java-demo-nacos-1`、`java-demo-nacos-2`、`java-demo-nacos-3` |
| Redis | `java-demo-redis-1` | Redis Cluster 每个 master/replica 一个容器 |
| RabbitMQ | `java-demo-rabbitmq-1` | RabbitMQ 每个 broker 节点一个容器 |
| Kafka | `java-demo-kafka-1` | Kafka 每个 broker/controller 节点一个容器 |
| Elasticsearch | `java-demo-elasticsearch-1` | Elasticsearch 每个 node 一个容器 |
| Seata | `java-demo-seata-1` | Seata 3 节点各自独立容器 |
| Jenkins | `java-demo-jenkins-1` | Jenkins controller 独立容器，后续 agent 如需容器化也独立容器 |

## 文件组织要求

```text
infra
└─ docker-compose
   ├─ nacos
   │  ├─ docker-compose.single.yml
   │  └─ docker-compose.cluster.yml
   ├─ redis
   │  ├─ docker-compose.single.yml
   │  └─ docker-compose.cluster.yml
   ├─ mq
   │  ├─ rabbitmq.single.yml
   │  ├─ rabbitmq.cluster.yml
   │  ├─ kafka.single.yml
   │  └─ kafka.cluster.yml
   └─ ...
```

具体文件名可以根据 milestone 简化，但必须保持“单节点”和“集群演练”边界清晰。

## 验收要求

每个新增 Docker 服务的 milestone 至少记录：

1. 镜像名称和版本。
2. 容器名称。
3. 暴露端口，并确认避开 `7991-8090`、`8146-8245`。
4. volume 名称和用途。
5. 所属 Docker network。
6. 健康检查或最小可用验证命令。
7. 单节点到集群化时需要新增哪些容器。

## 影响

1. 后续 Nacos、Redis、RabbitMQ、Kafka、Elasticsearch、Seata、Jenkins 等 milestone 必须遵守该容器边界。
2. Docker Compose 不能为了省事把多个服务写进同一个自定义镜像容器。
3. 如果某个官方镜像内部天然包含多个进程，必须在 milestone 文档中说明原因，并确认不会影响后续集群扩展。
4. 文档和 README 中的启动命令应优先使用 `docker compose -f ... up -d`，停止命令使用 `docker compose -f ... stop` 或 `down`，避免依赖 Docker Desktop UI 手动点击。
