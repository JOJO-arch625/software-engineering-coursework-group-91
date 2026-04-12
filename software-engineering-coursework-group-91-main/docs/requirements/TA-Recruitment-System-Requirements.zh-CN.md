# 助教招聘系统需求文档

## 1. 文档目的
本文档定义 BUPT International School 助教招聘系统（Teaching Assistant Recruitment System, TARS）的业务背景、范围、功能需求、非功能需求、数据存储方案、验收标准以及敏捷开发方式。本文档作为小组内部的中文主稿，用于统一需求理解、指导原型设计、拆分 Product Backlog，并作为英文课程提交版 SRS 的对应依据。

## 2. 项目背景与课程约束
### 2.1 项目背景
BUPT International School 每学期需要为各模块招聘 Teaching Assistant（TA），当前流程主要依赖表格和 Excel 文件完成信息收集、岗位发布、申请筛选和工作量统计，流程分散且效率较低。本项目拟开发一个简洁、可扩展的招聘系统，以支持 TA、Module Organiser（MO）和管理员在统一平台上完成招聘流程。

### 2.2 课程约束
- 本项目必须采用敏捷开发方法，并在需求、分析、设计、实现和测试阶段持续体现迭代和反馈。
- 系统实现形态限定为独立 Java 应用或轻量级 Java Servlet/JSP Web 应用。本项目默认采用轻量级 Java Servlet/JSP Web 应用。
- 系统禁止使用数据库，所有输入和输出数据必须使用简单文本文件格式存储。
- 最终产品需要支持 staged assessment，能够逐步形成 backlog、prototype、working versions 和 final delivery。

### 2.3 当前分析前提
- 当前阶段暂无可直接访问的真实访谈对象。
- 第一版需求主要来自课程 handout 分析、团队讨论和代理假设（proxy assumptions）。
- 后续如获得真实用户反馈，应通过 backlog refinement 对需求进行修订。

## 3. 利益相关者与用户角色
### 3.1 利益相关者
- TA 申请人：希望查看岗位、提交申请并跟踪结果。
- MO：希望快速发布岗位、审查候选人并做出录取决定。
- 管理员：希望查看 TA 整体工作量，避免个别学生承担过多任务。
- 课程教学团队：关注系统是否体现清晰需求、敏捷方法和可验证的工程过程。
- 项目小组成员：需要通过 GitHub 分支协作、提交记录和会议纪要体现个人贡献。

### 3.2 用户角色定义
- `TA`：系统中的申请者，可维护个人资料、上传简历并申请岗位。
- `MO`：岗位发布者和评审者，可管理岗位并处理申请。
- `Admin`：管理视角用户，可查看全局工作量统计。

## 4. 项目范围与排除项
### 4.1 MVP 范围
本项目第一版正式范围仅覆盖模块 TA 招聘，不包含 invigilation 或其他学校活动。系统支持以下核心流程：
- TA 注册或录入基础账户后维护个人资料。
- TA 上传和更新 CV。
- TA 浏览开放岗位并提交申请。
- TA 查看申请状态。
- MO 发布、编辑和关闭岗位。
- MO 查看申请列表，查看申请者资料和 CV，并给出录取或拒绝结果。
- Admin 查看 TA 总体工作量，并识别潜在超负荷情况。

### 4.2 排除项
以下内容不纳入 MVP：
- AI 匹配、推荐或解释功能。
- 邮件、短信、站内消息通知。
- 外部单点登录或学校现有账户系统集成。
- 数据库、Spring Boot 或其他复杂框架。
- 移动端应用。
- 多学院、多学期并行管理的复杂配置。
- 非模块类岗位招聘流程。

## 5. 业务流程概述
### 5.1 TA 侧流程
1. TA 登录或进入 TA 角色界面。
2. TA 创建或更新个人资料。
3. TA 上传或替换个人 CV。
4. TA 浏览岗位列表并查看岗位详情。
5. TA 对开放岗位提交申请。
6. TA 在个人申请页面查看当前状态。

### 5.2 MO 侧流程
1. MO 登录或进入 MO 角色界面。
2. MO 创建岗位并填写模块名称、职责、时间安排和申请条件。
3. MO 编辑或关闭岗位。
4. MO 查看岗位申请列表。
5. MO 查看候选人资料和 CV。
6. MO 将申请更新为录取或拒绝。

### 5.3 Admin 侧流程
1. Admin 登录或进入管理员界面。
2. Admin 查看所有 TA 当前已录取岗位数量和相关模块。
3. Admin 识别超出预设工作量阈值的 TA。

## 6. 功能需求
### 6.1 账户与角色
- `FR-001` 系统应支持按角色进入不同功能界面。
- `FR-002` 系统应限制用户只能访问其角色允许的页面和操作。
- `FR-003` 系统应能够基于本地文件加载账户与角色信息。

