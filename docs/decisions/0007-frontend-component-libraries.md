# 0007 Frontend Component Libraries

## 状态

已采纳。

## 日期

2026-05-22，Asia/Shanghai。

## 背景

项目后续会分别实现 `v0.3 React Frontend` 和 `v0.4 Vue Frontend`。为了让两套前端在学习时更接近真实后台管理系统开发方式，同时便于对比 React 与 Vue 生态中的主流企业级组件库，需要在正式开发前明确组件库选择。

## 决策

1. `frontend-react` 使用 Ant Design 组件库实现页面、表单、表格、布局和反馈组件。
2. `frontend-vue` 使用 Element UI 系列组件库实现页面、表单、表格、布局和反馈组件。
3. 如果 `v0.4` 选择 Vue 3，则使用 Element Plus；Element Plus 视为 Element UI 在 Vue 3 生态中的延续版本。
4. 两套前端都应优先使用组件库提供的表单校验、表格、分页、消息提示、弹窗和布局能力，不重复造基础 UI 轮子。
5. 两套前端的页面结构和交互可以保持相似，方便学习时横向比较 Ant Design 与 Element UI / Element Plus 的使用差异。
6. 组件库策略与语言策略配合执行：React + Ant Design 使用 TypeScript，Vue + Element UI / Element Plus 使用 JavaScript。

## 影响

| 范围 | 影响 |
|---|---|
| `v0.3 React Frontend` | React 管理端默认引入 Ant Design，登录页、首页、用户列表、用户编辑表单均基于 Ant Design 实现 |
| `v0.4 Vue Frontend` | Vue 管理端默认引入 Element UI 系列组件库；若使用 Vue 3，则引入 Element Plus |
| README 和 milestone 文档 | 前端启动、依赖和验收说明需要记录实际使用的组件库 |
| 后续前端扩展 | WebSocket、MinIO 上传、搜索、趋势图等前端页面应延续对应组件库的交互风格 |

## 执行约束

1. 开发 `v0.3` 前必须在 React milestone 中确认 Ant Design 依赖、样式引入方式和主题配置方式。
2. 开发 `v0.4` 前必须在 Vue milestone 中确认 Vue 版本；Vue 3 默认使用 Element Plus。
3. 开发 `v0.4` 时 Vue 组件和页面使用 JavaScript 编写，不为 Element Plus 单独引入 TypeScript 业务代码。
4. 不同时在同一套前端中混用多个同类大型 UI 组件库，除非新增决策记录说明原因。
5. 页面实现应保留学习价值，关键组件封装、表单校验、表格分页和接口错误处理需要补充中文注释。
