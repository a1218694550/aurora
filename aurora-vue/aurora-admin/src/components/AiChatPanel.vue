<template>
  <div class="ai-chat-panel">
    <!-- AI聊天按钮 -->
    <el-button 
      type="primary" 
      icon="el-icon-chat-dot-round" 
      class="ai-chat-toggle"
      @click="toggleChatPanel"
      :disabled="!aiAvailable">
      AI助手
    </el-button>

    <!-- AI聊天面板 -->
    <el-drawer
      title="AI写作助手"
      :visible.sync="chatVisible"
      direction="rtl"
      size="400px"
      :before-close="handleClose">
      
      <!-- 模型选择器 -->
      <div class="model-selector">
        <el-select 
          v-model="selectedModel" 
          placeholder="选择AI模型" 
          size="small"
          @change="switchModel"
          style="width: 100%; margin-bottom: 15px;">
          <el-option-group
            v-for="(models, provider) in allAvailableModels"
            :key="provider"
            :label="getProviderDisplayName(provider)">
            <el-option
              v-for="model in models"
              :key="model"
              :label="getModelDisplayName(model)"
              :value="model">
            </el-option>
          </el-option-group>
        </el-select>
        
        <!-- 当前模型信息 -->
        <div v-if="currentModelInfo" class="model-info">
          <el-tag size="mini" :type="getProviderTagType(currentModelInfo.provider)">
            {{ getProviderDisplayName(currentModelInfo.provider) }}
          </el-tag>
          <span class="model-name">{{ getModelDisplayName(currentModelInfo.model) }}</span>
          <el-button 
            size="mini" 
            type="text" 
            @click="testConnection"
            :loading="testingConnection"
            style="margin-left: 8px;">
            <i class="el-icon-connection"></i> 测试连接
          </el-button>
        </div>
        
        <!-- 深度搜索开关 -->
        <div v-if="supportsDeepSearch" class="deep-search-toggle">
          <el-switch
            v-model="enableDeepSearch"
            active-color="#409EFF"
            inactive-color="#C0C4CC"
            active-text="深度搜索"
            inactive-text=""
            style="margin-top: 8px;">
          </el-switch>
          <el-tooltip content="启用深度搜索可获得更准确和详细的回答，但会消耗更多tokens" placement="top">
            <i class="el-icon-question deep-search-help" style="margin-left: 5px; color: #909399; cursor: help;"></i>
          </el-tooltip>
        </div>
      </div>
      
      <!-- 功能选择 -->
      <div class="function-tabs">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="智能对话" name="chat">
            <div class="chat-container">
              <!-- 聊天历史 -->
              <div class="chat-history" ref="chatHistory">
                <div 
                  v-for="(message, index) in chatHistory" 
                  :key="index" 
                  :class="['message', message.type]">
                  <div class="message-content">
                    <div class="message-text" v-html="formatMessage(message.content)"></div>
                    <div class="message-time">{{ formatTime(message.time) }}</div>
                  </div>
                </div>
              </div>
              
              <!-- 输入框 -->
              <div class="chat-input">
                <el-input
                  v-model="chatMessage"
                  type="textarea"
                  :rows="3"
                  placeholder="输入您的问题或需求..."
                  @keyup.ctrl.enter.native="sendMessage"
                  :disabled="loading">
                </el-input>
                <el-button 
                  type="primary" 
                  @click="sendMessage"
                  :loading="loading"
                  style="margin-top: 10px; width: 100%;">
                  发送 (Ctrl+Enter)
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="文章生成" name="generate">
            <div class="generate-container">
              <el-form :model="generateForm" label-width="80px" size="small">
                <el-form-item label="关键词" required>
                  <el-input 
                    v-model="generateForm.keywords" 
                    placeholder="请输入文章主题关键词">
                  </el-input>
                </el-form-item>
                
                <el-form-item label="文章长度">
                  <el-select v-model="generateForm.length" placeholder="请选择">
                    <el-option label="短文 (500-800字)" value="short"></el-option>
                    <el-option label="中等 (800-1500字)" value="medium"></el-option>
                    <el-option label="长文 (1500-3000字)" value="long"></el-option>
                  </el-select>
                </el-form-item>
                
                <el-form-item label="写作风格">
                  <el-select v-model="generateForm.style" placeholder="请选择">
                    <el-option label="专业技术" value="technical"></el-option>
                    <el-option label="通俗易懂" value="popular"></el-option>
                    <el-option label="学术严谨" value="academic"></el-option>
                    <el-option label="轻松幽默" value="humorous"></el-option>
                  </el-select>
                </el-form-item>
                
                <el-form-item label="目标读者">
                  <el-select v-model="generateForm.audience" placeholder="请选择">
                    <el-option label="初学者" value="beginner"></el-option>
                    <el-option label="进阶用户" value="intermediate"></el-option>
                    <el-option label="专家" value="expert"></el-option>
                    <el-option label="普通读者" value="general"></el-option>
                  </el-select>
                </el-form-item>
                
                <el-button 
                  type="success" 
                  @click="generateArticle"
                  :loading="loading"
                  style="width: 100%;">
                  生成文章
                </el-button>
              </el-form>
            </div>
          </el-tab-pane>

          <el-tab-pane label="图片生成" name="image">
            <div class="image-container">
              <el-form :model="imageForm" label-width="80px" size="small">
                <el-form-item label="图片描述" required>
                  <el-input 
                    v-model="imageForm.prompt" 
                    type="textarea"
                    :rows="4"
                    placeholder="请详细描述您想要生成的图片...">
                  </el-input>
                </el-form-item>
                
                <el-button 
                  type="warning" 
                  @click="generateImage"
                  :loading="loading"
                  style="width: 100%;">
                  生成图片
                </el-button>
              </el-form>
              
              <!-- 生成的图片显示 -->
              <div v-if="generatedImage" class="generated-image">
                <img :src="generatedImage" alt="AI生成图片" style="width: 100%; margin-top: 10px;">
                <el-button 
                  size="small" 
                  type="primary" 
                  @click="insertImage"
                  style="margin-top: 10px; width: 100%;">
                  插入到文章
                </el-button>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="格式优化" name="format">
            <div class="format-container">
              <p style="color: #666; font-size: 14px; margin-bottom: 15px;">
                将自动优化当前文章的格式和排版
              </p>
              <el-button 
                type="info" 
                @click="optimizeFormat"
                :loading="loading"
                style="width: 100%;">
                优化文章格式
              </el-button>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script>
