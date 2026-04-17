<template>
  <div class="app-page health-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon health-icon">
          <icon-heart-fill />
        </div>
        <div class="title-text">
          <h1>云医通健康助手</h1>
          <span class="title-badge">健康</span>
        </div>
      </div>
      <div class="page-actions">
        <a-button type="outline" class="clear-btn" @click="clearMessages">
          <template #icon>
            <icon-delete />
          </template>
          清除记录
        </a-button>
      </div>
    </div>
    
    <div class="chat-container">
      <div class="chat-description">
        我是您的健康顾问，可以提供健康知识、生活方式建议和基础医疗信息咨询。
      </div>
      
      <div class="chat-content">
        <div class="welcome-message">
          <div class="welcome-message-content">
            <h2>您好，我是云医通健康助手！</h2>
            <p>我可以帮助您：</p>
            <ul>
              <li>解答常见健康问题和医疗知识</li>
              <li>提供健康生活方式和饮食建议</li>
              <li>分享疾病预防和健康管理知识</li>
              <li>推荐适合您的健康习惯和运动方式</li>
            </ul>
            <p class="disclaimer">请注意：我提供的信息仅供参考，不能替代专业医生的诊断和建议。如有严重健康问题，请及时就医。</p>
          </div>
        </div>
        
        <chat-message
          v-for="(message, index) in messages"
          :key="index"
          :text="message.content"
          :is-user="message.isUser"
          :sender-name="message.isUser ? '我' : '健康助手'"
          :timestamp="message.timestamp"
        />
        
        <div v-if="isLoading" class="typing-indicator">
          <a-spin dot />
          <span>健康助手正在回复...</span>
        </div>
      </div>
      
      <div class="chat-input">
        <a-input-search
          placeholder="请输入您的健康问题..."
          button-text="发送"
          search-button
          @search="sendMessage"
          v-model="inputValue"
          :loading="isLoading"
          :disabled="isLoading"
          @keydown="handleKeydown"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { onBeforeUnmount, ref } from 'vue';
import ChatMessage from '@/components/ChatMessage.vue';
import { connectToHealthChat } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';
import { IconHeartFill, IconDelete } from '@arco-design/web-vue/es/icon';

