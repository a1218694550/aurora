<template>
  <div
    v-if="isEnabled"
    ref="agentContainer"
    class="coze-agent-container"
    :class="{ 'dragging': isDragging, 'expanded': isExpanded }"
    :style="containerStyle">
    
    <!-- 智能体头像/触发器 -->
    <div 
      class="agent-trigger" 
      @mousedown="handleMouseDown"
      @touchstart="handleTouchStart">
      <img :src="config.avatar" :alt="config.title" class="agent-avatar" />
      <div class="agent-status" :class="{ 'online': isOnline }"></div>
      <div v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</div>
    </div>

    <!-- 聊天窗口 -->
    <transition name="chat-window">
      <div v-if="isExpanded" class="chat-window" :style="chatWindowStyle">
        <div class="chat-header">
          <span class="chat-title">{{ config.title }}</span>
          <div class="chat-controls">
            <button @click="minimizeChat" class="control-btn">
              <svg-icon icon-class="minus" />
            </button>
            <button @click="closeChat" class="control-btn">
              <svg-icon icon-class="close" />
            </button>
          </div>
        </div>
        
        <div ref="chatMessages" class="chat-messages">
          <div
            v-for="message in messages"
            :key="message.id"
            class="message"
            :class="{ 'user': message.type === 'user', 'agent': message.type === 'agent' }">
            <div class="message-content">
              <div v-if="message.type === 'agent'" class="agent-info">
                <img :src="config.avatar" class="message-avatar" />
                <span class="agent-name">{{ config.title }}</span>
              </div>
              <div class="message-text" v-html="message.content"></div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
          
          <!-- 加载状态 -->
          <div v-if="isLoading" class="message agent">
            <div class="message-content">
              <div class="agent-info">
                <img :src="config.avatar" class="message-avatar" />
                <span class="agent-name">{{ config.title }}</span>
              </div>
              <div class="typing-indicator">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="chat-input">
          <div class="input-container">
            <textarea
              v-model="inputMessage"
              ref="messageInput"
              placeholder="输入消息..."
              @keydown.enter.prevent="sendMessage"
              @input="adjustTextareaHeight"
              rows="1"></textarea>
            <button
              @click="sendMessage"
              :disabled="!inputMessage.trim() || isLoading"
              class="send-btn">
              <svg-icon icon-class="send" />
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue'
import api from '@/api/api'

interface Message {
  id: string
  type: 'user' | 'agent'
  content: string
  timestamp: number
}

interface CozeConfig {
  enabled: boolean
  avatar: string
  title: string
  theme: string
  draggable: boolean
  position: { x: number; y: number }
  size: { width: number; height: number }
}

