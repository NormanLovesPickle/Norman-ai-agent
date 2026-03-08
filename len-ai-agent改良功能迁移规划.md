# len-ai-agent 改良功能迁移规划

> 将 len-ai-agent 的改良模块迁移到 norman-ai-agent 的规划文档

---

## 一、依赖迁移

| 依赖 | 说明 | 优先级 |
|------|------|--------|
| spring-boot-starter-freemarker | 模板引擎 | 中 |
| spring-boot-starter-mail | 邮件发送 | 低 |
| mysql-connector-j | MySQL 驱动 | 高 |
| mybatis-plus-spring-boot3-starter | MyBatis-Plus | 高 |
| spring-ai-tika-document-reader | Tika 文档读取 | 中 |
| github-api (kohsuke) | GitHub 文档加载 | 低 |

---

## 二、新增工具（Tools）

| 工具类 | 功能 | 依赖 |
|--------|------|------|
| HtmlGenerationTool | 生成带动效、响应式、主题切换的 HTML，支持 `{{embed:URL}}` 内嵌 | FileConstant |
| DateTimeTool | 获取当前时间、日期差、未来日期 | 无 |
| ImageSearchTool | Pexels 图片搜索 | pexels.api-key |
| EmailSendingTool | 邮件发送（文本/HTML/附件） | spring.mail 配置 |
| DatabaseOperationTool | SQL 查询/插入/更新/删除 | JdbcTemplate |

**ToolRegistration 修改**：在 `allTools()` 中注册上述工具。

---

## 三、新增 Advisor

| Advisor | 功能 | 资源 |
|---------|------|------|
| ProhibitedWordAdvisor | 违禁词检测，请求前拦截 | prohibited-words.txt |

**实现要点**：
- 实现 `CallAroundAdvisor`、`StreamAroundAdvisor`
- `getOrder()` 返回 -100 保证优先执行
- 从 ClassPathResource 加载违禁词列表

---

## 四、对话记忆（ChatMemory）

| 实现 | 存储 | 说明 |
|------|------|------|
| MySQLChatMemory | MySQL | 逻辑删除，支持 `get(conversationId, lastN)` |
| MybatisPlusChatMemory | MySQL | 通过 ChatMemoryService 操作 |

**需要**：
- chatmemory 表结构
- ChatMemoryService、ChatMemoryMapper
- DataSource 配置

**LoveApp 修改**：增加 `CHAT_MEMORY_RETRIEVE_SIZE_KEY` 参数（如 10）。

---

## 五、新增 Controller

| Controller | 路径 | 功能 |
|------------|------|------|
| QwenController | /api/qwen/* | 文本/流式/图像/视频/音频对话 |
| FileResourceController | /api/files/* | PDF/HTML/文件列表、下载、内联展示 |

**QwenController 依赖**：
- QwenService
- QwenTextRequest、QwenMultiModalRequest、QwenResponse

---

## 六、新增 Agent

| Agent | 路径 | 用途 |
|-------|------|------|
| QuizAssistant | /ai/quiz/chat | 答题/测评分析助手 |
| LenHealthAssistant | /ai/health/chat | 健康助手 |

---

## 七、RAG 文档加载

| 新增 | 功能 |
|------|------|
| GitHubDocumentLoader | 从 GitHub 仓库加载文档，支持目录、分支回退 |

---

## 八、LoveApp 修改项

| 项目 | 修改内容 |
|------|----------|
| System Prompt | 替换为更详细的「恋爱大师·情感导航员」描述 |
| Advisor 链 | 增加 ProhibitedWordAdvisor |
| 工具调用 | `toolCallbacks` → `tools` |
| 记忆参数 | 增加 `CHAT_MEMORY_RETRIEVE_SIZE_KEY` |

---

## 九、资源文件

| 文件 | 说明 |
|------|------|
| prohibited-words.txt | 违禁词列表，放 `src/main/resources/` |
| ChatMemoryMapper.xml | MyBatis 映射，放 `src/main/resources/mapper/` |

---

## 十、配置迁移

| 配置项 | 说明 |
|--------|------|
| spring.datasource.mysql | MySQL 数据源 |
| spring.datasource.postgres | PostgreSQL 数据源（PgVector） |
| spring.mail.* | 邮件发送 |
| pexels.api-key | 图片搜索（可选） |

---

## 十一、迁移顺序建议

1. **Phase 1 - 基础**
   - DataSourceConfig（多数据源）
   - prohibited-words.txt + ProhibitedWordAdvisor
   - LoveApp 修改（Advisor、tools、记忆参数）

2. **Phase 2 - 工具**
   - DateTimeTool
   - HtmlGenerationTool
   - FileResourceController

3. **Phase 3 - 高级工具**
   - EmailSendingTool
   - DatabaseOperationTool
   - ImageSearchTool

4. **Phase 4 - 持久化**
   - MySQLChatMemory / MybatisPlusChatMemory
   - ChatMemoryService、Mapper、表结构

5. **Phase 5 - 扩展**
   - QwenController + QwenService
   - QuizAssistant、LenHealthAssistant
   - GitHubDocumentLoader

---

## 十二、注意事项

- norman-ai-agent 使用 Spring AI 1.0.0，len-ai-agent 使用 1.0.0-M6，API 可能有差异（如 `toolCallbacks` vs `tools`）
- 包名保持 `com.norman.normanaiagent`，迁移时需替换 `com.lenyan.lenaiagent`
- 数据库迁移需先建表，再启用 MySQLChatMemory
