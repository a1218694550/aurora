# Aurora博客系统 AI聊天功能配置说明

## 功能简介

Aurora博客系统现已集成AI聊天功能，支持在文章编辑页面使用AI助手进行：
- 智能对话和问答
- 根据关键词自动生成文章
- 生成文章配图（GPT模型）
- 优化文章格式和排版
- 支持多种AI模型切换（DeepSeek、GPT等）

## 配置方法

### 1. 修改配置文件

在 `aurora-springboot/src/main/resources/application-dev.yml` 中添加AI配置：

```yaml
# AI聊天配置
ai:
  enabled: true                    # 是否启用AI功能
  provider: deepseek               # AI提供商: deepseek, gpt
  timeout: 30                      # 请求超时时间(秒)
  maxTokens: 2000                  # 最大令牌数
  temperature: 0.7                 # 温度参数(0-1)
  
  # DeepSeek配置
  deepseek:
    apiKey: your-deepseek-api-key  # DeepSeek API密钥
    baseUrl: https://api.deepseek.com
    chatModel: deepseek-chat       # 聊天模型
    codeModel: deepseek-coder      # 代码模型
    
  # GPT配置  
  gpt:
    apiKey: your-gpt-api-key       # OpenAI API密钥
    baseUrl: https://api.openai.com
    chatModel: gpt-3.5-turbo       # 聊天模型
    imageModel: dall-e-3           # 图片生成模型
```

### 2. 获取API密钥