export default defineComponent({
  name: 'CozeAgent',
  setup() {
    const agentContainer = ref<HTMLElement>()
    const chatMessages = ref<HTMLElement>()
    const messageInput = ref<HTMLTextAreaElement>()
    
    const isEnabled = ref(true)
    console.log('CozeAgent setup called')
    const isExpanded = ref(false)
    const isDragging = ref(false)
    const isLoading = ref(false)
    const isOnline = ref(true)
    const unreadCount = ref(0)
    const inputMessage = ref('')
    
    const position = reactive({ x: 50, y: 50 })
    const dragOffset = reactive({ x: 0, y: 0 })
    const messages = ref<Message[]>([])
    
    const config = reactive<CozeConfig>({
      enabled: true,
      avatar: '/images/coze-avatar.svg',
      title: '智能助手',
      theme: 'light',
      draggable: true,
      position: { x: 50, y: 50 },
      size: { width: 300, height: 400 }
    })

    // 计算样式
    const containerStyle = computed(() => ({
      position: 'fixed',
      left: `${position.x}px`,
      top: `${position.y}px`,
      zIndex: 1000,
      cursor: isDragging.value ? 'grabbing' : (config.draggable ? 'grab' : 'default')
    }))

    const chatWindowStyle = computed(() => ({
      width: `${config.size.width}px`,
      height: `${config.size.height}px`
    }))

    // 拖拽相关方法
    let dragStartTime = 0
    let dragStartPos = { x: 0, y: 0 }
    let hasDragged = false
    let isMouseDown = false
    
    const handleMouseDown = (event: MouseEvent) => {
      isMouseDown = true
      dragStartTime = Date.now()
      dragStartPos.x = event.clientX
      dragStartPos.y = event.clientY
      hasDragged = false
      
      dragOffset.x = event.clientX - position.x
      dragOffset.y = event.clientY - position.y
      
      document.addEventListener('mousemove', handleMouseMove)
      document.addEventListener('mouseup', handleMouseUp)
      event.preventDefault()
    }

    const handleTouchStart = (event: TouchEvent) => {
      const touch = event.touches[0]
      isMouseDown = true
      dragStartTime = Date.now()
      dragStartPos.x = touch.clientX
      dragStartPos.y = touch.clientY
      hasDragged = false
      
      dragOffset.x = touch.clientX - position.x
      dragOffset.y = touch.clientY - position.y
      
      document.addEventListener('touchmove', handleTouchMove)
      document.addEventListener('touchend', handleTouchEnd)
      event.preventDefault()
    }

    const handleMouseMove = (event: MouseEvent) => {
      if (!isMouseDown) return
      
      // 计算移动距离
      const moveDistance = Math.sqrt(
        Math.pow(event.clientX - dragStartPos.x, 2) + 
        Math.pow(event.clientY - dragStartPos.y, 2)
      )
      
      // 根据是否展开设置不同的拖拽阈值
      const timeThreshold = isExpanded.value ? 500 : 200  // 展开时需要更长时间
      const distanceThreshold = isExpanded.value ? 15 : 5  // 展开时需要更大移动距离
      
      // 如果移动距离或时间超过阈值，则认为是拖拽
      if (moveDistance > distanceThreshold || (Date.now() - dragStartTime > timeThreshold)) {
        hasDragged = true
        isDragging.value = true
        
        if (config.draggable) {
          position.x = Math.max(0, Math.min(window.innerWidth - 60, event.clientX - dragOffset.x))
          position.y = Math.max(0, Math.min(window.innerHeight - 60, event.clientY - dragOffset.y))
        }
      }
    }

    const handleTouchMove = (event: TouchEvent) => {
      if (!isMouseDown) return
      
      const touch = event.touches[0]
      
      // 计算移动距离
      const moveDistance = Math.sqrt(
        Math.pow(touch.clientX - dragStartPos.x, 2) + 
        Math.pow(touch.clientY - dragStartPos.y, 2)
      )
      
      // 根据是否展开设置不同的拖拽阈值
      const timeThreshold = isExpanded.value ? 500 : 200  // 展开时需要更长时间
      const distanceThreshold = isExpanded.value ? 15 : 5  // 展开时需要更大移动距离
      
      // 如果移动距离或时间超过阈值，则认为是拖拽
      if (moveDistance > distanceThreshold || (Date.now() - dragStartTime > timeThreshold)) {
        hasDragged = true
        isDragging.value = true
        
        if (config.draggable) {
          position.x = Math.max(0, Math.min(window.innerWidth - 60, touch.clientX - dragOffset.x))
          position.y = Math.max(0, Math.min(window.innerHeight - 60, touch.clientY - dragOffset.y))
        }
      }
    }

    const handleMouseUp = (event: MouseEvent) => {
      isMouseDown = false
      isDragging.value = false
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
      
      // 如果没有拖拽，则认为是点击事件
      if (!hasDragged) {
        handleTriggerClick(event)
      } else {
        // 如果拖拽了，保存位置
        savePosition()
      }
      
      // 重置状态
      hasDragged = false
    }

    const handleTouchEnd = (event: TouchEvent) => {
      isMouseDown = false
      isDragging.value = false
      document.removeEventListener('touchmove', handleTouchMove)
      document.removeEventListener('touchend', handleTouchEnd)
      
      // 如果没有拖拽，则认为是点击事件
      if (!hasDragged) {
        // 创建一个模拟的 MouseEvent 用于触发点击
        const mouseEvent = new MouseEvent('click', {
          bubbles: true,
          cancelable: true,
          clientX: event.changedTouches[0].clientX,
          clientY: event.changedTouches[0].clientY
        })
        handleTriggerClick(mouseEvent)
      } else {
        // 如果拖拽了，保存位置
        savePosition()
      }
      
      // 重置状态
      hasDragged = false
    }

    // 聊天相关方法
    const handleTriggerClick = (event: MouseEvent) => {
      console.log('Trigger clicked', { isExpanded: isExpanded.value })
      
      // 阻止事件冒泡
      event.stopPropagation()
      event.preventDefault()
      toggleExpanded()
    }
    
    const toggleExpanded = () => {
      console.log('toggleExpanded called, current state:', isExpanded.value)
      isExpanded.value = !isExpanded.value
      console.log('toggleExpanded new state:', isExpanded.value)
      if (isExpanded.value) {
        unreadCount.value = 0
        nextTick(() => {
          scrollToBottom()
          messageInput.value?.focus()
        })
      }
    }

    const minimizeChat = () => {
      isExpanded.value = false
    }

    const closeChat = () => {
      isExpanded.value = false
    }

    const sendMessage = async () => {
      if (!inputMessage.value.trim() || isLoading.value) return
      
      const userMessage: Message = {
        id: Date.now().toString(),
        type: 'user',
        content: inputMessage.value.trim(),
        timestamp: Date.now()
      }
      
      messages.value.push(userMessage)
      const messageContent = inputMessage.value.trim()
      inputMessage.value = ''
      
      scrollToBottom()
      isLoading.value = true
      
      try {
        // 调用扣子智能体API
        const response = await api.sendMessageToCoze({
          message: messageContent,
          conversationId: getConversationId()
        })
        
        const agentMessage: Message = {
          id: (Date.now() + 1).toString(),
          type: 'agent',
          content: response.data.content,
          timestamp: Date.now()
        }
        
        messages.value.push(agentMessage)
        scrollToBottom()
      } catch (error) {
        console.error('发送消息失败:', error)
        const errorMessage: Message = {
          id: (Date.now() + 1).toString(),
          type: 'agent',
          content: '抱歉，我现在无法回复，请稍后再试。',
          timestamp: Date.now()
        }
        messages.value.push(errorMessage)
        scrollToBottom()
      } finally {
        isLoading.value = false
      }
    }

    const scrollToBottom = () => {
      nextTick(() => {
        if (chatMessages.value) {
          chatMessages.value.scrollTop = chatMessages.value.scrollHeight
        }
      })
    }

    const adjustTextareaHeight = () => {
      if (messageInput.value) {
        messageInput.value.style.height = 'auto'
        messageInput.value.style.height = Math.min(messageInput.value.scrollHeight, 100) + 'px'
      }
    }

    const formatTime = (timestamp: number) => {
      return new Date(timestamp).toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const getConversationId = () => {
      let conversationId = localStorage.getItem('coze-conversation-id')
      if (!conversationId) {
        conversationId = Date.now().toString()
        localStorage.setItem('coze-conversation-id', conversationId)
      }
      return conversationId
    }

    const savePosition = () => {
      localStorage.setItem('coze-agent-position', JSON.stringify({
        x: position.x,
        y: position.y
      }))
    }

    const loadPosition = () => {
      const savedPosition = localStorage.getItem('coze-agent-position')
      if (savedPosition) {
        const pos = JSON.parse(savedPosition)
        position.x = pos.x
        position.y = pos.y
      }
    }

    const loadConfig = async () => {
      try {
        console.log('Loading coze config...')
        // 从后端获取配置
        const response = await api.getCozeConfig()
        console.log('Coze config response:', response)
        Object.assign(config, response.data)
        isEnabled.value = config.enabled
        console.log('Coze config loaded:', config)
      } catch (error) {
        console.error('加载扣子配置失败:', error)
        // 如果加载失败，使用默认配置
        isEnabled.value = true
      }
    }

    onMounted(() => {
      console.log('CozeAgent mounted')
      loadConfig()
      loadPosition()
      
      // 添加欢迎消息
      messages.value.push({
        id: 'welcome',
        type: 'agent',
        content: '你好！我是你的智能助手，有什么可以帮助你的吗？',
        timestamp: Date.now()
      })
    })

    onUnmounted(() => {
      document.removeEventListener('mousemove', handleMouseMove)
      document.removeEventListener('mouseup', handleMouseUp)
      document.removeEventListener('touchmove', handleTouchMove)
      document.removeEventListener('touchend', handleTouchEnd)
    })

    return {
      agentContainer,
      chatMessages,
      messageInput,
      isEnabled,
      isExpanded,
      isDragging,
      isLoading,
      isOnline,
      unreadCount,
      inputMessage,
      messages,
      config,
      containerStyle,
      chatWindowStyle,
      handleMouseDown,
      handleTouchStart,
      handleTriggerClick,
      toggleExpanded,
      minimizeChat,
      closeChat,
      sendMessage,
      adjustTextareaHeight,
      formatTime
    }
  }
})
</script>

<style lang="scss" scoped>
.coze-agent-container {
  user-select: none;
  
  &.dragging {
    .agent-trigger {
      transform: scale(1.1);
      cursor: grabbing;
    }
    
    .chat-window {
      pointer-events: none;
    }
  }
}

.agent-trigger {
  position: relative;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 25px rgba(0, 0, 0, 0.2);
  }
  
  .agent-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .agent-status {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: #ccc;
    border: 2px solid white;
    
    &.online {
      background: #4CAF50;
    }
  }
  
  .unread-badge {
    position: absolute;
    top: -5px;
    right: -5px;
    background: #ff4757;
    color: white;
    border-radius: 10px;
    padding: 2px 6px;
    font-size: 12px;
    font-weight: bold;
    min-width: 18px;
    text-align: center;
  }
}

