<template>
  <div class="app-page love-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon love-icon">
          <icon-heart />
        </div>
        <div class="title-text">
          <h1>AI 恋爱大师</h1>
          <span class="title-badge">情感</span>
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
      <div class="chat-content">
        <div class="welcome-card">
          <div class="welcome-glow"></div>
          <div class="welcome-content">
            <div class="welcome-icon">
              <icon-heart />
            </div>
            <h2>你好，我是AI恋爱大师！</h2>
            <p>我可以帮助你:</p>
            <ul class="feature-list">
              <li>
                <span class="feature-icon">💕</span>
                提供情感关系咨询，解答感情困惑
              </li>
              <li>
                <span class="feature-icon">🌹</span>
                给出约会建议，提高约会成功率
              </li>
              <li>
                <span class="feature-icon">💬</span>
                分析关系问题，改善沟通技巧
              </li>
              <li>
                <span class="feature-icon">✨</span>
                设计浪漫惊喜，增添感情乐趣
              </li>
            </ul>
            <p class="welcome-cta">请告诉我你的情感困扰，或直接提出你的问题！</p>
          </div>
        </div>
        
        <chat-message
          v-for="(message, index) in messages"
          :key="index"
          :text="message.content"
          :is-user="message.isUser"
          :sender-name="message.isUser ? '我' : '恋爱大师'"
          :timestamp="message.timestamp"
        />
        
        <div v-if="isLoading" class="typing-indicator">
          <a-spin dot />
          <span>恋爱大师正在回复...</span>
        </div>
      </div>
      
      <div class="chat-input">
        <a-input-search
          placeholder="告诉我你的情感疑问..."
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
import { onBeforeUnmount, ref, watch, nextTick } from 'vue';
import ChatMessage from '@/components/ChatMessage.vue';
import { connectToLoveAppSse } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';
import { IconHeart, IconDelete } from '@arco-design/web-vue/es/icon';

export default {
  name: 'LoveAppView',
  components: {
    ChatMessage,
    IconHeart,
    IconDelete
  },
  setup() {
    const CHAT_ID_KEY = 'love_app_chat_id';
    const chatId = ref('');
    const currentEventSource = ref(null);
    const inputValue = ref('');
    const isLoading = ref(false);
    const messages = ref([]);
    
    // 获取或创建聊天 ID
    chatId.value = getOrCreateChatId(CHAT_ID_KEY);
    
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
      if (window.confirm('你确定要清除所有聊天记录吗？此操作不可恢复。')) {
        if (currentEventSource.value) {
          currentEventSource.value.close();
          currentEventSource.value = null;
        }

        messages.value = [];
        localStorage.removeItem(`chat_messages_${chatId.value}`);
        localStorage.removeItem(CHAT_ID_KEY);
        chatId.value = getOrCreateChatId(CHAT_ID_KEY);
        isLoading.value = false;
      }
    };
    
    // 自动滚动到底部
    const scrollToBottom = () => {
      nextTick(() => {
        const chatContent = document.querySelector('.chat-content');
        if (chatContent) {
          chatContent.scrollTo({
            top: chatContent.scrollHeight,
            behavior: 'smooth'
          });
        }
      });
    };

    // 监听消息列表变化，自动滚动
    watch(() => messages.value.length, () => {
      scrollToBottom();
    });

    // 监听最后一条消息内容变化，自动滚动
    watch(() => messages.value[messages.value.length - 1]?.content, () => {
      if (messages.value.length > 0) {
        scrollToBottom();
      }
    });
    
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
      
      // 用于累积SSE消息的变量
      let accumulatedResponse = '';
      
      // 建立新的 SSE 连接
      const eventSource = connectToLoveAppSse(
        message,
        chatId.value,
        (data) => {
          // 累加新收到的消息片段
          accumulatedResponse += data;
          
          // 更新或创建AI回复消息
          const aiMessageIndex = messages.value.findIndex(
            msg => !msg.isUser && msg.timestamp > userMessage.timestamp
          );
          
          if (aiMessageIndex !== -1) {
            // 更新现有消息
            messages.value[aiMessageIndex].content = accumulatedResponse;
          } else {
            // 创建新消息
            const aiMessage = {
              content: accumulatedResponse,
              isUser: false,
              timestamp: Date.now()
            };
            messages.value.push(aiMessage);
          }
          
          saveMessages();
        },
        (error) => {
          console.error('SSE 连接错误:', error);
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
  background:
    radial-gradient(circle at 14% 8%, rgba(244, 114, 182, 0.2) 0%, rgba(244, 114, 182, 0) 42%),
    radial-gradient(circle at 88% 86%, rgba(225, 29, 72, 0.16) 0%, rgba(225, 29, 72, 0) 46%),
    linear-gradient(145deg, #fff7fb 0%, #fff0f6 45%, #fff9fc 100%);
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

[data-theme="dark"] .app-page {
  background:
    radial-gradient(circle at 10% 10%, rgba(225, 29, 72, 0.23) 0%, rgba(225, 29, 72, 0) 44%),
    radial-gradient(circle at 88% 90%, rgba(244, 114, 182, 0.18) 0%, rgba(244, 114, 182, 0) 50%),
    linear-gradient(145deg, #1f1119 0%, #26121c 52%, #1b0f16 100%);
}

.app-page::before,
.app-page::after {
  content: '';
  position: absolute;
  pointer-events: none;
  z-index: 0;
}

.app-page::before {
  width: 360px;
  height: 360px;
  right: -100px;
  top: -130px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(244, 114, 182, 0.22) 0%, rgba(244, 114, 182, 0) 70%);
}

.app-page::after {
  width: 420px;
  height: 420px;
  left: -150px;
  bottom: -170px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(225, 29, 72, 0.16) 0%, rgba(225, 29, 72, 0) 74%);
}

.page-header,
.chat-container {
  position: relative;
  z-index: 1;
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

.love-icon {
  background: linear-gradient(135deg, var(--color-love-light) 0%, rgba(236, 72, 153, 0.2) 100%);
  color: var(--color-love);
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
  background: linear-gradient(135deg, var(--color-love-light) 0%, rgba(236, 72, 153, 0.15) 100%);
  color: var(--color-love);
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
  border-color: var(--color-love) !important;
  color: var(--color-love) !important;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9) 0%, rgba(255, 247, 251, 0.84) 100%);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(225, 29, 72, 0.13);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 48px rgba(225, 29, 72, 0.13);
  overflow: hidden;
}

[data-theme="dark"] .chat-container {
  background: linear-gradient(180deg, rgba(45, 21, 33, 0.86) 0%, rgba(38, 18, 28, 0.92) 100%);
  border-color: rgba(244, 114, 182, 0.15);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.42);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xl);
}

