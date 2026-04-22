# Group 91 Git 协作规范

## 1. 目的
本规范为 6 人新手小组定义一套简单、稳定的 Git 协作方式，目标是让协作过程清晰、减少合并冲突，并且让每位成员在 GitHub 上都能留下明确的个人贡献记录。

## 2. 分支策略
- 仓库只保留一个稳定主分支：`main`
- 正常开发时不要直接提交到 `main`
- 每位成员都应从 `main` 拉出自己的短期功能分支
- 推荐分支命名格式：
  - `name/feature`
  - 例如：
    - `long/profile-page`
    - `jojo/job-posting`
    - `alice/application-review`

## 3. 团队角色
- 6 名成员都应参与需求、编码、测试和文档工作
- 指定 1 人担任 `Scrum Master / coordinator`
- 指定 1 人负责 `Backlog / 文档维护`
- 这两个角色只是协调职责，不代表不写代码，每个人都仍然需要有自己的分支和提交记录

## 4. 建议任务拆分
- 成员 A：TA 个人资料与 CV 上传
- 成员 B：TA 岗位浏览与申请提交
- 成员 C：MO 岗位发布、编辑、关闭
- 成员 D：MO 审核申请与状态更新
- 成员 E：Admin 工作量统计与超负荷规则
- 成员 F：文件存储、联调支持、测试、README 和公共修复

## 5. 每日开发流程
### 5.1 开始任务前
开始新任务前，先同步最新的 `main`：

```bash
git checkout main
git pull origin main
```

然后为当前任务新建一个分支：

```bash
git checkout -b long/profile-page
```

### 5.2 开发与提交
- 一个分支只做一个功能或一个修复
- 小步提交，尽量不要攒很多改动一次性提交
- 提交信息要简短、清晰、统一

提交信息示例：
- `Add TA profile form`
- `Implement job application submission`
- `Fix workload calculation bug`
- `Update SRS and acceptance criteria`

常用命令：

```bash
git add .
git commit -m "Add TA profile form"
```

### 5.3 推送并发起 Pull Request
把本地分支推到 GitHub：

```bash
git push -u origin long/profile-page
```

然后在 GitHub 上从你的分支向 `main` 发起 Pull Request。

### 5.4 Review 与合并
- 每个 Pull Request 最少让 1 名队友看过再合并
- 合并完成后，所有成员都要同步本地 `main`：

```bash
git checkout main
git pull origin main
```

## 6. 团队规则
- 正常开发不要直接 push 到 `main`
- 不要使用 `git push --force`
- 未经沟通不要删除别人的分支
- 不要在同一个分支里混入多个无关功能
- 开新分支前先拉最新 `main`
- 如果两个人可能会改同一个文件，先沟通再动手

## 7. Pull Request 规范
每个 Pull Request 应尽量小，便于快速 review，并且至少包含：
- 清晰的标题
- 简短的改动说明
- 如果界面改了，附上截图
- 简短说明自己做了什么测试

推荐标题示例：
- `Add TA profile management page`
- `Implement MO job closing flow`
- `Add admin workload summary`

## 8. 减少冲突的规则
- 在并行开发前先统一 JSON 数据结构
- 在页面开发前先统一字段名、状态名和基础流程
- 尽量避免多人同时修改同一个核心文件
- 先搭好基础项目骨架，再按模块拆分功能

## 9. Sprint 内的协作方式
- 每个 Sprint 开始时：
  - 确认 backlog 中的高优先级故事
  - 给每条故事指定负责人
  - 只为已计划的工作创建分支
- Sprint 进行中：
  - 开短会同步进度
  - 如果任务变化，及时更新 GitHub issue 或组会记录
- Sprint 结束时：
  - 把已完成分支合并进 `main`
  - 检查当前版本是否可以演示
  - 记录 retrospective notes

## 10. 需要保留的 Agile 证据
为了课程评分，团队应保留以下记录：
- GitHub 分支
- commits
- pull requests
- merge 历史
- meeting notes
- backlog 更新记录
- prototype 的迭代版本

## 11. 最低配置清单
- 建好 GitHub 仓库并确认所有成员都有访问权限
- 确认团队统一使用 `main` 作为稳定分支
- 确认统一的分支命名规则
- 确认统一的提交信息风格
- 确认 Sprint 1 各功能负责成员
- 确认谁负责会议纪要和 backlog 更新

## 12. 推荐的第一步
在正式并行开发前，先由 1 名成员搭好项目骨架、基础目录结构和共享数据模型占位。这样后续大家再分模块开分支开发，冲突会明显少很多。