### 6.2 TA 功能
- `FR-101` TA 应能够创建个人资料，包括姓名、学号或唯一编号、联系方式、技能、可工作时间等基础信息。
- `FR-102` TA 应能够修改已有个人资料。
- `FR-103` TA 应能够上传 CV 文件，并在需要时替换已有文件。
- `FR-104` 系统应向 TA 展示当前所有开放岗位。
- `FR-105` TA 应能够查看岗位详情，包括模块名称、岗位职责、申请条件、截止时间和状态。
- `FR-106` TA 应能够对开放岗位提交申请。
- `FR-107` TA 不应能够对已关闭岗位提交申请。
- `FR-108` TA 应能够查看自己所有申请及其状态。

### 6.3 MO 功能
- `FR-201` MO 应能够创建岗位并保存岗位信息。
- `FR-202` MO 应能够编辑未结束岗位的信息。
- `FR-203` MO 应能够关闭岗位，使其不再接受新申请。
- `FR-204` MO 应能够查看某岗位的所有申请记录。
- `FR-205` MO 应能够查看申请人的个人资料和 CV 路径或下载入口。
- `FR-206` MO 应能够将申请状态更新为 `Under Review`、`Accepted` 或 `Rejected`。
- `FR-207` 系统应记录申请状态变更结果并反映到 TA 视图中。

### 6.4 Admin 功能
- `FR-301` Admin 应能够查看所有已录取 TA 的岗位分配情况。
- `FR-302` 系统应能够按 TA 汇总已录取岗位数量。
- `FR-303` 系统应支持基于预设阈值标记潜在超负荷的 TA。

### 6.5 数据与文件处理
- `FR-401` 系统应将结构化数据保存为 JSON 文件。
- `FR-402` 系统应将上传的 CV 文件保存到指定本地目录，并在 JSON 中记录文件路径。
- `FR-403` 系统应在启动时读取已有文件数据，并在操作后写回。
- `FR-404` 系统应在数据文件缺失或格式异常时给出可理解的错误提示。

## 7. 非功能需求
- `NFR-001` 系统应保持界面清晰，主要操作在 3 次点击内可到达对应页面。
- `NFR-002` 系统应采用模块化设计，便于后续增加 AI 功能或额外招聘场景。
- `NFR-003` 系统应避免依赖复杂框架，以符合课程要求并降低部署复杂度。
- `NFR-004` 系统应具备基础输入校验，包括必填项检查、文件类型检查和角色访问控制。
- `NFR-005` 系统应确保文件写入不会导致明显的数据丢失风险，例如写入失败时提供错误提示。
- `NFR-006` 系统应在演示场景下稳定运行，支持多个典型业务流程顺畅展示。

## 8. 数据模型与文件存储方案
### 8.1 核心数据对象
- `UserAccount`：`userId`、`username`、`role`
- `TAProfile`：`taId`、`name`、`studentNumber`、`email`、`phone`、`skills`、`availability`、`cvPath`
- `Module`：`moduleId`、`moduleCode`、`moduleName`、`organiserId`
- `JobPosting`：`jobId`、`moduleId`、`title`、`description`、`requirements`、`deadline`、`status`
- `Application`：`applicationId`、`jobId`、`taId`、`submittedAt`、`status`
- `WorkloadSummary`：`taId`、`acceptedJobsCount`、`acceptedJobIds`、`overloadFlag`

### 8.2 建议文件结构
- `data/accounts.json`
- `data/ta_profiles.json`
- `data/modules.json`
- `data/job_postings.json`
- `data/applications.json`
- `data/config.json`
- `uploads/cv/`

### 8.3 文件存储规则
- 所有结构化业务数据默认使用 JSON 存储。
- `config.json` 中可配置工作量阈值、允许的 CV 文件类型和最大文件大小。
- 若后续需要导出统计信息，可增加 CSV 导出，但不替代 JSON 主存储。

## 9. 验收标准
### 9.1 核心验收场景
- `AC-001` 当 TA 填写完整资料并上传合法 CV 时，系统应成功保存资料并提示保存成功。
- `AC-002` 当 TA 浏览岗位时，系统只显示状态为开放的岗位。
- `AC-003` 当 TA 对开放岗位提交申请时，系统应生成申请记录并显示状态为 `Submitted`。
- `AC-004` 当 TA 尝试申请已关闭岗位时，系统应阻止提交并提示该岗位不可申请。
- `AC-005` 当 MO 创建岗位并保存后，该岗位应出现在开放岗位列表中。
- `AC-006` 当 MO 关闭岗位后，TA 侧不应再允许新申请提交。
- `AC-007` 当 MO 将申请标记为 `Accepted` 或 `Rejected` 后，TA 在个人申请页面应看到同步更新后的状态。
- `AC-008` 当 Admin 打开工作量界面时，系统应显示每名 TA 的已录取岗位数量，并标记超出阈值的 TA。
- `AC-009` 当用户提交缺失必填字段的信息时，系统应拒绝保存并给出明确提示。
- `AC-010` 当用户上传非法文件类型时，系统应拒绝上传并说明原因。