export default {
  name: 'AiChatPanel',
  props: {
    articleContent: {
      type: String,
      default: ''
    },
    articleTitle: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      chatVisible: false,
      activeTab: 'chat',
      loading: false,
      aiAvailable: false,
      chatMessage: '',
      chatHistory: [],
      generatedImage: null,
      availableModels: [],
      allAvailableModels: {},
      selectedModel: '',
      currentModelInfo: null,
      testingConnection: false,
      enableDeepSearch: false,
      supportsDeepSearch: false,
      generateForm: {
        keywords: '',
        length: 'medium',
        style: 'technical',
        audience: 'intermediate'
      },
      imageForm: {
        prompt: ''
      }
    }
  },
  created() {
    this.checkAiStatus()
    this.loadAllAvailableModels()
    this.loadCurrentModelInfo()
  },
  methods: {
    async checkAiStatus() {
      try {
        const { data } = await this.axios.get('/api/admin/ai/status')
        this.aiAvailable = data.data
      } catch (error) {
        this.aiAvailable = false
        console.error('检查AI状态失败:', error)
      }
    },

    toggleChatPanel() {
      this.chatVisible = !this.chatVisible
    },

    handleClose() {
      this.chatVisible = false
    },

    handleTabClick() {
      // 切换标签页时的处理
    },

    async sendMessage() {
      if (!this.chatMessage.trim()) {
        this.$message.warning('请输入消息内容')
        return
      }

      // 添加用户消息到历史
      this.chatHistory.push({
        type: 'user',
        content: this.chatMessage,
        time: new Date()
      })

      const userMessage = this.chatMessage
      this.chatMessage = ''
      this.loading = true

      try {
        const { data } = await this.axios.post('/api/admin/ai/chat', {
          message: userMessage,
          chatType: 'general',
          enableDeepSearch: this.enableDeepSearch
        })

        if (data.data.success) {
          this.chatHistory.push({
            type: 'ai',
            content: data.data.content,
            time: new Date()
          })
        } else {
          this.chatHistory.push({
            type: 'error',
            content: data.data.error || '服务异常',
            time: new Date()
          })
        }
      } catch (error) {
        this.chatHistory.push({
          type: 'error',
          content: '网络异常，请稍后重试',
          time: new Date()
        })
        console.error('AI聊天异常:', error)
      } finally {
        this.loading = false
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      }
    },

    async generateArticle() {
      if (!this.generateForm.keywords.trim()) {
        this.$message.warning('请输入关键词')
        return
      }

      this.loading = true
      try {
        const { data } = await this.axios.post('/api/admin/ai/generate/article', {
          keywords: this.generateForm.keywords,
          length: this.generateForm.length,
          style: this.generateForm.style,
          audience: this.generateForm.audience
        })

        if (data.data.success) {
          this.$emit('content-generated', data.data.content)
          this.$message.success('文章生成成功！')
          this.chatVisible = false
        } else {
          this.$message.error(data.data.error || '文章生成失败')
        }
      } catch (error) {
        this.$message.error('网络异常，请稍后重试')
        console.error('文章生成异常:', error)
      } finally {
        this.loading = false
      }
    },

    async generateImage() {
      if (!this.imageForm.prompt.trim()) {
        this.$message.warning('请输入图片描述')
        return
      }

      this.loading = true
      try {
        const { data } = await this.axios.post('/api/admin/ai/generate/image', {
          prompt: this.imageForm.prompt
        })

        if (data.data.success) {
          this.generatedImage = data.data.imageUrl
          this.$message.success('图片生成成功！')
        } else {
          this.$message.error(data.data.error || '图片生成失败')
        }
      } catch (error) {
        this.$message.error('网络异常，请稍后重试')
        console.error('图片生成异常:', error)
      } finally {
        this.loading = false
      }
    },

    async optimizeFormat() {
      if (!this.articleContent.trim()) {
        this.$message.warning('请先输入文章内容')
        return
      }

      this.loading = true
      try {
        const { data } = await this.axios.post('/api/admin/ai/optimize/format', {
          content: this.articleContent
        })

        if (data.data.success) {
          this.$emit('content-optimized', data.data.content)
          this.$message.success('格式优化成功！')
          this.chatVisible = false
        } else {
          this.$message.error(data.data.error || '格式优化失败')
        }
      } catch (error) {
        this.$message.error('网络异常，请稍后重试')
        console.error('格式优化异常:', error)
      } finally {
        this.loading = false
      }
    },

    insertImage() {
      if (this.generatedImage) {
        const imageMarkdown = `\n![AI生成图片](${this.generatedImage})\n`
        this.$emit('image-generated', imageMarkdown)
        this.$message.success('图片已插入到文章中')
      }
    },

    formatMessage(content) {
      // 简单的Markdown格式化
      return content
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`(.*?)`/g, '<code>$1</code>')
        .replace(/\n/g, '<br>')
    },

    formatTime(time) {
      return this.$moment(time).format('HH:mm:ss')
    },

    scrollToBottom() {
      const chatHistory = this.$refs.chatHistory
      if (chatHistory) {
        chatHistory.scrollTop = chatHistory.scrollHeight
      }
    },

    async loadAllAvailableModels() {
      try {
        const { data } = await this.axios.get('/api/admin/ai/models/all')
        this.allAvailableModels = data.data
        
        // 同时获取当前提供商的模型列表（兼容性）
        const currentResponse = await this.axios.get('/api/admin/ai/models')
        this.availableModels = currentResponse.data.data
      } catch (error) {
        console.error('获取模型列表失败:', error)
      }
    },

    async loadCurrentModelInfo() {
      try {
        const { data } = await this.axios.get('/api/admin/ai/current-model')
        this.currentModelInfo = data.data
        this.selectedModel = this.currentModelInfo.model
        
        // 检查当前模型是否支持深度搜索
        if (this.currentModelInfo.config && this.currentModelInfo.config.supportsDeepSearch) {
          this.supportsDeepSearch = this.currentModelInfo.config.supportsDeepSearch
        } else {
          this.supportsDeepSearch = false
          this.enableDeepSearch = false
        }
      } catch (error) {
        console.error('获取当前模型信息失败:', error)
      }
    },

    async switchModel(modelName) {
      try {
        const { data } = await this.axios.post('/api/admin/ai/switch-model', {
          modelName: modelName
        })
        if (data.data) {
          // 重新加载当前模型信息
          await this.loadCurrentModelInfo()
          
          const providerName = this.getProviderDisplayName(this.currentModelInfo.provider)
          const modelDisplayName = this.getModelDisplayName(modelName)
          
          // 如果新模型不支持深度搜索，自动关闭深度搜索
          if (!this.supportsDeepSearch) {
            this.enableDeepSearch = false
          }
          
          this.$message.success(`已切换到 ${providerName} - ${modelDisplayName}`)
        } else {
          this.$message.error('模型切换失败')
        }
      } catch (error) {
        this.$message.error('模型切换异常')
        console.error('切换模型失败:', error)
      }
    },

    getModelDisplayName(model) {
      const modelNames = {
        // DeepSeek模型
        'deepseek-chat': 'DeepSeek Chat (通用对话)',
        'deepseek-coder': 'DeepSeek Coder (代码专用)',
        'deepseek-ai/DeepSeek-V3': 'DeepSeek V3 (最新版本) 🔍',
        'deepseek-ai/DeepSeek-R1': 'DeepSeek R1 (推理模型) 🔍',
        'deepseek-ai/DeepSeek-R1-Distill-Qwen-32B': 'DeepSeek R1 Distill Qwen-32B 🔍',
        'deepseek-ai/DeepSeek-R1-Distill-Llama-8B': 'DeepSeek R1 Distill Llama-8B 🔍',
        // GPT模型
        'gpt-3.5-turbo': 'GPT-3.5 Turbo',
        'gpt-4': 'GPT-4',
        'gpt-4-turbo': 'GPT-4 Turbo',
        'gpt-4o': 'GPT-4o (多模态)',
        'gpt-4o-mini': 'GPT-4o Mini (轻量版)'
      }
      return modelNames[model] || model
    },

    getProviderDisplayName(provider) {
      const providerNames = {
        'deepseek': 'DeepSeek',
        'gpt': 'OpenAI GPT',
        'claude': 'Anthropic Claude'
      }
      return providerNames[provider] || provider
    },

    getProviderTagType(provider) {
      const tagTypes = {
        'deepseek': 'success',
        'gpt': 'primary',
        'claude': 'warning'
      }
      return tagTypes[provider] || 'info'
    },

    async testConnection() {
      this.testingConnection = true
      try {
        const { data } = await this.axios.post('/api/admin/ai/test-connection')
        
        if (data.data.success) {
          this.$message.success('AI连接测试成功！')
          // 显示AI的回复内容
          if (data.data.content) {
            this.$notify({
              title: 'AI回复',
              message: data.data.content,
              type: 'success',
              duration: 5000
            })
          }
        } else {
          this.$message.error(`连接测试失败: ${data.data.error}`)
          // 显示详细错误信息
          this.$notify({
            title: '连接测试失败',
            message: data.data.error,
            type: 'error',
            duration: 8000
          })
        }
      } catch (error) {
        this.$message.error('连接测试异常')
        console.error('API连接测试失败:', error)
        
        // 显示网络错误详情
        let errorMsg = '网络连接异常'
        if (error.response && error.response.data && error.response.data.message) {
          errorMsg = error.response.data.message
        }
        
        this.$notify({
          title: '连接测试异常',
          message: errorMsg,
          type: 'error',
          duration: 8000
        })
      } finally {
        this.testingConnection = false
      }
    }
  }
}
</script>

