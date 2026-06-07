# MinIO 单节点说明

`v0.9` 使用单节点 MinIO 验证对象存储、头像上传和任务附件上传。MinIO 只作为基础设施容器运行，不和任何业务服务打包在同一个容器里。

## 启动

```powershell
docker compose -f infra\docker-compose\minio\docker-compose.yml up -d
```

## 检查

```powershell
docker ps --filter "name=java-demo-minio-1"
```

默认访问地址：

| 项目 | 值 |
|---|---|
| API Endpoint | `http://localhost:9000` |
| Console | `http://localhost:9001` |
| Root User | `java_demo_minio` |
| Root Password | `java_demo_minio_pwd_123` |
| Volume | `java-demo-minio-data` |
| Network | `java-demo-minio-net` |

如果本机端口被占用，可以临时覆盖宿主机端口，并同步设置后端服务的 `JAVA_DEMO_MINIO_ENDPOINT`：

```powershell
$env:JAVA_DEMO_MINIO_API_PORT='19000'
$env:JAVA_DEMO_MINIO_CONSOLE_PORT='19001'
docker compose -f infra\docker-compose\minio\docker-compose.yml up -d
$env:JAVA_DEMO_MINIO_ENDPOINT='http://127.0.0.1:19000'
$env:JAVA_DEMO_MINIO_PUBLIC_ENDPOINT='http://127.0.0.1:19000'
```

## Bucket

后端会在首次上传时检查并创建 bucket：

| Bucket | 用途 |
|---|---|
| `java-demo-avatars` | 用户头像 |
| `java-demo-task-attachments` | 任务附件 |

## 停止

```powershell
docker compose -f infra\docker-compose\minio\docker-compose.yml stop
```
