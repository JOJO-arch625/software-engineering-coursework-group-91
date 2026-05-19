# TARS 助教招聘系统 — 具体需求文档

## 项目概览

**课程**: EBU6304 Software Engineering Group Project (35%)
**系统**: BUPT International School Teaching Assistant Recruitment System (TARS)
**开发方式**: 敏捷开发 (Scrum-lite)，Java Servlet/JSP Web 应用
**团队**: 6 人

---

## 1. 技术约束（强制）

| 约束 | 说明 |
|------|------|
| 应用形态 | 独立 Java 应用 **或** 轻量级 Java Servlet/JSP Web 应用 |
| 数据存储 | **禁止使用数据库**，必须用文本文件（JSON/CSV/XML/TXT） |
| 框架限制 | **禁止 Spring Boot** 及复杂框架 |
| JDK | Java 8 (JDK 1.8) |
| 服务器 | Tomcat (通过 tomcat7-maven-plugin) |

---

## 2. 用户角色

| 角色 | 职责 |
|------|------|
| **TA** | 申请者：维护个人资料、上传简历、浏览岗位、提交申请、查看状态 |
| **MO** (Module Organiser) | 岗位管理者：发布岗位、编辑岗位、关闭岗位、审查申请人、做出录用/拒绝决定 |
| **Admin** | 管理员：查看全局工作量、标记过载风险 |

---

## 3. 功能需求

### 3.1 账户与角色 (FR-001 ~ FR-003)

- 系统支持三种角色进入各自的功能界面
- 用户只能访问其角色允许的页面和操作
- 账户与角色信息从本地文件加载（`data/accounts.json`）

### 3.2 TA 功能 (FR-101 ~ FR-108)

| ID | 功能 |
|------|------|
| FR-101 | 创建个人资料：姓名、学号、联系方式、技能、可用时间 |
| FR-102 | 修改已有个人资料 |
| FR-103 | 上传 CV 文件（PDF/DOC/DOCX），可替换已有文件 |
| FR-104 | 浏览当前所有**开放**岗位 |
| FR-105 | 查看岗位详情：模块名称、职责、要求、截止日期、状态 |
| FR-106 | 对开放岗位提交申请 |
| FR-107 | **不能**对已关闭岗位提交申请 |
| FR-108 | 查看自己所有申请及状态 |

### 3.3 MO 功能 (FR-201 ~ FR-207)

| ID | 功能 |
|------|------|
| FR-201 | 创建岗位（模块代码、标题、技能要求、描述、知识要求、工作量、截止日期） |
| FR-202 | 编辑未关闭岗位的信息 |
| FR-203 | 关闭岗位，使其不再接受新申请 |
| FR-204 | 查看某岗位的所有申请记录 |
| FR-205 | 查看申请人个人资料和 CV |
| FR-206 | 将申请状态更新为 `Under Review` / `Accepted` / `Rejected` |
| FR-207 | 状态变更后 TA 侧同步更新 |

### 3.4 Admin 功能 (FR-301 ~ FR-303)

| ID | 功能 |
|------|------|
| FR-301 | 查看所有 TA 的已录用岗位分配情况 |
| FR-302 | 按 TA 汇总已录用岗位数量 |
| FR-303 | 按预设阈值（3 个录用岗位）标记过载风险 |

### 3.5 数据与文件处理 (FR-401 ~ FR-404)

| ID | 功能 |
|------|------|
| FR-401 | 结构化数据保存为 JSON 文件 |
| FR-402 | CV 文件保存到本地目录（`uploads/cv/`），JSON 中记录路径 |
| FR-403 | 启动时读取已有数据，操作后写回 |
| FR-404 | 数据文件缺失或格式异常时给出可理解的错误提示 |

---

## 4. 业务规则

| 规则 | 说明 |
|------|------|
| 申请上限 | TA 最多申请 **3 个**岗位 |
| 录用上限 | TA 最多被录用 **3 个**岗位 |
| 关闭岗位 | 关闭后不允许新申请提交 |
| 拒绝可见 | 被拒绝的申请对 TA 保持可见（不静默消失） |
| 过载阈值 | Admin 端：已录用 ≥ 3 个岗位标记为"过载风险" |

---

## 5. 数据模型

### 5.1 核心数据对象