.chat-content {
  background: linear-gradient(180deg, rgba(255, 236, 246, 0.52) 0%, rgba(255, 255, 255, 0) 52%);
}

[data-theme="dark"] .chat-content {
  background: linear-gradient(180deg, rgba(56, 22, 38, 0.36) 0%, rgba(56, 22, 38, 0) 52%);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.welcome-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-gray-200);
  border-radius: var(--radius-xl);
  padding: 28px;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
}

[data-theme="dark"] .welcome-card {
  background: rgba(26, 26, 36, 0.8);
  border-color: rgba(255, 255, 255, 0.08);
}

.welcome-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 0% 0%, rgba(225, 29, 72, 0.15) 0%, transparent 60%);
  pointer-events: none;
  animation: pulse 4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.welcome-content {
  position: relative;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-love-light) 0%, rgba(225, 29, 72, 0.15) 100%);
  color: var(--color-love);
  border-radius: var(--radius-lg);
  font-size: 28px;
  margin-bottom: 16px;
  position: relative;
  animation: float 3s ease-in-out infinite;
}

.welcome-icon::before {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-love), #F472B6);
  opacity: 0.3;
  filter: blur(12px);
  z-index: -1;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.welcome-content h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
  background: linear-gradient(135deg, var(--color-love) 0%, #F472B6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-content p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0 0 16px 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  font-size: 14px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--color-gray-100);
}

[data-theme="dark"] .feature-list li {
  border-bottom-color: rgba(255, 255, 255, 0.05);
}

.feature-list li:last-child {
  border-bottom: none;
}

.feature-icon {
  font-size: 18px;
}

.welcome-cta {
  font-size: 14px;
  color: var(--color-love);
  font-weight: 500;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.typing-indicator :deep(.arco-spin-dot) {
  color: var(--color-love);
}

.chat-input {
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--border-color);
  background: var(--color-gray-50);
}

[data-theme="dark"] .chat-input {
  background: var(--bg-primary);
  border-top-color: rgba(255, 255, 255, 0.08);
}

.chat-input :deep(.arco-input-search) {
  background: var(--color-bg-card);
  border-color: var(--border-color);
}

.chat-input :deep(.arco-input-search:hover),
.chat-input :deep(.arco-input-search:focus) {
  border-color: var(--color-love);
}

.chat-input :deep(.arco-input-search .arco-input) {
  background: transparent;
}

.chat-input :deep(.arco-btn-primary) {
  background: linear-gradient(135deg, var(--color-love) 0%, #F472B6 100%);
  border: none;
  position: relative;
  overflow: hidden;
}

.chat-input :deep(.arco-btn-primary)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.chat-input :deep(.arco-btn-primary:hover)::before {
  left: 100%;
}

.chat-input :deep(.arco-btn-primary:hover) {
  background: linear-gradient(135deg, #F472B6 0%, var(--color-love) 100%);
  box-shadow: 0 4px 15px rgba(225, 29, 72, 0.4);
  transform: translateY(-1px);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  color: var(--text-secondary);
  font-size: 14px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.typing-indicator :deep(.arco-spin-dot) {
  color: var(--color-love);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(0.8); }
}
</style> 