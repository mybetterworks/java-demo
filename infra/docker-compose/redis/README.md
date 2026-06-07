# Redis Infrastructure Notes

## v0.7 Single Node

`docker-compose.yml` starts one independent Redis container:

| Item | Value |
|---|---|
| Container | `java-demo-redis-1` |
| Image | `redis:7.2.5-alpine` |
| Host port | `6379` by default, or `${JAVA_DEMO_REDIS_HOST_PORT}` when overridden |
| Data volume | `java-demo-redis-data` |
| Network | `java-demo-redis-net` |
| Health check | `redis-cli ping` |

Start:

```powershell
docker compose -f infra\docker-compose\redis\docker-compose.yml up -d
```

Start with an alternate host port when local `6379` is unavailable:

```powershell
$env:JAVA_DEMO_REDIS_HOST_PORT='16380'
docker compose -f infra\docker-compose\redis\docker-compose.yml up -d
$env:JAVA_DEMO_REDIS_PORT='16380'
```

Check:

```powershell
docker ps --filter "name=java-demo-redis-1"
docker exec java-demo-redis-1 redis-cli ping
```

Stop:

```powershell
docker compose -f infra\docker-compose\redis\docker-compose.yml stop
```

## Cluster Plan

Redis Cluster is not started in v0.7 because this milestone only needs one new core capability: cache and rate limit. The later cluster exercise should keep the same container boundary rule:

| Node | Future container |
|---|---|
| master-1 | `java-demo-redis-cluster-1` |
| master-2 | `java-demo-redis-cluster-2` |
| master-3 | `java-demo-redis-cluster-3` |
| replica-1 | `java-demo-redis-cluster-4` |
| replica-2 | `java-demo-redis-cluster-5` |
| replica-3 | `java-demo-redis-cluster-6` |

Each cluster node must run in its own Redis container with its own data volume. Do not combine multiple Redis nodes into one custom image or one container.
