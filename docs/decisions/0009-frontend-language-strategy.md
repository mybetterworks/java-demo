# 0009 Frontend Language Strategy

## 状态

已采纳。

## 日期

2026-05-26，Asia/Shanghai。

## 背景

项目已经在 `v0.3 React Frontend` 中完成 React 管理端，并实际采用 Vite + React + TypeScript。后续 `v0.4 Vue Frontend` 尚未开发，用户希望 Vue 管理端使用 JavaScript 开发，以便在同一个学习项目中对比 React TypeScript 与 Vue JavaScript 两种常见前端开发体验。

该决策只约束本项目的前端业务代码和脚手架选择，不改变 Node.js 22、本地端口、组件库和后端 API 规划。

## 决策

1. `frontend-react` 使用 TypeScript 开发，业务代码优先使用 `.ts` / `.tsx`。
2. React 后续功能扩展继续保留 TypeScript 类型检查，构建脚本应继续包含类型检查步骤。
3. `frontend-vue` 使用 JavaScript 开发，创建 `v0.4` 项目时选择 JavaScript 模板。
4. Vue 管理端不主动引入 TypeScript 模板、`tsconfig` 或 `.ts` / `.tsx` 业务代码。
5. 如果未来需要改变 Vue 语言策略，必须新增或更新决策记录，并同步更新 `ROADMAP`、`DEVELOPMENT_RULES`、`PROGRESS` 和对应 milestone。

## 影响

| 范围 | 影响 |
|---|---|
| `v0.3 React Frontend` | 已完成版本保持 React + TypeScript，后续维护继续执行类型检查 |
| `v0.4 Vue Frontend` | 待开发版本必须选择 Vue + JavaScript，不使用 TypeScript 模板 |
| README 和 milestone 文档 | 前端启动、构建和验收说明需要明确对应语言 |
| 后续前端扩展 | WebSocket、MinIO 上传、搜索、趋势图等功能在 React 侧使用 TypeScript，在 Vue 侧使用 JavaScript |

## 执行约束

1. 开发 React 功能时，接口类型、页面状态和组件属性应尽量保持类型明确，避免退化为大量 `any`。
2. 开发 Vue 功能时，优先使用 JavaScript、Vue 单文件组件和必要的 JSDoc 注释表达意图，不引入 TypeScript 编译链。
3. 两套前端应共享接口语义、页面交互、操作路径和核心处理逻辑，以便进行对比学习。
4. 两套前端不要求目录一一镜像；React 可以保持 `components` 页面组织，Vue 应体现常见 Vue 分层，例如 `views`、`layouts`、`composables`、`api` 和 `storage`。
5. 不要为了“代码一致”强行统一语言、组件库、样式实现或目录结构；React 保持 TypeScript，Vue 保持 JavaScript。
6. 验收 `v0.4` 时，需要检查 `frontend-vue` 未生成 `tsconfig`，且业务代码未使用 `.ts` / `.tsx`。
