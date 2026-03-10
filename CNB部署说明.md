# CNB 部署说明

## 1. 密钥仓库配置

在 CNB 密钥仓库 `pickle-2025/pickle-ai-agent-key` 的 Web 界面创建两个文件：

### env.yml（无 allow_images，供流水线 script 阶段用）

```yaml
allow_slugs: "pickle-2025/norman-ai-agent"
allow_events: "web_trigger_one"
allow_branches: "main"
SPRING_PROFILES_ACTIVE: "prod"
BACKEND_SSH_IP: "你的服务器IP"
BACKEND_SSH_PORT: "22"
DOCKER_TOKEN: "CNB Docker 令牌"
DASHSCOPE_API_KEY: "sk-你的阿里云API密钥"
```

### env_ssh.yml（含 allow_images，仅 deploy 插件用）

```yaml
allow_slugs: "pickle-2025/norman-ai-agent"
allow_events: "web_trigger_one"
allow_branches: "main"
allow_images: "tencentcom/ssh"
SPRING_PROFILES_ACTIVE: "prod"
BACKEND_SSH_IP: "你的服务器IP"
BACKEND_SSH_PORT: "22"
SSH_KEY: |
  -----BEGIN RSA PRIVATE KEY-----
  你的私钥内容（每行2空格缩进）
  -----END RSA PRIVATE KEY-----
DOCKER_TOKEN: "CNB Docker 令牌"
DASHSCOPE_API_KEY: "sk-你的阿里云API密钥"
```

**关键**：SSH_KEY 必须用多行块格式（`|` + 2空格缩进），与 tencentcom/ssh 插件文档一致；deploy 使用 `key: $SSH_KEY` 直接传私钥文本。

## 2. 提交代码

确保 `.cnb.yml`、`.cnb/web_trigger.yml`、`Dockerfile`、`settings.xml` 在 main 分支。

## 3. 触发构建

在 CNB 分支详情页点击「构建 Norman AI Agent」。

## 4. 访问服务

`http://服务器IP:8123/api/doc.html`