.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  
  .chat-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 12px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .chat-title {
      font-weight: 600;
      font-size: 14px;
    }
    
    .chat-controls {
      display: flex;
      gap: 8px;
      
      .control-btn {
        background: rgba(255, 255, 255, 0.2);
        border: none;
        color: white;
        width: 24px;
        height: 24px;
        border-radius: 4px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.2s;
        
        &:hover {
          background: rgba(255, 255, 255, 0.3);
        }
      }
    }
  }
  
  .chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    max-height: 300px;
    
    .message {
      margin-bottom: 16px;
      
      &.user {
        .message-content {
          background: #007bff;
          color: white;
          margin-left: 40px;
          border-radius: 18px 18px 4px 18px;
        }
      }
      
      &.agent {
        .message-content {
          background: #f1f3f5;
          color: #333;
          margin-right: 40px;
          border-radius: 18px 18px 18px 4px;
        }
      }
      
      .message-content {
        padding: 12px 16px;
        max-width: 80%;
        word-wrap: break-word;
        
        .agent-info {
          display: flex;
          align-items: center;
          margin-bottom: 8px;
          
          .message-avatar {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            margin-right: 8px;
          }
          
          .agent-name {
            font-size: 12px;
            font-weight: 600;
            color: #666;
          }
        }
        
        .message-text {
          line-height: 1.4;
          font-size: 14px;
        }
        
        .message-time {
          font-size: 11px;
          color: #999;
          margin-top: 4px;
          text-align: right;
        }
      }
    }
  }
  
  .chat-input {
    border-top: 1px solid #eee;
    padding: 12px;
    
    .input-container {
      display: flex;
      align-items: flex-end;
      gap: 8px;
      
      textarea {
        flex: 1;
        border: 1px solid #ddd;
        border-radius: 20px;
        padding: 8px 12px;
        resize: none;
        outline: none;
        font-size: 14px;
        line-height: 1.4;
        max-height: 100px;
        
        &:focus {
          border-color: #007bff;
        }
      }
      
      .send-btn {
        background: #007bff;
        color: white;
        border: none;
        width: 36px;
        height: 36px;
        border-radius: 50%;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: background 0.2s;
        
        &:hover:not(:disabled) {
          background: #0056b3;
        }
        
        &:disabled {
          background: #ccc;
          cursor: not-allowed;
        }
      }
    }
  }
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 8px 0;
  
  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #999;
    animation: typing 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
    &:nth-child(3) { animation-delay: 0s; }
  }
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.chat-window-enter-active,
.chat-window-leave-active {
  transition: all 0.3s ease;
}

.chat-window-enter-from,
.chat-window-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}
</style>
