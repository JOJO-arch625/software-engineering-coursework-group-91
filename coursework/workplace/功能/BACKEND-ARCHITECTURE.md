# 后端代码实现总结 — TA Recruitment System (TARS)

## 项目概览

这是一个 **TA（助教）招聘管理系统**，使用标准的 **三层架构（Servlet + Service + Storage）**，无数据库，所有数据以 JSON 文件形式持久化。

- **技术栈**: Java 8, Jakarta Servlet 4.0, Google Gson, Maven + Tomcat 7
- **数据存储**: `data/` 目录下的 JSON 文件（accounts, ta-profiles, job-postings, applications, notifications）
- **用户角色**: TA（助教）、MO（Module Organiser 课程负责人）、ADMIN（管理员）

---

## 三层架构

```
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (13 个 Servlet + JSP 视图)       │
│  包: com.group91.tars.servlet                       │
│  BasePageServlet 提供认证检查、i18n、Flash消息等       │
├─────────────────────────────────────────────────────┤
│  Business Logic Layer (服务层)                       │
│  包: com.group91.tars.service                       │
│  TarsService — 单例，包含全部业务逻辑                  │
├─────────────────────────────────────────────────────┤
│  Data Access Layer (持久化层)                         │
│  包: com.group91.tars.storage                       │
│  JsonDataStore — 单例，Gson 读写 JSON 文件            │
└─────────────────────────────────────────────────────┘
```

---

## 各模块详细说明

### 1. Model 层 (7 个 POJO)

| 类 | 用途 |
|---|---|
| [UserAccount.java](../../src/main/java/com/group91/tars/model/UserAccount.java) | 用户账户：id, username, password, displayName, role, linkedId |
| [TAProfile.java](../../src/main/java/com/group91/tars/model/TAProfile.java) | TA个人资料：姓名、学号、邮箱、技能、空闲时间、CV路径 |
| [JobPosting.java](../../src/main/java/com/group91/tars/model/JobPosting.java) | 岗位：课程代码、技能要求、工作量、截止日期、状态(Open/Closed) |
| [ApplicationRecord.java](../../src/main/java/com/group91/tars/model/ApplicationRecord.java) | 申请记录：优先级、状态(Submitted/Under Review/Accepted/Rejected)、备注 |
| [Notification.java](../../src/main/java/com/group91/tars/model/Notification.java) | 通知：类别(status/review/overload/deadline)、已读标记、时间戳 |
| [WorkloadSummary.java](../../src/main/java/com/group91/tars/model/WorkloadSummary.java) | 工作量汇总：TA已接受的模块列表、是否超负荷 |
| [OperationResult.java](../../src/main/java/com/group91/tars/model/OperationResult.java) | 操作结果封装：success/message，支持 i18n 消息键 |

### 2. Servlet 层 (13 个)

**公共路由：**
- [LoginServlet.java](../../src/main/java/com/group91/tars/servlet/LoginServlet.java) — `/login`：登录认证，成功后按角色跳转
- [LogoutServlet.java](../../src/main/java/com/group91/tars/servlet/LogoutServlet.java) — `/logout`：清除 Session
- [HomeServlet.java](../../src/main/java/com/group91/tars/servlet/HomeServlet.java) — `/gateway`：角色首页，展示仪表盘概览

**TA 端：**
- [TaDashboardServlet.java](../../src/main/java/com/group91/tars/servlet/TaDashboardServlet.java) — `/ta/dashboard`：TA 仪表盘
- [TaJobsServlet.java](../../src/main/java/com/group91/tars/servlet/TaJobsServlet.java) — `/ta/jobs`：浏览开放岗位
- [TaJobDetailServlet.java](../../src/main/java/com/group91/tars/servlet/TaJobDetailServlet.java) — `/ta/job`：岗位详情 + 提交申请
- [TaApplicationsServlet.java](../../src/main/java/com/group91/tars/servlet/TaApplicationsServlet.java) — `/ta/applications`：查看我的申请
- [TaProfileServlet.java](../../src/main/java/com/group91/tars/servlet/TaProfileServlet.java) — `/ta/profile`：个人资料编辑 + CV上传（`@MultipartConfig`）

**MO 端：**
- [MoDashboardServlet.java](../../src/main/java/com/group91/tars/servlet/MoDashboardServlet.java) — `/mo/dashboard`：我的岗位、申请统计
- [MoReviewServlet.java](../../src/main/java/com/group91/tars/servlet/MoReviewServlet.java) — `/mo/review`：审核申请（接受/拒绝/标记审核中）
- [MoJobEditServlet.java](../../src/main/java/com/group91/tars/servlet/MoJobEditServlet.java) — `/mo/jobs/edit`：新建/编辑/关闭岗位

**Admin 端：**
- [AdminWorkloadServlet.java](../../src/main/java/com/group91/tars/servlet/AdminWorkloadServlet.java) — `/admin/workload`：查看所有TA工作量

