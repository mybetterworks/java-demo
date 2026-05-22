# 0001 Java Version

状态：已采纳。

日期：2026-05-20。

## 背景

本项目目标包含 Spring Cloud Gateway、Spring Cloud、Nacos、Seata、可观测性、容器化和后续 K8s 部署。完整微服务学习路线更适合使用 Java 17 或更高 LTS。

用户已在本机升级并配置：

| 项目 | 值 |
|---|---|
| JDK | `17.0.19` |
| JDK 路径 | `D:\software\jdk-17.0.19` |
| Maven | Apache Maven `3.9.16` |
| Maven 路径 | `D:\software\apache-maven-3.9.16` |
| Maven 本地仓库 | `D:\software\maven_download` |

## 决策

项目目标基线使用 JDK `17.0.19`。

正式开发前优先完成以下确认：

1. 确认 `JAVA_HOME` 指向 `D:\software\jdk-17.0.19`。
2. 确认 `PATH` 包含 `D:\software\jdk-17.0.19\bin`。
3. 确认 `PATH` 包含 `D:\software\apache-maven-3.9.16\bin`。
4. 确认 `mvn -v` 中 Java version 显示 `17.0.19`。
5. 项目引入 Maven Wrapper，避免依赖全局 Maven PATH。
6. Maven 本地仓库统一使用 `D:\software\maven_download`。
7. 在 `README.md` 中记录 Java、Maven 和本地仓库要求。

## 可选方案

| 方案 | 优点 | 缺点 |
|---|---|---|
| 使用 Java 17 | 适合现代 Spring 生态，长期维护稳定 | 需要确保所有终端和工具都刷新到新 `JAVA_HOME` |
| 升级 Java 21 | 生命周期更长，性能和语言能力更好 | 部分依赖组合需要额外确认 |

## 影响

1. `v0.1` 开发前应确认 Java 和 Maven 命令均使用目标路径。
2. Spring Boot、Spring Cloud、Spring Cloud Alibaba、MyBatis Plus 等依赖版本需按目标 Java 版本统一选择。
3. Docker 镜像基础层也应与 Java 版本一致。
4. Maven 依赖下载路径应保持为 `D:\software\maven_download`。

## 验证方式

```powershell
java -version
mvn -v
```

如果项目引入 Maven Wrapper：

```powershell
.\mvnw.cmd -v
```

期望 `mvn -v` 和 `.\mvnw.cmd -v` 中都显示 Java version 为 `17.0.19`。