#### DeepSeek API密钥
1. 访问 [DeepSeek开放平台](https://platform.deepseek.com/)
2. 注册账号并登录
3. 在API密钥管理页面创建新的API密钥
4. 将密钥配置到 `ai.deepseek.apiKey`

#### OpenAI API密钥
1. 访问 [OpenAI平台](https://platform.openai.com/)
2. 注册账号并登录
3. 在API Keys页面创建新的API密钥
4. 将密钥配置到 `ai.gpt.apiKey`

### 3. 切换AI模型

通过修改 `ai.provider` 参数来切换AI提供商：
- `deepseek`: 使用DeepSeek模型（推荐，性价比高）
- `gpt`: 使用OpenAI GPT模型（支持图片生成）

### 4. DeepSeek模型详细配置

系统现已支持DeepSeek的多种模型，每种模型都有不同的特点和适用场景：

#### 基础模型
- **deepseek-chat**: 通用对话模型，适合日常聊天和文章生成
  - 上下文长度: 32K tokens (输入)
  - 最大输出tokens: 4096 (API限制)
  - 推荐温度: 0.7
  - 适用场景: 通用文章写作、内容创作

- **deepseek-coder**: 代码专用模型，专为编程任务优化
  - 上下文长度: 32K tokens (输入)
  - 最大输出tokens: 4096 (API限制)
  - 推荐温度: 0.1 (更精确)
  - 适用场景: 技术文章、代码解释、编程教程

#### V3系列模型
- **deepseek-ai/DeepSeek-V3**: 最新版本，性能全面提升 🔍
  - 上下文长度: 64K tokens (输入)
  - 最大输出tokens: 8192 (API限制)
  - 推荐温度: 0.7
  - **支持深度搜索**: ✅
  - 适用场景: 长文档处理、复杂推理任务

#### R1系列推理模型
- **deepseek-ai/DeepSeek-R1**: 强推理能力模型 🔍
  - 上下文长度: 64K tokens (输入)
  - 最大输出tokens: 8192 (API限制)
  - 推荐温度: 0.7
  - **支持深度搜索**: ✅
  - 适用场景: 复杂逻辑推理、学术写作、深度分析

- **deepseek-ai/DeepSeek-R1-Distill-Qwen-32B**: R1蒸馏版本(Qwen架构) 🔍
  - 上下文长度: 32K tokens (输入)
  - 最大输出tokens: 4096 (API限制)
  - 推荐温度: 0.7
  - **支持深度搜索**: ✅
  - 适用场景: 平衡性能和成本的推理任务

- **deepseek-ai/DeepSeek-R1-Distill-Llama-8B**: R1蒸馏版本(Llama架构) 🔍
  - 上下文长度: 32K tokens (输入)
  - 最大输出tokens: 4096 (API限制)
  - 推荐温度: 0.7
  - **支持深度搜索**: ✅
  - 适用场景: 轻量级推理任务，成本更低

> **重要提示**: DeepSeek API的`max_tokens`参数（控制输出长度）有效范围是1-8192，系统会自动验证和调整超出范围的值。

#### GPT模型  
- **gpt-3.5-turbo**: 性价比较高的聊天模型
- **gpt-4**: 更强大的模型，但成本较高
- **dall-e-3**: 图片生成模型

### 5. 模型选择建议

根据不同使用场景选择合适的模型：

1. **日常文章写作**: deepseek-chat
2. **技术文档**: deepseek-coder  
3. **长文档处理**: deepseek-ai/DeepSeek-V3
4. **复杂推理任务**: deepseek-ai/DeepSeek-R1
5. **成本敏感场景**: deepseek-ai/DeepSeek-R1-Distill-Llama-8B

## 使用方法

### 1. 启动AI助手
在文章编辑页面，点击右下角的"AI助手"按钮打开聊天面板。

### 2. 选择AI模型
在聊天面板顶部的下拉框中选择合适的AI模型：
- **跨提供商切换**: 支持直接从DeepSeek切换到GPT等其他提供商
- **分组显示**: 模型按提供商分组显示，便于选择
- **实时切换**: 可以随时切换模型和提供商，无需重启应用
- **状态显示**: 显示当前使用的提供商和模型信息
- **智能识别**: 系统自动识别模型所属提供商并切换
- **深度搜索标识**: 支持深度搜索的模型会显示 🔍 图标

### 3. 深度搜索功能 🔍
**适用模型**: DeepSeek-V3、DeepSeek-R1系列模型

#### 功能特点：
- **智能开关**: 仅在支持深度搜索的模型下显示开关
- **自动适配**: 切换到不支持的模型时自动关闭
- **增强效果**: 启用后可获得更准确和详细的回答
- **成本提醒**: 会消耗更多tokens，请合理使用

#### 使用方法：
1. 选择支持深度搜索的模型（带🔍标识）
2. 在模型信息下方找到"深度搜索"开关
3. 点击开关启用深度搜索功能
4. 发送消息时会自动使用深度搜索模式

### 4. 智能对话
在"智能对话"标签页中：
- 输入问题或需求
- AI会提供相应的建议和帮助
- 支持Ctrl+Enter快速发送

### 5. 文章生成
在"文章生成"标签页中：
- 输入文章主题关键词（必填）
- 选择文章长度（短文/中等/长文）
- 选择写作风格（专业技术/通俗易懂/学术严谨/轻松幽默）
- 选择目标读者（初学者/进阶用户/专家/普通读者）
- 点击"生成文章"按钮

### 6. 图片生成（仅GPT支持）
在"图片生成"标签页中：
- 详细描述想要生成的图片
- 点击"生成图片"按钮
- 生成成功后可直接插入到文章中

### 7. 格式优化
在"格式优化"标签页中：
- 点击"优化文章格式"按钮
- AI会自动优化当前文章的Markdown格式和排版

## 注意事项

1. **API费用**: 使用AI功能会产生API调用费用，请注意控制使用频率
2. **网络要求**: 需要稳定的网络连接访问AI服务
3. **内容审查**: AI生成的内容仅供参考，请自行审查和修改
4. **隐私安全**: 请勿在AI对话中包含敏感信息
5. **服务可用性**: AI服务可能因网络或服务商问题临时不可用

## 故障排除

### 1. AI功能不可用
- 检查 `ai.enabled` 是否为 `true`
- 检查API密钥是否正确配置
- 检查网络连接是否正常

### 2. API调用失败
- 检查API密钥是否有效
- 检查账户余额是否充足
- 检查API请求频率是否超限

### 3. 图片生成失败
- 确认使用的是GPT提供商
- 检查图片描述是否符合内容政策
- 确认dall-e-3模型可用

## 技术架构

### 后端实现
- **AiConfigProperties**: AI配置属性类
- **AiChatService**: AI聊天服务接口
- **AiChatServiceImpl**: AI聊天服务实现
- **AiChatController**: AI聊天控制器

### 前端实现
- **AiChatPanel**: AI聊天面板组件
- 集成到文章编辑页面
- 支持多种功能切换

### 配置管理
- 支持多环境配置
- 热切换AI提供商
- 灵活的参数调整

### 🚀 API接口说明

#### 1. 聊天对话
**POST /api/admin/ai/chat**
```json
{
  "message": "用户消息内容",
  "chatType": "general"
}
```

#### 2. 生成文章
**POST /api/admin/ai/generate/article**
```json
{
  "keywords": "文章关键词",
  "length": "medium",
  "style": "technical", 
  "audience": "intermediate"
}
```

#### 3. 生成图片
**POST /api/admin/ai/generate/image**
```json
{
  "prompt": "图片描述提示词"
}
```

#### 4. 优化格式
**POST /api/admin/ai/optimize/format**
```json
{
  "content": "要优化的文章内容"
}
```

#### 5. 切换模型
**POST /api/admin/ai/switch-model**
```json
{
  "modelName": "deepseek-ai/DeepSeek-V3"
}
```

#### 6. 查询接口
- **GET /api/admin/ai/models/all** - 获取所有提供商的模型列表
- **GET /api/admin/ai/models** - 获取当前提供商的模型列表
- **GET /api/admin/ai/current-model** - 获取当前模型信息
- **GET /api/admin/ai/status** - 检查AI服务状态

## 更新日志

### v1.3.0
- **新增深度搜索功能** 🔍
- 支持DeepSeek-V3和R1系列模型的深度搜索
- 智能开关：仅在支持的模型下显示
- 自动适配：切换模型时自动调整开关状态
- 模型标识：支持深度搜索的模型显示🔍图标
- 参数传递：前后端完整支持深度搜索参数

### v1.2.1
- **修复max_tokens参数限制问题**
- 调整所有模型的max_tokens到API支持范围(1-8192)
- 添加max_tokens参数自动验证和调整
- 优化错误处理和日志记录
- 更新配置文档说明

### v1.2.0
- **优化API接口参数格式**
- 所有POST接口统一使用JSON格式参数
- 添加参数验证和错误提示
- 完善API文档和示例

### v1.1.0
- **新增跨提供商模型切换功能**
- 支持直接从DeepSeek切换到GPT等其他提供商
- 新增模型分组显示界面
- 添加当前模型状态显示
- 优化模型选择用户体验
- 新增GPT-4o和GPT-4o-mini模型支持

### v1.0.0
- 初始版本发布
- 支持DeepSeek和GPT模型
- 实现基础聊天、文章生成、图片生成、格式优化功能
- 完整的前后端集成

---

如有问题或建议，请提交Issue或联系开发团队。