| 对象 | 字段 |
|------|------|
| **UserAccount** | id, username, password, displayName, role (TA/MO/ADMIN), linkedId |
| **TAProfile** | id, fullName, studentNumber, email, phone, skills, availability, cvPath |
| **JobPosting** | id, moId, moduleCode, title, skills, requirements, workload, deadline, status (Open/Closed), description |
| **ApplicationRecord** | id, jobId, taId, priority (Priority 1/2/3), status (Submitted/Under Review/Accepted/Rejected), notes, submittedAt, applicantSkills, applicantDescription |
| **Notification** | id, userId, category (status/review/deadline/overload), message, link, read, createdAt |
| **WorkloadSummary** | taId, taName, acceptedModules (List), acceptedCount, overloadFlag |

### 5.2 数据文件

```
data/
  accounts.json          — 账户信息（3 个 demo 账号）
  applications.json      — 申请记录
  job-postings.json      — 岗位信息
  notifications.json     — 通知消息         //未提及的要求
  ta-profiles.json       — TA 个人资料
uploads/cv/              — 简历文件
```

---

## 6. AI 功能（课程要求）

Handout 明确要求可集成 AI-powered 功能：

| 功能 | 说明 |
|------|------|
| 技能匹配 | 将 TA 技能与岗位要求进行匹配评分 |
| 缺失技能提示 | 识别申请人缺失的技能 |
| 工作量均衡建议 | 基于工作量数据给出分配建议 |

文档明确写了 3 个 AI-powered features：
技能匹配
岗位需要什么技能 → 申请者有什么技能 → AI 自动匹配打分
缺失技能提示
告诉申请者：你申请这个岗位还缺哪些技能
工作量平衡
管理员用 AI 自动分配 TA，避免有人太忙、有人太闲

**AI 使用原则**：
- AI 输出作为**辅助**而非权威
- 必须**可解释** — 能说明 AI 为什么给出这个建议
- 最终决策权属于**人类用户**（MO/Admin）
- Viva 答辩可能被问 "Why did AI make this recommendation?" 或 "How did you modify AI's solution?"

---

## 7. 非功能需求

| ID | 说明 |
|------|------|
| NFR-001 | 界面清晰，关键操作 **3 次点击**内可达 |
| NFR-002 | 模块化设计，便于后续扩展 AI 功能 |
| NFR-003 | 避免复杂框架，降低部署难度 |
| NFR-004 | 基础输入校验：必填项检查、文件类型检查、角色访问控制 |
| NFR-005 | 文件写入失败时有明确错误提示，防止数据丢失 |
| NFR-006 | 演示场景下稳定运行，支持典型业务流程顺畅展示 |

---

## 8. 验收标准 (Acceptance Criteria)

| ID | 场景 | 预期结果 |
|------|------|------|
| AC-001 | TA 提交完整资料 + 合法 CV | 保存成功，提示保存成功 |
| AC-002 | TA 浏览岗位 | 只显示状态为 Open 的岗位 |
| AC-003 | TA 对开放岗位提交申请 | 生成申请记录，状态为 `Submitted` |
| AC-004 | TA 尝试申请已关闭岗位 | 阻止提交，提示不可申请 |
| AC-005 | MO 创建并保存岗位 | 岗位出现在开放岗位列表 |
| AC-006 | MO 关闭岗位 | TA 侧不允许新申请 |
| AC-007 | MO 更新申请为 Accepted/Rejected | TA 申请页面同步更新 |
| AC-008 | Admin 打开工作量页面 | 显示每个 TA 的录用数，标记过载 |
| AC-009 | 用户提交缺失必填字段 | 拒绝保存，给出明确提示 |
| AC-010 | 用户上传非法文件类型 | 拒绝上传，说明原因 |

---

## 9. 评估时间线

| 节点 | 日期 | 占比 | 交付物 |
|------|------|------|--------|
| **First Assessment** | 3/22 | 30% | Product Backlog (Excel) + Prototype (PDF) + Brief Report (PDF) |
| **Intermediate** | 4/12 | 20% | 现场 Demo + Viva 答辩 + GitHub 检查 |
| **Final** | 5/24 | 50% | 源码 + 测试 + 用户手册 + README + 演示视频 (≤10min) + 最终报告 + Viva |

---

## 10. MVP 明确排除项

以下**不在 MVP 范围内**：
- 真正的 AI/ML 匹配和推荐
- 邮件、短信通知
- 外部 SSO 登录集成
- 数据库（MySQL 等）
- Spring Boot 等框架
- 移动端应用
- 多学院、多学期并行管理
- 监考（invigilation）等其他活动招聘
