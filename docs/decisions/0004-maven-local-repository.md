# 0004 Maven Local Repository

状态：已采纳。

日期：2026-05-20。

## 背景

本项目将在学习过程中逐步引入 Spring Boot、Spring Cloud、MyBatis Plus、Spring Cloud Alibaba、数据库驱动、消息队列客户端、可观测性组件等大量依赖。如果 Maven 依赖下载到默认用户目录，后续排查、清理、迁移和复现都会更分散。

用户已指定 Maven 依赖下载路径：

```text
D:\software\maven_download
```

本地 Maven 安装信息：

| 项目 | 值 |
|---|---|
| Maven | Apache Maven `3.9.16` |
| Maven 路径 | `D:\software\apache-maven-3.9.16` |
| JDK | `17.0.19` |
| JDK 路径 | `D:\software\jdk-17.0.19` |

## 决策

项目 Maven 依赖统一下载到：

```text
D:\software\maven_download
```

后续创建项目时，优先通过以下方式之一保证该规则生效：

1. 在 Maven 全局或用户 `settings.xml` 中配置 `localRepository`。
2. 在项目文档中明确使用 `-Dmaven.repo.local=D:\software\maven_download`。
3. 如项目引入 `.mvn/maven.config`，可在确认团队接受本机路径约束后写入该参数。

由于该路径是本机绝对路径，若未来项目要共享给其他机器或团队，应提供可替换配置说明，避免把个人机器路径变成不可移植约束。

## 验证方式

查看 Maven 实际本地仓库：

```powershell
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

或在使用 Maven Wrapper 后执行：

```powershell
.\mvnw.cmd help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

期望输出：

```text
D:\software\maven_download
```

## 影响

1. `v0.1` 创建后端项目和 Maven Wrapper 时必须考虑该本地仓库路径。
2. README 应记录该 Maven 本地仓库要求。
3. 如果后续使用 Jenkins 或容器构建，不应强行复用该 Windows 本机路径，而应使用 CI/CD 环境自己的 Maven 缓存路径。

