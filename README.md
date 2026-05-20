# software-engineering-coursework-group-91

## Team Members
- JOJO-arch625: 231224859 (Leader)
- Chencc7002: 231224653 (Member)
- lidarou: 231225018 (Member)
- ElanusCaeruleus9: 231224930 (Member)
- Boson-Lyu: 231224815 (Member)
- lswsb: 2024018006 (Support TA)

## directory structure
- coursework: 文书记录目录
- data: json数据库
- src: 前后端源码
- test:测试代码
- uploads:上传cv等

## 运行必要环境
- javajdk8 - 推荐使用openjdk
- maven

attention：must confirm 环境变量中有maven的bin以及java的bin

## 构建项目
在项目根目录执行：

```powershell
mvn -q "-Dmaven.repo.local=.m2/repository" -DskipTests package
```

预期结果：
- 构建成功
- 生成 WAR 文件：`target/ta-recruitment-system.war`

## 本地运行
推荐在 PowerShell 中执行下面这条命令：

```powershell
mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run "-Dmaven.repo.local=.m2\repository"
```

说明：
- 确保mvnbin已经在环境变量中配置
- `-Dmaven.repo.local` 需要带引号，避免 PowerShell 把参数拆坏。

启动成功后可在浏览器访问：

- `http://127.0.0.1:8080/login`
- 或 `http://localhost:8080/login`


## Current Implementation Docs
- local run guide: `coursework/docs/implementation/LOCAL-RUNNING-GUIDE.md`
- implemented features and TODO scope: `docs/implementation/FEATURE-STATUS.md`
- sprint handoff: `coursework/docs/implementation/SPRINT-2-HANDOFF.md`

## AI Agent Configuration

The TA recruitment system includes advisory AI agents for TA fit advice, MO candidate summaries, and Admin workload advice.

```env
AI_MODE=auto
AI_TOOL_CALLING_ENABLED=false
LLM_API_KEY=
LLM_BASE_URL=https://aihubmix.com/v1
LLM_MODEL=gpt-4.1-mini-free
```

- `AI_MODE=local` runs deterministic local AI only.
- `AI_MODE=auto` tries the LLM when `LLM_API_KEY` is available and falls back to local rules if unavailable or failed.
- `AI_MODE=llm` requires the LLM and reports an error state if the request cannot complete.
- `AI_TOOL_CALLING_ENABLED=true` enables the tool-calling agent loop for `/ai/assist/chat` and LLM-backed TA/MO advisory summaries.
- `OPENAI_API_KEY`, `OPENAI_BASE_URL`, and `OPENAI_MODEL` are also accepted as compatibility aliases.

PDF CV analysis uses Apache PDFBox. Only PDF CV content is analysed in v1, and extracted CV text is not stored in JSON.

For local development, copy `.env.example` to `.env` and fill in `LLM_API_KEY`. The app reads `.env` from the project root on startup. Real system environment variables still take priority over `.env` values.

## 中文实现文档
- 本地运行说明：`coursework/docs/implementation/LOCAL-RUNNING-GUIDE.zh-CN.md`
- 已实现功能与 TODO 说明：`coursework/docs/implementation/FEATURE-STATUS.zh-CN.md`
- 阶段性中文总结报告：`coursework/docs/implementation/WORKSPACE-SUMMARY.zh-CN.md`
