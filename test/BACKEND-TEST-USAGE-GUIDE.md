# 后端测试使用指南

## 说明

后端测试和前端测试已经分开：

- 前端测试：验证页面渲染、跳转、会话和角色页面展示
- 后端测试：验证模型对象、后端规则契约、存储初始化和路由保护

## 可用命令

在项目根目录执行：

```powershell
npm --prefix test run test:backend
```

只运行后端契约测试：

```powershell
npm --prefix test run test:backend:contracts
```

只运行后端 Java 模型测试：

```powershell
npm --prefix test run test:backend:java
```

运行全部前后端测试：

```powershell
npm --prefix test run test:all
```

## 每条命令分别做什么

### `test:backend:java`

这条命令会：

1. 编译 `src/main/java/com/group91/tars/model/` 下的模型源码
2. 编译 `test/backend/java/src/` 下的 Java 测试源码
3. 运行 `BackendModelTestMain`

### `test:backend:contracts`

这条命令会：

1. 读取 `TarsService.java`
2. 读取 `JsonDataStore.java`
3. 读取关键 servlet 源码
4. 读取 `data/*.json` 种子数据
5. 校验核心后端规则是否仍满足预期

## 运行前置条件

### 后端 Java 模型测试

- 需要本机可用 `javac`
- 需要本机可用 `java`

### 后端契约测试

- 需要本机可用 `node`
- 不需要启动 Web 服务

## 常见失败排查

### 1. `javac` 不存在

说明本机没有安装 JDK 或环境变量未配置。  
请先确认：

```powershell
javac -version
java -version
```

### 2. 契约测试提示常量或规则不匹配

通常表示有人修改了后端源码中的业务规则，例如：

- 最大申请数量
- 最大录取数量
- 登录跳转路径
- 权限保护逻辑

这时应先确认修改是否是有意为之。

### 3. 种子数据测试失败

说明 `data/accounts.json`、`data/job-postings.json`、`data/applications.json` 或 `data/ta-profiles.json` 被改动后不再满足当前系统演示要求。

## 如何扩展后端测试

### 新增 Java 模型测试

把新的测试方法加到：

- `test/backend/java/src/com/group91/tars/tests/BackendModelTestMain.java`

### 新增后端契约测试

在下面目录增加新的 `.test.mjs` 文件：

- `test/backend/contracts/`

然后在：

- `test/run-backend-contract-tests.mjs`

里注册新的测试套件。

## 推荐扩展顺序

1. 先补 `TarsService` 的业务规则单测
2. 再补 `JsonDataStore` 的读写测试
3. 最后补 servlet 级集成测试