### 9.2 验收测试原则
- 高优先级用户故事必须具备可直接执行的验收标准。
- 每个角色至少应有一个主成功路径和一个失败或校验路径。
- 验收标准应支持最终 demo/viva 中的 acceptance testing tasks。

## 10. Agile Development Approach
### 10.1 采用方式
本项目采用 `Scrum-lite` 作为敏捷开发方式。选择该方式的原因如下：
- 课程明确要求在整个项目过程中采用 Agile methods。
- 项目规模相对有限，完整 Scrum 的仪式成本较高。
- Scrum-lite 已足够支持 backlog 管理、迭代计划、评审、回顾和持续改进。

### 10.2 团队流程
- 建立并维护 `Product Backlog`。
- 每个 Sprint 开始前进行 `Sprint Planning`，确定目标和故事范围。
- Sprint 期间通过 GitHub 分支开发和短会同步进度。
- Sprint 结束后进行 `Sprint Review`，检查已完成功能和问题。
- Review 后进行 `Retrospective`，总结改进点并调整下一轮 backlog。

### 10.3 团队角色
- 全体成员共同参与需求分析、设计、实现、测试和文档工作。
- 指定一名 `Scrum Master / coordinator` 负责会议组织和进度跟踪。
- 指定一名 `Product Backlog Owner` 负责整理和维护 backlog，但不垄断需求决策。

### 10.4 敏捷证据
以下内容作为 Agile evidence：
- GitHub 分支、commits、pull requests、merge 记录。
- 每次组会的会议纪要（meeting notes）。
- backlog 的增量更新记录。
- prototype 和 working versions 的迭代演进记录。

## 11. Release / Iteration Plan
### 11.1 Sprint 1
- 分析项目 handout。
- 明确用户角色、范围和 MVP。
- 完成用户故事初稿和 Product Backlog 初稿。
- 完成原型设计。
- 设计核心数据模型和文件存储方案。
- 产出 first assessment 所需材料。

### 11.2 Sprint 2
- 实现 TA 侧核心流程：个人资料、CV 上传、岗位浏览、申请提交、状态查看。
- 完成 Version 1 并记录发现的问题。

### 11.3 Sprint 3
- 实现 MO 侧岗位管理与申请处理流程。
- 实现 Admin 工作量查看。
- 修复 Version 1 中暴露的关键问题。
- 完成 Version 2。

### 11.4 Sprint 4
- 增强错误处理和输入校验。
- 完善测试、用户手册、README 和演示材料。
- 准备最终提交和 demo/viva。

### 11.5 迭代调整原则
- 每个 Sprint 结束后依据 review、反馈和 retrospective 更新 backlog。
- 先完成 Must-have 的核心招聘流程，再考虑 Could-have 的增强项。

## 12. 第一阶段材料与需求文档映射
### 12.1 Product Backlog
Backlog 需要严格使用课程模板字段：
- `Story ID`
- `Story Name`
- `Description`
- `Priority`
- `Iteration (Sprint) number`
- `Acceptance Criteria`
- `Estimation`
- `Notes`
- `Date started (or planned)`
- `Date finished (or planned)`

用户故事应直接来源于本文档中的功能需求与验收标准。

### 12.2 Prototype
Prototype 应覆盖目标系统的完整业务流程，而不仅仅是第一轮实现代码范围。每个角色都应在原型中出现，并体现基本成功路径和失败或校验场景。

### 12.3 Brief Report
Brief Report 应围绕以下四项展开：
- fact-finding techniques
- iteration plan
- prioritisation
- estimation methods

当前阶段应如实说明证据来源主要包括：
- handout analysis
- team workshop
- proxy assumptions

若后续获得真实用户反馈，可作为 supporting material 追加。

## 13. Future Enhancements
- 基于技能的岗位匹配建议。
- 对申请人缺失技能的提示。
- 更细粒度的工作量平衡建议。
- 邮件通知或消息提醒。
- 面向多种招聘活动的扩展支持。

## 14. 默认假设与待确认事项
- 当前尚无现成项目代码仓库，后续应尽快建立 GitHub 仓库。
- 主分支默认使用 `master`，各成员需通过可见个人分支协作，并在检查节点前完成合并。
- 当前无真实访谈对象，因此需求证据仍属于第一版分析结果。
- 若团队后续决定缩小或扩大范围，应同步更新本需求文档、backlog 和原型。
