# 本地运行说明

## 概述
本项目是一个基于 `Java Servlet/JSP` 的轻量级 TA 招聘系统课程项目。  
当前实现使用：
- `Maven` 进行构建与本地运行
- `Tomcat Maven Plugin` 作为本地开发服务器
- `JSON` 文件存储结构化数据
- 本地文件夹存储 CV 文件

本项目**不使用数据库**。

## 环境要求
- `Java 8`
- `Maven`
- 首次运行时建议有网络，以便下载 Maven 插件和依赖

## 主要目录
- 源代码：`src/main/java`
- JSP 页面：`src/main/webapp/WEB-INF/jsp`
- 样式文件：`src/main/webapp/assets/styles/app.css`
- 种子数据：`data/`
- CV 文件：`uploads/cv/`

## 构建项目
执行：

```powershell
mvn -q "-Dmaven.repo.local=.m2/repository" -DskipTests package
```

预期结果：
- 构建成功
- 生成 WAR 文件：`target/ta-recruitment-system.war`

## 本地运行
执行：

```powershell
mvn "org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run" "-Dmaven.repo.local=.m2/repository"
```

启动成功后可在浏览器访问：

- `http://127.0.0.1:8080/`
- 或 `http://127.0.0.1:8080/gateway`

## 主要访问路径
- `/gateway`
- `/ta/dashboard`
- `/ta/profile`
- `/ta/jobs`
- `/ta/job`
- `/ta/applications`
- `/mo/dashboard`
- `/mo/jobs/edit`
- `/mo/review`
- `/admin/workload`
- `/ai/assist`

## 停止本地服务
如果服务运行在当前终端中：
- 按 `Ctrl + C`

如果服务在后台运行：

```powershell
Get-Process java
Stop-Process -Id <PID>
```

## 演示数据
项目已经附带一套稳定的本地演示数据：
- `data/ta-profiles.json`
- `data/job-postings.json`
- `data/applications.json`

占位 CV 文件位于：
- `uploads/cv/`

## 常见问题
### 1. Maven 插件或依赖下载失败
通常是首次运行时网络受限导致。  
在网络可用时重新执行命令即可。

### 2. 8080 端口被占用
请先关闭旧的 Java/Tomcat 进程，再重新启动。

### 3. 数据显示不一致
请检查是否有人手动修改了以下文件：
- `data/ta-profiles.json`
- `data/job-postings.json`
- `data/applications.json`

### 4. CV 上传校验失败
当前仅接受以下文件类型：
- `.pdf`
- `.doc`
- `.docx`

## 建议演示顺序
1. 打开 `/gateway`
2. 演示 TA 流程
3. 演示 MO 流程
4. 演示 Admin 工作量页面
5. 最后展示 `/ai/assist`，并说明它只是可选增强
