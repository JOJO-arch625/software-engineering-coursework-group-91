# I18N 中英文切换功能实现文档
## 业务逻辑
"flash.profile.saved"
        ↓
BasePageServlet.flashI18n(...)
        ↓
I18n.t("flash.profile.saved")
        ↓
ResourceBundle.getString("flash.profile.saved")
        ↓
src/main/resources/i18n/messages_en.properties
或
src/main/resources/i18n/messages_zh.properties

示例：
return OperationResult.success(
    "flash.profile.saved",
    "Profile saved successfully."
);
flashI18n(request, "success", result.getMessageKey());
## 概述

在 TARS 项目中实现了中英文界面切换功能，用户点击顶部栏的 **"中文"** / **"EN"** 按钮即可在两种语言之间切换。语言偏好保存在 session 中，跨页面持久。

## 架构设计

```
用户点击 ?lang=zh 或 ?lang=en
       ↓
LocaleFilter (@WebFilter "/*") 拦截请求
       ↓
将 Locale 存入 HttpSession["locale"]
       ↓
BasePageServlet.preparePage() 从 session 取 locale
       ↓
创建 I18n(locale) 实例，set 到 request["i18n"]
       ↓
JSP 中 <%= i18n.t("key") %> 取翻译文本
```

## 新建文件

### 1. `src/main/java/com/group91/tars/i18n/I18n.java`

翻译工具类，包装 Java `ResourceBundle`：

- `t(String key)` — 根据 key 取翻译文本，缺失时返回 `!KEY!` 作为可见提示
- `t(String key, Object... args)` — 支持 `{0}`, `{1}` 参数替换（基于 `MessageFormat`）
- `getLanguage()` — 返回 `"en"` 或 `"zh"`，用于 HTML `<html lang="...">` 属性

**关键实现细节**：自定义了 `ResourceBundle.Control`，用 **UTF-8 Reader** 读取 `.properties` 文件，而非 Java 默认的 ISO-8859-1。这使得中文可直接写在 properties 文件中，无需转成 `\uXXXX` 格式。

```java
private static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                     ClassLoader loader, boolean reload)
            throws ... {
        if ("java.properties".equals(format)) {
            String resourceName = toResourceName(toBundleName(baseName, locale), "properties");
            InputStream stream = loader.getResourceAsStream(resourceName);
            if (stream != null) {
                try (Reader reader = new InputStreamReader(stream, "UTF-8")) {
                    return new PropertyResourceBundle(reader);
                }
            }
        }
        return super.newBundle(baseName, locale, format, loader, reload);
    }
};
```

### 2. `src/main/java/com/group91/tars/i18n/LocaleFilter.java`

`@WebFilter("/*")` 过滤器，在所有请求到达 Servlet 前执行：

- 检测 URL 参数 `?lang=en` 或 `?lang=zh`
- 将对应的 `java.util.Locale` 存入 `HttpSession["locale"]`
- 无参数时保持 session 中已有 locale，默认为 English

### 3. `src/main/resources/i18n/messages_en.properties`

英文翻译文件，约 250+ key。命名规范为层级点分法：

```
brand.*       — 品牌文字（标题、副标题）
nav.*         — 侧边栏导航标签
topbar.*      — 顶部栏元素
metric.*      — 指标卡片标签
status.*      — 状态显示值（Open, Closed, Submitted...）
flash.*       — 闪存提示消息
login.*       — 登录页面
gateway.*     — 入口/概览页面
ta.*          — TA 相关页面
mo.*          — MO 相关页面
admin.*       — 管理员相关页面
inbox.*       — 通知收件箱
search.*      — 搜索页面
ai.*          — AI 助手页面
common.*      — 通用按钮/标签
```

### 4. `src/main/resources/i18n/messages_zh.properties`

中文翻译文件，key 与 `messages_en.properties` 完全一致，值为中文翻译。

## 修改的核心文件

### BasePageServlet.java

- `preparePage()` 中新增：从 session 获取 locale → 创建 I18n 实例 → 存入 request attribute
- 新增 `flashI18n()` 方法：将闪存消息从硬编码英文改为 i18n key
- 三个 `resolveMetric*Label()` 方法：返回值从硬编码文本改为 i18n key（如 `"metric.open-jobs"`）

### pageStart.jspf（共享外壳，影响所有页面）

- `<html lang="en">` → `<html lang="<%= lang %>">`
- 侧边栏导航标签 → `i18n.t("nav.xxx")`
- 顶部栏所有文本 → i18n key
- 指标卡片标签 → `i18n.t(topMetricOneLabel)`
- 新增语言切换按钮：

```jsp
<a class="lang-toggle" href="?lang=<%= "zh".equals(lang) ? "en" : "zh" %>">
    <%= "zh".equals(lang) ? "EN" : "中文" %>
</a>
```

### 所有 JSP 页面（14个）

每个页面中所有硬编码英文文本替换为 `i18n.t("key")` 调用。状态值通过动态 key 映射翻译：

```jsp
<%-- 数据中存储的是英文 "Open"，JSP 展示时翻译 --%>
<%= i18n.t("status." + job.getStatus().toLowerCase().replace(" ", "-")) %>
```

CSS class 比较（如 `"Open".equals(job.getStatus())`）保持原样不动，仅翻译展示文本。

### CSS

`.lang-toggle` — 圆角药丸按钮样式，白色文字 + 透明背景，hover 时高亮。

## 添加新翻译的步骤

1. 在 `messages_en.properties` 和 `messages_zh.properties` 中添加新 key 和对应翻译
2. 在 JSP 中使用 `<%= i18n.t("your.new.key") %>` 替代硬编码文本
3. 如需参数化消息，使用 `<%= i18n.t("key", arg1, arg2) %>`

## 注意事项

- **状态值**（Open/Closed/Submitted 等）在 JSON 数据文件中保持英文不变，仅在 JSP 显示时通过 `i18n.t("status.xxx")` 翻译
- **CSS class 比较**（如判断状态来添加样式类）使用原始英文值，不要翻译
- **用户数据**（姓名、学号、技能等）不翻译
- **通知消息内容**暂保持英文，可作为后续迭代项
- **闪存消息**（Flash messages）来自 `TarsService` 的部分仍为英文，`LoginServlet` 和 `LogoutServlet` 已改为 i18n key

## 验证方式

1. `mvn package` 构建
2. `mvn org.apache.tomcat.maven:tomcat7-maven-plugin:2.2:run` 启动
3. 访问 `http://localhost:8080/login`，点击顶部栏 **中文** / **EN** 按钮切换
4. 登录各角色账号，检查所有页面文字是否正常切换
5. 任何显示 `!KEY!` 的地方表示有 key 缺失，需在 properties 文件中补上
