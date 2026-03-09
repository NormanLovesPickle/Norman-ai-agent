# CNB 部署说明

## 1. 在 CNB 创建密钥仓库

访问 https://cnb.cool/ 创建密钥仓库，上传环境配置文件：

**test_env.yml / prod_env.yml 示例：**

```yaml
BACKEND_SSH_IP: "你的服务器IP"
BACKEND_SSH_PORT: "22"
PRIVATE_KEY: |
  -----BEGIN RSA PRIVATE KEY-----
  你的SSH私钥内容
  -----END RSA PRIVATE KEY-----

DOCKER_TOKEN: "CNB Docker 仓库令牌"
SPRING_PROFILES_ACTIVE: "prod"
DASHSCOPE_API_KEY: "sk-你的阿里云百炼API密钥"
```

## 2. 修改 web_trigger 配置

编辑 `.cnb/web_trigger.yml`，将 `pipeline_env` 的 `options` 中 `value` 替换为你们密钥仓库的实际 URL，例如：

```
https://cnb.cool/你的组/密钥仓库名/secret/-/raw/main/test_env.yml
https://cnb.cool/你的组/密钥仓库名/secret/-/raw/main/prod_env.yml
```

## 3. 提交到 master 分支

确保以下文件在 master 分支：
- `.cnb.yml`
- `.cnb/web_trigger.yml`
- `Dockerfile`
- `settings.xml`

## 4. 触发构建

在 CNB 项目页面点击「构建 Norman AI Agent」，选择环境后执行。

## 5. 访问服务

部署成功后访问：`http://服务器IP:8123/api/doc.html`

**说明：** 当前 CNB 配置仅部署后端。前端需单独部署（如 Nginx 静态托管），并将 API 请求代理到后端。
