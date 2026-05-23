# 0006 Node Frontend Environment

## 状态

已采纳。

## 日期

2026-05-22，Asia/Shanghai。

## 背景

项目后续会在 `v0.3` 引入 React 管理端，在 `v0.4` 引入 Vue 管理端，并在后续 Nginx、Docker、Kubernetes 和 Jenkins 阶段反复执行前端开发、构建和部署验证。

用户已在本地 Windows 11 系统环境中配置好 Node.js 22。为了避免 React 和 Vue 阶段使用不同 Node 版本导致依赖、脚手架、构建工具行为不一致，需要明确前端默认运行环境。

## 决策

1. Node.js `22.x` 作为本项目默认前端运行环境。
2. `frontend-react`、`frontend-vue` 和后续所有前端本地开发、依赖安装、开发服务器、生产构建默认使用 Node.js `22.x`。
3. 进入前端 milestone 前，必须在新终端执行 `node -v`，期望结果为 `v22.x.x`。
4. 前端默认优先使用项目内 `npm scripts` 作为任务入口。
5. 如果后续改用 `pnpm`、`yarn` 或其他包管理器，需要新增或更新决策记录，并提交对应 lockfile。

## 影响

| 范围 | 影响 |
|---|---|
| `v0.3 React Frontend` | React 项目创建、启动、测试和构建以 Node.js `22.x` 为基线 |
| `v0.4 Vue Frontend` | Vue 项目创建、启动、测试和构建以 Node.js `22.x` 为基线 |
| Nginx 和容器化阶段 | 前端静态资源构建应沿用 Node.js `22.x` |
| Jenkins 阶段 | CI 前端构建节点或镜像应选择 Node.js `22.x` |

## 执行约束

1. 不依赖全局脚手架生成不可追踪内容，应优先通过项目内脚本和 lockfile 固化行为。
2. 不把 Node.js 运行时安装文件提交到项目仓库。
3. 如果当前 Codex 或 PowerShell 会话无法识别最新 Node.js PATH，应重启终端或 Codex 会话后再验证。
4. 如果本地 `node -v` 不是 `v22.x.x`，不得开始前端 milestone，应先修正本地环境或在 `docs/PROGRESS.md` 中记录偏差。