<style scoped>
.ai-chat-panel {
  position: relative;
  padding: 20px;
}

.ai-chat-toggle {
  position: fixed;
  right: 30px;
  bottom: 100px;
  z-index: 1000;
  border-radius: 50px;
  padding: 12px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.model-selector {
  padding: 0 20px;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 15px;
}

.model-info {
  display: flex;
  align-items: center;
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}

.model-info .model-name {
  margin-left: 8px;
  font-weight: 500;
}

.deep-search-toggle {
  display: flex;
  align-items: center;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.deep-search-help {
  font-size: 14px;
}

.function-tabs {
  height: calc(100% - 60px);
}

.chat-container {
  height: calc(100vh - 120px);
  display: flex;
  flex-direction: column;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 10px 0;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 15px;
}

.message.user .message-content {
  background: #409eff;
  color: white;
  margin-left: 20%;
  border-radius: 10px 10px 5px 10px;
}

.message.ai .message-content {
  background: #f5f7fa;
  color: #303133;
  margin-right: 20%;
  border-radius: 10px 10px 10px 5px;
}

.message.error .message-content {
  background: #fef0f0;
  color: #f56c6c;
  margin-right: 20%;
  border-radius: 10px 10px 10px 5px;
  border: 1px solid #fbc4c4;
}

.message-content {
  padding: 10px 15px;
  position: relative;
}

.message-text {
  word-wrap: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 5px;
  text-align: right;
}

.chat-input {
  border-top: 1px solid #e4e7ed;
  padding-top: 10px;
}

.generate-container,
.image-container,
.format-container {
  padding: 20px 0;
}

.generated-image {
  text-align: center;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  margin-top: 15px;
}

.generated-image img {
  border-radius: 4px;
}

/* 滚动条样式 */
.chat-history::-webkit-scrollbar {
  width: 6px;
}

.chat-history::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-history::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
