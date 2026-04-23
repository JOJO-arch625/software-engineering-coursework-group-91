# 前端测试使用指南

## 说明

前端测试和后端测试现在已经分开：

- 前端测试：`test:frontend`
- 后端测试：`test:backend`

前端测试专门验证页面渲染、角色跳转、会话保持和页面展示结果。

## 当前目录结构

```text
test/
  package.json
  FRONTEND-TEST-DOCUMENT.md
  TEST-USAGE-GUIDE.md
  BACKEND-TEST-DOCUMENT.md
  BACKEND-TEST-USAGE-GUIDE.md
  run-frontend-tests.mjs
  run-backend-contract-tests.mjs
  helpers/
    htmlAssertions.mjs
    miniTestRunner.mjs
    projectFiles.mjs
    sessionClient.mjs
    testConfig.mjs
  frontend/
    login-and-routing.test.mjs
    ta-pages.test.mjs
    mo-admin-pages.test.mjs
  backend/
    contracts/
    java/
```

## 运行前准备

前端测试需要先启动项目服务。  
默认目标地址为：

```text
http://127.0.0.1:8080
```

如果项目跑在其他端口，可以通过环境变量 `FRONTEND_BASE_URL` 指定。

## 常用命令

在项目根目录执行：

```powershell
npm --prefix test run test:frontend
```

查看更详细输出：

```powershell
npm --prefix test run test:frontend:verbose
```

运行全部前后端测试：

```powershell
npm --prefix test run test:all
```

## 自定义地址运行

PowerShell 示例：

```powershell
$env:FRONTEND_BASE_URL = "http://127.0.0.1:8081"
npm --prefix test run test:frontend
```

## 前端测试实际会做什么

- 向正在运行的 Web 应用发请求
- 保存登录后的 Session Cookie
- 验证页面跳转是否正确
- 验证权限拦截是否正确
- 检查 HTML 中的关键页面文案是否出现

测试内部使用的是一个轻量自定义 runner，因此在受限环境里也能尽量稳定执行。

## 注意事项

- 当前前端测试尽量保持只读，不主动修改业务 JSON 数据。
- 登录会创建正常的会话状态，但不会修改核心业务数据。
- 如果服务没有启动，测试会在开始阶段直接给出明确错误提示。

## 失败排查顺序

1. 先确认项目服务已经启动。
2. 确认端口和 `FRONTEND_BASE_URL` 是否一致。
3. 确认 `data/accounts.json` 中的演示账号仍和 `helpers/testConfig.mjs` 一致。
4. 检查 JSP 页面标题或关键文案是否被修改。

## 如何扩展前端测试

在 `test/frontend/` 下新增 `.test.mjs` 文件，然后在 `run-frontend-tests.mjs` 中注册。

建议模式：

1. 创建 `SessionClient`
2. 使用对应角色登录
3. 调用 `getHtml()` 或 `request()`
4. 断言状态码、跳转路径和可见文案

## 建议后续增强

- 为写操作场景增加数据重置辅助脚本
- 后续增加真正浏览器级 UI 自动化
- 和后端 service 层测试一起形成完整回归链路
