# 后端测试说明

## 目标

这一轮后端测试围绕当前课程项目中最稳定、最值得优先守护的后端部分展开，重点覆盖：

- 纯 Java 模型对象行为
- 服务层中的关键业务规则契约
- 存储层初始化逻辑
- 本地 JSON 种子数据的完整性与约束
- Servlet 路由和权限保护的稳定性

## 为什么后端测试要分两层

当前工程的正式后端实现是 `Servlet + Service + JsonDataStore`，但本地环境里没有现成的 Maven 依赖缓存。  
因此这轮后端测试拆成两层，既保证可执行性，也尽量贴近真实后端逻辑：

### 1. Java 模型单元测试

目录：`test/backend/java/`

这部分直接使用 `javac + java` 编译并运行，不依赖 Maven。  
覆盖对象包括：

- `UserAccount`
- `OperationResult`
- `WorkloadSummary`
- `TAProfile`
- `JobPosting`
- `ApplicationRecord`

### 2. 后端契约测试

目录：`test/backend/contracts/`

这部分使用 Node.js 读取后端源码和种子数据文件，验证关键后端契约是否被破坏，例如：

- 最大申请数是否仍为 `3`
- 最大录取数是否仍为 `3`
- 关闭岗位后是否禁止申请
- 重复申请是否被阻止
- 超过 workload 上限时是否禁止录取
- `JsonDataStore` 是否仍初始化目录和种子数据
- 演示账号、岗位、申请状态是否仍完整
- 核心 servlet 路由和权限保护是否仍存在

## 当前文件结构

- `backend/java/src/com/group91/tars/tests/BackendModelTestMain.java`
- `backend/java/run-java-tests.ps1`
- `backend/contracts/service-rules.test.mjs`
- `backend/contracts/storage-seed-data.test.mjs`
- `backend/contracts/servlet-routing.test.mjs`
- `run-backend-contract-tests.mjs`

## 覆盖范围

### Java 模型测试

- `UserAccount.getInitials()` 的不同输入分支
- `OperationResult.success/failure()` 工厂方法
- `WorkloadSummary` 默认集合和 overload 标记
- `TAProfile`、`JobPosting`、`ApplicationRecord` 的字段读写

### 后端契约测试

- `TarsService` 中的角色跳转和业务限制
- `JsonDataStore` 中的目录初始化和种子数据约束
- `BasePageServlet` 中的登录与越权保护
- 关键 servlet 的 `@WebServlet` 路由声明

## 这套后端测试的价值

它更像一层“后端回归护栏”：

- 当组员修改业务常量时，可以尽快发现破坏
- 当路由或权限保护被改坏时，可以尽快发现
- 当演示数据被误改到不可演示状态时，可以尽快发现
- 即使当前没有完整 Maven 单测环境，也能先把后端最重要的规则守住

## 当前限制

- 还没有直接执行 `TarsService` 全量方法级单测
- 还没有引入 JUnit、Mockito 等标准 Java 测试框架
- 由于依赖和运行环境限制，`service + storage + servlet` 的全链路 Java 单测暂时没有接入

## 建议下一步

1. 等 Maven 依赖环境稳定后，补 `service` 层真正的 Java 单元测试。
2. 为 `JsonDataStore` 增加可替换的数据目录或测试夹具。
3. 将 workload、申请上限、岗位关闭规则补成可直接执行的业务单测。
4. 再往后可以加入集成测试，把 servlet 和 service 串起来验证。