export default {
  name: 'HealthAppView',
  components: {
    ChatMessage,
    IconHeartFill,
    IconDelete
  },
  setup() {
    const chatId = ref('');
    const currentEventSource = ref(null);
    const inputValue = ref('');
    const isLoading = ref(false);
    const messages = ref([]);
    const currentSteps = ref([]);
    
    // 获取或创建聊天 ID
    chatId.value = getOrCreateChatId('health_app_chat_id');
    
    // 从localStorage加载历史消息
    const loadMessages = () => {
      const savedMessages = localStorage.getItem(`chat_messages_${chatId.value}`);
      if (savedMessages) {
        messages.value = JSON.parse(savedMessages);
      }
    };
    
    // 保存消息到localStorage
    const saveMessages = () => {
      localStorage.setItem(`chat_messages_${chatId.value}`, JSON.stringify(messages.value));
    };
    
    // 加载消息历史
    loadMessages();
    
    const clearMessages = () => {
      if (window.confirm('您确定要清除所有聊天记录吗？此操作不可恢复。')) {
        messages.value = [];
        localStorage.removeItem(`chat_messages_${chatId.value}`);
      }
    };
    
    // 解析步骤函数
    const parseSteps = (text) => {
      // 使用正则表达式匹配 "Step X:" 格式
      const stepPattern = /Step \d+: 工具\[\w+\].*?(?=Step \d+:|$)/gs;
      
      // 尝试找到所有步骤
      let matches;
      try {
        matches = Array.from(text.matchAll(stepPattern));
      } catch (e) {
        console.error('正则匹配错误:', e);
        return [text];
      }
      
      // 如果没有匹配到步骤，检查是否包含部分步骤格式
      if (matches.length === 0) {
        if (text.includes('Step') && text.includes('工具[')) {
          // 可能是不完整的步骤，返回原文本
          return [text];
        }
        
        // 检查非步骤格式的其他类型消息
        if (text.trim()) {
          return [text];
        }
        
        return [];
      }
      
      // 创建步骤数组并确保每个步骤都是完整的
      const steps = matches.map(match => match[0].trim());
      return steps;
    };
    
    // 处理键盘事件
    const handleKeydown = (e) => {
      if (e.key === 'Enter') {
        if (e.shiftKey) {
          // Shift+Enter: 插入换行符
          inputValue.value += '\n';
          e.preventDefault();
        } else if (!isLoading.value) {
          // 单独的Enter: 发送消息
          sendMessage(inputValue.value);
          e.preventDefault();
        }
      }
    };
    
    // 发送消息给后端
    const sendMessage = (message) => {
      if (!message.trim() || isLoading.value) return;
      
      // 添加用户消息
      const userMessage = {
        content: message,
        isUser: true,
        timestamp: Date.now()
      };
      messages.value.push(userMessage);
      saveMessages();
      
      // 清空输入框
      inputValue.value = '';
      isLoading.value = true;
      
      // 如果有现存的连接，先关闭
      if (currentEventSource.value) {
        currentEventSource.value.close();
      }
      
      // 重置步骤数组
      currentSteps.value = [];
      let accumulatedResponse = '';
      
      // 建立新的 SSE 连接
      const eventSource = connectToHealthChat(
        message,
        (data) => {
          // 累加新收到的消息片段
          accumulatedResponse += data;
          
          // 解析出步骤
          const steps = parseSteps(accumulatedResponse);
          
          // 更新步骤数组
          if (steps.length > currentSteps.value.length) {
            // 有新的步骤
            for (let i = currentSteps.value.length; i < steps.length; i++) {
              // 为每个新步骤创建一个新的响应
              const stepMessage = {
                content: steps[i],
                isUser: false,
                timestamp: Date.now() + i
              };
              messages.value.push(stepMessage);
            }
            currentSteps.value = steps;
          } else if (steps.length > 0) {
            // 最后一个步骤有更新
            const lastStepIndex = messages.value.findIndex(
              msg => !msg.isUser && msg.content.includes(currentSteps.value[currentSteps.value.length - 1].substring(0, 20))
            );
            
            if (lastStepIndex !== -1) {
              messages.value[lastStepIndex].content = steps[steps.length - 1];
            }
            
            currentSteps.value = steps;
          }
          
          saveMessages();
        },
        (error) => {
          console.error('SSE连接错误:', error);
          isLoading.value = false;
        }
      );
      
      // 当连接关闭时
      eventSource.addEventListener('done', () => {
        isLoading.value = false;
        eventSource.close();
      });
      
      // 保存当前连接，以便后续可以关闭
      currentEventSource.value = eventSource;
    };
    
    // 组件卸载前关闭SSE连接
    onBeforeUnmount(() => {
      if (currentEventSource.value) {
        currentEventSource.value.close();
        currentEventSource.value = null;
      }
    });
    
    return {
      chatId,
      inputValue,
      isLoading,
      messages,
      sendMessage,
      clearMessages,
      handleKeydown
    };
  }
};
</script>

<style scoped>
.app-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
  background: var(--color-gray-50);
  position: relative;
}

[data-theme="dark"] .app-page {
  background: var(--bg-primary);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
  padding-bottom: var(--space-lg);
  border-bottom: 1px solid var(--border-color);
}

[data-theme="dark"] .page-header {
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.page-title-group {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.title-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  font-size: 24px;
}

.health-icon {
  background: linear-gradient(135deg, var(--color-health-light) 0%, rgba(16, 185, 129, 0.2) 100%);
  color: var(--color-health);
}

.title-text {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.title-text h1 {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.title-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--color-health-light) 0%, rgba(16, 185, 129, 0.15) 100%);
  color: var(--color-health);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.page-actions {
  display: flex;
  gap: var(--space-md);
}

.clear-btn {
  border-color: var(--border-color) !important;
  color: var(--text-secondary) !important;
}

.clear-btn:hover {
  border-color: var(--color-health) !important;
  color: var(--color-health) !important;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-card);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.chat-description {
  padding: var(--space-lg) var(--space-xl);
  font-size: 14px;
  color: var(--text-secondary);
  background: var(--color-health-bg);
  border-bottom: 1px solid var(--border-color);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xl);
}