**通用功能：**
- [SearchServlet.java](../../src/main/java/com/group91/tars/servlet/SearchServlet.java) — `/search`：全局搜索（岗位/申请/申请人），按角色过滤
- [InboxServlet.java](../../src/main/java/com/group91/tars/servlet/InboxServlet.java) — `/inbox`：通知中心，标记已读
- [AiAssistServlet.java](../../src/main/java/com/group91/tars/servlet/AiAssistServlet.java) — `/ai/assist`：AI 辅助概念页面

### 3. Service 层

[TarsService.java](../../src/main/java/com/group91/tars/service/TarsService.java) 是核心业务逻辑，包含：

- **认证**: 用户名/密码验证，角色路由
- **岗位管理**: 分角色获取岗位、按状态过滤
- **申请处理**: 提交申请（上限 3 个）、更新申请状态、工作量检查（TA 最多被 3 个岗位录取）
- **AI 功能**: `calculateFitScore()` 基于关键词的技能匹配度计算，`getMissingSkills()` 识别缺失技能
- **搜索**: 大小写不敏感的子串匹配
- **通知**: 创建、查询、已读标记

### 4. Storage 层

[JsonDataStore.java](../../src/main/java/com/group91/tars/storage/JsonDataStore.java) — 单例模式，所有 CRUD 操作：
- 使用 Gson 序列化/反序列化
- 自动初始化种子数据（3 个 TA 档案、3 个岗位、6 个申请、3 个账号、10 条通知）
- CV 文件存储在 `uploads/cv/`

### 5. 横切关注点

| 类 | 用途 |
|---|---|
| [BasePageServlet.java](../../src/main/java/com/group91/tars/servlet/BasePageServlet.java) | 所有 Servlet 的基类：认证检查(`requireAuthenticated`/`requireRole`)、i18n注入、Flash消息 |
| [LocaleFilter.java](../../src/main/java/com/group91/tars/i18n/LocaleFilter.java) | `@WebFilter("/*")` — 拦截所有请求，处理 `lang` 参数切换中英语言 |
| [I18n.java](../../src/main/java/com/group91/tars/i18n/I18n.java) | 国际化工具，`messages_en.properties` / `messages_zh.properties` |
| [FlashScope.java](../../src/main/java/com/group91/tars/web/FlashScope.java) | 重定向闪存消息（Session 级别的 disposable 消息） |
| [AppBootstrapListener.java](../../src/main/java/com/group91/tars/web/AppBootstrapListener.java) | `@WebListener` — 应用启动时初始化数据存储 |

---

## Servlet URL 映射表

| URL Pattern | Servlet 类 | 需要角色 |
|---|---|---|
| `/login` | LoginServlet | 无（公开） |
| `/logout` | LogoutServlet | 无（公开） |
| `/gateway` | HomeServlet | 任意已登录 |
| `/ta/dashboard` | TaDashboardServlet | TA |
| `/ta/jobs` | TaJobsServlet | TA |
| `/ta/job` | TaJobDetailServlet | TA |
| `/ta/applications` | TaApplicationsServlet | TA |
| `/ta/profile` | TaProfileServlet | TA |
| `/mo/dashboard` | MoDashboardServlet | MO |
| `/mo/review` | MoReviewServlet | MO |
| `/mo/jobs/edit` | MoJobEditServlet | MO |
| `/admin/workload` | AdminWorkloadServlet | ADMIN |
| `/search` | SearchServlet | 任意已登录 |
| `/inbox` | InboxServlet | 任意已登录 |
| `/ai/assist` | AiAssistServlet | 任意已登录 |

---

## 关键业务规则

1. **TA 最多提交 3 个申请** (`MAX_APPLICATIONS = 3`)
2. **TA 最多被 3 个岗位录取** (`MAX_ACCEPTED_JOBS = 3`)，超过则触发过载告警
3. **CV 上传限制**: 仅允许 PDF、DOC、DOCX 格式
4. **搜索**: 大小写不敏感的简单子串匹配，TA 只能搜开放岗位和自己的申请
5. **AI 匹配**: 基于技能关键词的简单百分比计算（技能交集 / 岗位总技能），非真实 ML 模型

---

## 架构优点与局限

**优点**:
- 清晰的三层分离，Servlet 不直接操作数据
- 基类 `BasePageServlet` 统一处理认证/授权/i18n，减少重复代码
- JSON 文件存储，零配置，适合演示和小规模数据

**局限**:
- 无真实数据库，JSON 文件读写线程不安全，并发下会丢数据
- 硬编码的 `CURRENT_TA_ID` / `CURRENT_MO_ID`，不支持真实多用户
- AI 匹配是简单的关键词比对，不是真正的智能推荐
- 密码明文存储（在 seed data 中），无加密/哈希
