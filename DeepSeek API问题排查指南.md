# DeepSeek API 401认证错误排查指南

## 问题描述
在使用AI文章生成功能时出现以下错误：
```
org.springframework.web.client.HttpClientErrorException$Unauthorized: 401 Unauthorized
```

## 可能原因和解决方案

### 1. API密钥问题

#### 检查项目：
- API密钥是否正确配置
- API密钥是否有效（未过期/未被撤销）
- API密钥格式是否正确

#### 解决步骤：

1. **验证API密钥格式**
   - DeepSeek API密钥应该以 `sk-` 开头
   - 长度通常在40-60个字符之间
   - 当前配置的密钥：`sk-qlgrdqwbrmcaurxprhsvqqpcosnadtrfgaqrzkizobblmsqz`

2. **获取新的API密钥**
   ```
   1. 访问 https://platform.deepseek.com/
   2. 登录您的账号
   3. 进入 "API Keys" 页面
   4. 创建新的API密钥或检查现有密钥状态
   5. 复制新密钥到配置文件
   ```

3. **更新配置文件**
   ```yaml
   ai:
     deepseek:
       apiKey: your-new-api-key-here
   ```

### 2. API端点问题

#### 当前配置检查：
- 基础URL：`https://api.deepseek.com`
- 完整端点：`https://api.deepseek.com/v1/chat/completions`

#### 可能的解决方案：
1. **确认API端点正确**
   - DeepSeek官方API端点应该包含 `/v1/` 前缀
   - 已在代码中修正为：`/v1/chat/completions`

2. **检查网络连接**
   - 确认服务器可以访问 `api.deepseek.com`
   - 检查防火墙设置

### 3. 账户状态问题

#### 检查项目：
- 账户是否有足够的余额
- API调用是否超过配额限制
- 账户是否被暂停

#### 解决步骤：
1. 登录DeepSeek控制台
2. 检查账户余额和使用情况
3. 查看是否有任何限制或警告

### 4. 请求格式问题

#### 已优化的请求格式：
```json
{
  "model": "deepseek-chat",
  "messages": [
    {
      "role": "user",
      "content": "用户消息内容"
    }
  ],
  "max_tokens": 32768,
  "temperature": 0.7
}
```

#### 请求头设置：
```
Content-Type: application/json
Authorization: Bearer your-api-key
User-Agent: Aurora-Blog/1.0
```

## 测试API连接

### 使用新增的测试接口：
```bash
POST /api/admin/ai/test-connection
```

### 或者使用curl命令直接测试：
```bash
curl -X POST "https://api.deepseek.com/v1/chat/completions" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-api-key" \
  -d '{
    "model": "deepseek-chat",
    "messages": [
      {
        "role": "user",
        "content": "你好"
      }
    ],
    "max_tokens": 100
  }'
```

## 日志分析

### 查看详细日志：
1. 启动应用时查看API密钥验证日志
2. API调用时的详细请求信息
3. 错误响应的具体内容

### 日志示例：
```
DeepSeek API请求 - URL: https://api.deepseek.com/v1/chat/completions, Model: deepseek-chat, ApiKey: sk-qlgrdq***
```

## 立即行动建议

1. **首先检查API密钥有效性**
   - 登录DeepSeek控制台验证密钥状态
   - 如果密钥无效，立即生成新密钥

2. **测试API连接**
   - 使用新增的测试接口验证连接
   - 或使用curl命令直接测试

3. **更新配置**
   - 如果获得新密钥，更新 `application-dev.yml` 文件
   - 重启应用使配置生效

4. **检查账户状态**
   - 确认账户余额充足
   - 检查是否有调用限制

## 常见错误码说明

- **401 Unauthorized**: API密钥无效或未提供
- **429 Too Many Requests**: 调用频率超限
- **400 Bad Request**: 请求参数错误
- **500 Internal Server Error**: DeepSeek服务端错误

## 联系支持

如果以上步骤都无法解决问题，请：
1. 联系DeepSeek官方技术支持
2. 提供详细的错误日志和配置信息（隐藏敏感信息）
3. 说明具体的使用场景和错误复现步骤
