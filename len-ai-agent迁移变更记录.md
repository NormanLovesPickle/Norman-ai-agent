# len-ai-agent 迁移变更记录

## Phase 1-4（已完成，共 13 次提交）

| 提交 | 变更内容 |
|------|----------|
| chore: 添加迁移所需 Maven 依赖 | spring-boot-starter-freemarker, spring-boot-starter-mail, mysql-connector-j, mybatis-plus, spring-ai-tika-document-reader, github-api |
| feat: 添加多数据源配置 | DataSourceConfig, mysql/postgres 数据源 |
| feat: 添加违禁词检测 Advisor | ProhibitedWordAdvisor, prohibited-words.txt |
| feat: LoveApp 接入违禁词检测与记忆参数 | ProhibitedWordAdvisor, CHAT_MEMORY_RETRIEVE_SIZE_KEY |
| feat: 添加 DateTimeTool | DateTimeTool |
| feat: 添加 HtmlGenerationTool | HtmlGenerationTool |
| feat: 添加 FileResourceController | FileResourceController |
| feat: 添加 EmailSendingTool | EmailSendingTool |
| feat: 添加 DatabaseOperationTool | DatabaseOperationTool |
| feat: 添加 ImageSearchTool | ImageSearchTool |
| feat: 添加 ChatMemory 持久化层 | ChatMemoryService, ChatMemoryMapper, MybatisPlusChatMemory |
| feat: 添加 MySQL 对话记忆实现 | MySQLChatMemory |
| feat: 添加 chatmemory 表结构与数据源配置 | sql/chatmemory.sql, DataSourceConfig 调整 |

---

## Phase 5（本次完成）

### 1. QwenController + QwenService + DTOs

| 文件 | 变更 |
|------|------|
| pom.xml | 添加 rxjava2 依赖 |
| QwenConfig.java | 新增，qwen.* 配置 |
| QwenTextRequest.java | 新增，文本请求 DTO |
| QwenMultiModalRequest.java | 新增，多模态请求 DTO |
| QwenResponse.java | 新增，响应 DTO |
| QwenService.java | 新增，服务接口 |
| QwenServiceImpl.java | 新增，DashScope SDK 实现 |
| QwenController.java | 新增，/qwen 下 chat/stream-chat/image/video/audio |
| application.yml | 添加 qwen 配置段 |

**API 路径**（context-path /api）：
- POST /api/qwen/chat - 文本对话
- POST /api/qwen/stream-chat - 流式文本
- POST /api/qwen/image - 图像对话
- POST /api/qwen/video - 视频对话
- POST /api/qwen/audio - 音频对话

### 2. QuizAssistant + NormanHealthAssistant

| 文件 | 变更 |
|------|------|
| QuizAssistant.java | 新增，答题/测评分析助手 |
| NormanHealthAssistant.java | 新增，健康助手 |
| AiController.java | 新增 /ai/quiz/chat、/ai/health/chat |

**API 路径**：
- GET /api/ai/quiz/chat?message=xxx
- GET /api/ai/health/chat?message=xxx

### 3. GitHubDocumentLoader

| 文件 | 变更 |
|------|------|
| GitHubDocumentLoader.java | 新增，rag/documentreader/GitHubDocumentLoader |

**功能**：从 GitHub 仓库加载文档，支持 loadDocument(path)、loadDocuments(path)、getRepositoryInfo()，分支回退。

---

## 包名映射

| len | norman |
|-----|--------|
| com.lenyan.lenaiagent | com.norman.normanaiagent |
| LenHealthAssistant | NormanHealthAssistant |

---

## 配置说明

- `qwen.api-key`：默认从 `spring.ai.dashscope.api-key` 读取
- 多模态模型：text-model、vision-model、video-model、audio-model 见 QwenConfig 默认值
