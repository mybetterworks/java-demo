# 0005 Manual Git Commit Policy

## 状态

已采纳。

## 背景

项目已经提交到 GitHub。后续开发会由 Codex 辅助实现代码、运行验证和更新文档，但版本保存、提交历史和远端同步需要由用户保持最终控制。

## 决策

1. Codex 不允许自动执行 `git commit`、`git tag`、`git push`、`git merge`、`git rebase` 等会改变 Git 历史或远端状态的操作。
2. 每个 milestone 完成后，Codex 可以在最终说明或文档中提示建议的提交信息和 tag 名称，但必须由用户手动执行。
3. Codex 可以执行只读 Git 检查，例如 `git status`、`git diff`、`git log`，用于避免覆盖用户修改和说明当前工作区状态。
4. 如用户要求 Codex 执行提交或推送，Codex 应拒绝执行并提醒本项目规则是“用户手动提交”。

## 影响

| 项目 | 影响 |
|---|---|
| 开发流程 | Codex 负责实现、验证和记录，用户负责提交、打 tag 和推送 GitHub |
| 文档记录 | `ROADMAP.md`、`DEVELOPMENT_RULES.md`、`PROGRESS.md` 和 milestone 文档中的 Git 规则统一改为手动提交 |
| 风险控制 | 避免自动提交不完整代码、错误 tag 或误推送到远端 |

## 建议手动操作

milestone 完成并确认无误后，用户可以手动执行类似命令：

```powershell
git status
git add .
git commit -m "feat: complete v0.2 user crud"
git tag v0.2-user-crud
git push
git push --tags
```

以上命令仅作为示例，实际提交信息和 tag 名称由用户决定。