.chat-content::-webkit-scrollbar {
  width: 8px;
}

.chat-content::-webkit-scrollbar-track {
  background: transparent;
}

.chat-content::-webkit-scrollbar-thumb {
  background-color: var(--color-mid-gray);
  border-radius: 10px;
  border: 3px solid var(--color-off-white);
}

.welcome-message {
  background-color: rgba(76, 175, 80, 0.04);
  border-radius: 18px;
  padding: 28px 32px;
  margin-bottom: 30px;
  animation: fadeScale 0.8s ease-out;
  border: 1px solid rgba(76, 175, 80, 0.15);
  box-shadow: var(--shadow-soft);
  transition: all var(--transition-smooth);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.welcome-message::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(
    90deg, 
    transparent, 
    rgba(76, 175, 80, 0.2) 30%, 
    rgba(76, 175, 80, 0.2) 70%, 
    transparent
  );
}

.welcome-message::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    radial-gradient(
      circle at 10% 10%, 
      rgba(76, 175, 80, 0.04) 0%, 
      transparent 60%
    ),
    radial-gradient(
      circle at 90% 90%, 
      rgba(76, 175, 80, 0.06) 0%, 
      transparent 60%
    );
  pointer-events: none;
}

.welcome-message:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(76, 175, 80, 0.15);
}

.welcome-message-content h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-top: 0;
  margin-bottom: 18px;
  position: relative;
  display: inline-block;
}

.welcome-message-content h2::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 0;
  width: 40%;
  height: 2px;
  background: linear-gradient(90deg, rgba(76, 175, 80, 0.5), transparent);
}

.welcome-message-content p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 14px;
  line-height: 1.6;
}

.welcome-message-content .disclaimer {
  font-size: 14px;
  color: #f57c00;
  font-style: italic;
  margin-top: 18px;
  padding: 10px;
  border-left: 3px solid #f57c00;
  background-color: rgba(245, 124, 0, 0.05);
  border-radius: 0 8px 8px 0;
}

.welcome-message-content ul {
  padding-left: 20px;
  margin-bottom: 16px;
  list-style-type: none;
}

.welcome-message-content li {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 10px;
  position: relative;
  padding-left: 24px;
  line-height: 1.5;
}

.welcome-message-content li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: rgba(76, 175, 80, 0.6);
  box-shadow: 0 0 0 4px rgba(76, 175, 80, 0.1);
}

.chat-input {
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--border-color);
  background: var(--color-gray-50);
  position: relative;
}

[data-theme="dark"] .chat-input {
  background: var(--bg-primary);
  border-top-color: rgba(255, 255, 255, 0.08);
}

.chat-input :deep(.arco-btn-primary) {
  background: linear-gradient(135deg, var(--color-health) 0%, #34D399 100%) !important;
  border: none;
}

.chat-input :deep(.arco-btn-primary):hover {
  background: linear-gradient(135deg, #34D399 0%, var(--color-health) 100%) !important;
  box-shadow: 0 6px 15px rgba(16, 185, 129, 0.3);
  transform: translateY(-2px);
}

.chat-input :deep(.arco-input-search) {
  background: var(--color-bg-card) !important;
  border-color: var(--border-color) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-sm);
}

[data-theme="dark"] .chat-input :deep(.arco-input-search) {
  background: var(--bg-primary) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

.chat-input :deep(.arco-input-search):focus-within {
  border-color: var(--color-health) !important;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.15);
}

.chat-input :deep(.arco-input) {
  font-size: 15px;
  padding: 8px 4px;
  color: var(--text-primary);
}

.chat-input :deep(.arco-input)::placeholder {
  color: var(--text-tertiary);
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  margin-bottom: 20px;
  gap: 12px;
  color: var(--text-secondary);
  font-size: 15px;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(5px);
  animation: fadeScale 0.4s ease-out;
}

.typing-indicator :deep(.arco-spin-dot) {
  color: rgba(76, 175, 80, 0.8);
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes fadeScale {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  100% {
    opacity: 1;
    transform: scale(1);
  }
}
</style> 