# TARS（Teaching Assistant Recruitment System） 前端架构总结

## 概述

TARS 前端是**纯原生 Web 技术栈**：JSP + 手写 CSS + 原生 JS，无任何框架依赖（无 Bootstrap、无 React、无 jQuery）。

---

## 文件清单

| 文件 | 路径 | 说明 |
|------|------|------|
| `app.css` | `assets/styles/app.css` | **唯一的样式文件**，1404 行 |
| `pageStart.jspf` | `WEB-INF/jsp/fragments/` | **共享外壳上半部分**（侧边栏+顶栏+指标横幅） |
| `pageEnd.jspf` | `WEB-INF/jsp/fragments/` | **共享外壳下半部分**（滚动按钮+内联 JS） |
| `login.jsp` | `WEB-INF/jsp/` | **独立登录页**，不使用碎片 |
| `gateway.jsp` | `WEB-INF/jsp/` | 角色总览页 |
| `ta/dashboard.jsp` | `WEB-INF/jsp/ta/` | TA 仪表盘 |
| `ta/profile.jsp` | `WEB-INF/jsp/ta/` | TA 资料编辑 + CV 上传 |
| `ta/jobs.jsp` | `WEB-INF/jsp/ta/` | TA 岗位浏览 |
| `ta/job-detail.jsp` | `WEB-INF/jsp/ta/` | 岗位详情 + 申请 |
| `ta/applications.jsp` | `WEB-INF/jsp/ta/` | 申请状态追踪 |
| `mo/dashboard.jsp` | `WEB-INF/jsp/mo/` | MO 仪表盘 |
| `mo/job-editor.jsp` | `WEB-INF/jsp/mo/` | MO 岗位创建/编辑 |
| `mo/review.jsp` | `WEB-INF/jsp/mo/` | MO 申请人审核 |
| `admin/workload.jsp` | `WEB-INF/jsp/admin/` | Admin 工作量监控 |
| `inbox.jsp` | `WEB-INF/jsp/` | 通知收件箱 |
| `search.jsp` | `WEB-INF/jsp/` | 全局搜索 |
| `ai/assist.jsp` | `WEB-INF/jsp/ai/` | AI 助手面板 |

**总结**: 1 个 CSS + 2 个 JSP 碎片 + 14 个 JSP 页面 + 0 个 JS 文件。

---

## 页面组成模式

### 内部页面（碎片包裹模式）

```
pageStart.jspf ── 打开 <html>, <head>, <body>, 侧边栏, 顶栏, 指标横幅
   [页面专属内容] <section class="view active"> ... </section>
pageEnd.jspf ── 滚动按钮, </main>, </html>, 内联 JS
```

通过 `<%@ include file="..." %>` 静态引入，碎片中的 scriptlet 变量与父页面共享。

### 登录页（独立模式）

`login.jsp` 完全自闭合，有自己的 `<html>`, `<head>`, `<body class="login-page">`，不使用碎片。因为未登录用户不需要侧边栏导航和指标卡片。

---

## 页面布局结构

```
┌──────────────────────────────────────────────────────────┐
│ .app-shell (CSS Grid: 272px + 1fr)                       │
│ ┌─────────────┬──────────────────────────────────────────┐│
│ │ .sidebar    │ .main-panel                              ││
│ │  magenta    │ ┌────────────────────────────────────────┐││
│ │  gradient   │ │ .topbar (sticky, navy blue)            │││
│ │             │ │ [hamburger] [ISTARS] [workspace] [user]│││
│ │ [ISTARS]    │ │            [lang-toggle] [logout]      │││
│ │ [nav links] │ ├────────────────────────────────────────┤││
│ │             │ │ .summary-banner (3 metric cards)       │││
│ │             │ ├────────────────────────────────────────┤││
│ │             │ │ [FLASH MESSAGE]                        │││
│ │             │ │ ┌──────────────────────────────────────┐│││
│ │             │ │ │ .view.active                         ││││
│ │             │ │ │   (page-specific content)            ││││
│ │             │ │ └──────────────────────────────────────┘│││
│ │             │ │ [scroll-fab]                            │││
│ │             │ └────────────────────────────────────────┘││
│ └─────────────┴──────────────────────────────────────────┘│
└──────────────────────────────────────────────────────────┘
```

侧边栏可折叠：`body.nav-collapsed` 时从 272px 缩至 92px（仅显示图标），状态存 `localStorage`。

---

## 设计系统

### 配色

| 用途 | 颜色 |
|------|------|
| 主文字 | `#173a7a` (深海军蓝) |
| 次要文字 | `#5f6f8d` |
| 品牌强调色 | `#c4007a` (品红) |
| 成功 | `#2e7d5b` (绿) |
| 危险 | `#b14258` (红) |
| 警告 | `#a46f25` (琥珀黄) |
| 页面背景 | 渐变的 `#eef2f8` → `#f5f7fb` |
| 侧边栏 | 品红渐变 |
| 顶栏 | 深蓝实底 |

### 排版

| 层级 | 字体 | 字号 |
|------|------|------|
| 标题字体 | Space Grotesk | — |
| 正文字体 | IBM Plex Sans | 15px |
| 页面标题 | — | 38px |
| 卡片标题 | — | 20px |
| 导航标签 | — | 15px |
| 表格表头 | — | 13px, 大写 |
| 小标签 | — | 12px, 大写 |

### 按钮三层级

- `.primary-button` — 品红实底，用于 CTA
- `.secondary-button` — 淡蓝半透明底，用于次要操作
- `.ghost-button` — 白底品红边框，用于最低优先级

### 布局

- **主布局**: CSS Grid（272px 侧边栏 + 流动内容区）
- **卡片网格**: `.two-col` / `.three-col` / `.one-col`
- **表单网格**: `form-grid` (2 列)
- **弹性盒**: 用于顶栏、按钮行、状态条、卡片内部

### 响应式

| 断点 | 触发 |
|------|------|
| ≤1100px | 侧边栏坍缩、网格变单列、顶栏堆叠 |
| ≤720px | 内边距减小、hero 卡片纵向排列、字号缩小 |

---

## JavaScript（全部内联在 pageEnd.jspf）

两个功能，无外部依赖：

1. **侧边栏折叠** — 点击汉堡按钮切换 `nav-collapsed`，状态持久化到 `localStorage`
2. **滚动悬浮按钮** — 点击滚动 0.82 倍视口高度，到达底部时变为"回到顶部"

所有 JS 总共约 50 行，原生写法。

---

## 关键设计决策

1. **无框架** — 纯手写 CSS + 原生 JS，符合课程"禁止复杂框架"的要求
2. **碎片包裹模式** — `pageStart.jspf` + `pageEnd.jspf` 提供统一外壳，14 个内容页面只写核心区域
3. **login.jsp 独立** — 登录页不需要导航外壳，独立自闭合
4. **CSS Custom Properties** — 14 个设计令牌统一管理颜色和阴影
5. **CSS Grid 为主 + Flexbox 为辅** — Grid 负责页面级布局，Flexbox 负责组件内排列
6. **无外部字体文件** — 使用 Google Fonts CDN 加载 `Space Grotesk` 和 `IBM Plex Sans`
7. **i18n 已集成** — 通过 `I18n` 工具类 + 属性文件实现中英文切换，语言切换按钮在顶栏
