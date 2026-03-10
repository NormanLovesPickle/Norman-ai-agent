# CNB 部署说明

## 1. 密钥仓库配置

在 CNB 密钥仓库 `pickle-2025/pickle-ai-agent-key` 的 Web 界面创建 **env.yml**（仅此一个文件）：

```yaml
allow_slugs: "pickle-2025/norman-ai-agent"
allow_events: "web_trigger_one"
allow_branches: "main"
SPRING_PROFILES_ACTIVE: "prod"
BACKEND_SSH_IP: "你的服务器IP"
BACKEND_SSH_PORT: "22"
SSH_KEY: "-----BEGIN RSA PRIVATE KEY-----\n你的私钥内容\n-----END RSA PRIVATE KEY-----"
DOCKER_TOKEN: "CNB Docker 令牌"
DASHSCOPE_API_KEY: "sk-你的阿里云API密钥"
```

**关键**：deploy 使用 alpine+openssh 执行 ssh，不再用 tencentcom/ssh 插件；SSH_KEY 用单行双引号（`\n` 表示换行）。

## 2. 提交代码

确保 `.cnb.yml`、`.cnb/web_trigger.yml`、`Dockerfile`、`settings.xml` 在 main 分支。

## 3. 触发构建

在 CNB 分支详情页点击「构建 Norman AI Agent」。

## 4. 访问服务

`http://服务器IP:8123/api/doc.html`
