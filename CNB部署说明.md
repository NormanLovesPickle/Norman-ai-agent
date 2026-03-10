# CNB 部署说明

## 1. 在密钥仓库创建 env.yml

在 CNB 密钥仓库 `pickle-ai-agent-key` 的 Web 界面新建 `env.yml`，**完整复制**本地 `env.yml` 内容粘贴保存。

**必须包含：** `allow_slugs`、`allow_events`、`allow_branches`；`SSH_KEY`（prepare 阶段用，单行双引号含 `\n`）；`PRIVATE_KEY` 同格式；勿用多行块。

**env_ssh.yml**：仅 deploy 插件引用，需 `allow_images: "tencentcom/ssh"`；prepare 阶段改用 env.yml 获取 SSH_KEY。

## 2. 提交代码

确保以下文件在 master/main 分支：
- `.cnb.yml`
- `.cnb/web_trigger.yml`
- `Dockerfile`
- `settings.xml`

## 3. 触发构建

在 CNB 项目页面点击「构建 Norman AI Agent」。

## 4. 访问服务

部署成功后访问：`http://服务器IP:8123/api/doc.html`
