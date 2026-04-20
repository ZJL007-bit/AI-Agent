<template>
  <div class="app-page exam-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon exam-icon">
          <icon-book />
        </div>
        <div class="title-text">
          <h1>智慧答题助手</h1>
          <span class="title-badge">学习</span>
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
              <icon-book />
            </div>
            <h2>你好，我是智慧答题助手！</h2>
            <p>我可以帮助你:</p>
            <ul class="feature-list">
              <li>
                <span class="feature-icon">📚</span>
                解答各类学科问题，从数学、英语到物理、化学等
              </li>
              <li>
                <span class="feature-icon">🧩</span>
                分析复杂题目，提供解题思路和步骤
              </li>
              <li>
                <span class="feature-icon">💡</span>
                讲解知识点，帮助你更好地理解概念
              </li>
              <li>
                <span class="feature-icon">📝</span>
                提供考试技巧和学习方法建议
              </li>
            </ul>
            <p class="welcome-cta">请告诉我你想了解什么，或者直接提出你的问题！</p>
          </div>
        </div>
        
        <chat-message
          v-for="(message, index) in messages"
          :key="index"
          :text="message.content"
          :is-user="message.isUser"
          :sender-name="message.isUser ? '我' : '答题助手'"
          :timestamp="message.timestamp"
        />
        
        <div v-if="isLoading" class="typing-indicator">
          <a-spin dot />
          <span>答题助手正在回复...</span>
        </div>
      </div>
      
      <div class="chat-input">
        <a-input-search
          placeholder="输入你的问题..."
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
import { connectToQuizChat } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';
import { IconBook, IconDelete } from '@arco-design/web-vue/es/icon';

export default {
  name: 'ExamAppView',
  components: {
    ChatMessage,
    IconBook,
    IconDelete
  },
  setup() {
    const CHAT_ID_KEY = 'exam_app_chat_id';
    const chatId = ref('');
    const currentEventSource = ref(null);
    const inputValue = ref('');
    const isLoading = ref(false);
    const messages = ref([]);
    const stepMessageIndexes = ref({});
    const finalMessageIndex = ref(null);

    chatId.value = getOrCreateChatId(CHAT_ID_KEY);

    const loadMessages = () => {
      const savedMessages = localStorage.getItem(`chat_messages_${chatId.value}`);
      if (savedMessages) {
        messages.value = JSON.parse(savedMessages);
      }
    };

    const saveMessages = () => {
      localStorage.setItem(`chat_messages_${chatId.value}`, JSON.stringify(messages.value));
    };

    loadMessages();
    
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
    
    const sendMessage = (message) => {
      if (!message.trim() || isLoading.value) return;
      
      const userMessage = {
        content: message,
        isUser: true,
        timestamp: Date.now()
      };
      messages.value.push(userMessage);
      saveMessages();

      inputValue.value = '';
      isLoading.value = true;
      
      if (currentEventSource.value) {
        currentEventSource.value.close();
      }
      
      stepMessageIndexes.value = {};
      finalMessageIndex.value = null;
      
      const eventSource = connectToQuizChat(
        message,
        (data) => {
          const text = typeof data === 'string' ? data : String(data || '');
          if (!text.trim()) {
            return;
          }

          const trimmedText = text.trim();
          const stepMatch = trimmedText.match(/^Step\s+(\d+):/);

          if (stepMatch) {
            const stepNumber = Number(stepMatch[1]);
            const existingIndex = stepMessageIndexes.value[stepNumber];

            if (typeof existingIndex === 'number' && messages.value[existingIndex]) {
              messages.value[existingIndex].content = trimmedText;
            } else {
              const stepMessage = {
                content: trimmedText,
                isUser: false,
                timestamp: Date.now() + stepNumber
              };
              messages.value.push(stepMessage);
              stepMessageIndexes.value[stepNumber] = messages.value.length - 1;
            }
          } else if (typeof finalMessageIndex.value === 'number' && messages.value[finalMessageIndex.value]) {
            messages.value[finalMessageIndex.value].content += text;
          } else {
            const finalMessage = {
              content: text,
              isUser: false,
              timestamp: Date.now() + 999
            };
            messages.value.push(finalMessage);
            finalMessageIndex.value = messages.value.length - 1;
          }
          
          saveMessages();
        },
        (error) => {
          console.error('SSE 连接错误:', error);
          isLoading.value = false;
          currentEventSource.value = null;
        }
      );
      
      eventSource.addEventListener('done', () => {
        isLoading.value = false;
        saveMessages();
        eventSource.close();
        currentEventSource.value = null;
      });
      
      currentEventSource.value = eventSource;
    };

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
        stepMessageIndexes.value = {};
        finalMessageIndex.value = null;
        isLoading.value = false;
      }
    };
    
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

    watch(() => messages.value.length, () => {
      scrollToBottom();
    });

    watch(() => messages.value[messages.value.length - 1]?.content, () => {
      if (messages.value.length > 0) {
        scrollToBottom();
      }
    });
    
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
}
</script>

<style scoped>
.app-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
  background:
    radial-gradient(circle at 10% 8%, rgba(167, 139, 250, 0.18) 0%, rgba(167, 139, 250, 0) 42%),
    radial-gradient(circle at 86% 86%, rgba(139, 92, 246, 0.16) 0%, rgba(139, 92, 246, 0) 46%),
    linear-gradient(145deg, #fbf8ff 0%, #f5f1ff 45%, #fcfbff 100%);
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

[data-theme="dark"] .app-page {
  background:
    radial-gradient(circle at 12% 10%, rgba(124, 58, 237, 0.22) 0%, rgba(124, 58, 237, 0) 44%),
    radial-gradient(circle at 88% 92%, rgba(147, 51, 234, 0.18) 0%, rgba(147, 51, 234, 0) 50%),
    linear-gradient(145deg, #161222 0%, #1a1529 52%, #151221 100%);
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
  right: -120px;
  top: -120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.22) 0%, rgba(167, 139, 250, 0) 70%);
}

.app-page::after {
  width: 420px;
  height: 420px;
  left: -150px;
  bottom: -180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(139, 92, 246, 0.16) 0%, rgba(139, 92, 246, 0) 74%);
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

.exam-icon {
  background: linear-gradient(135deg, var(--color-exam-light) 0%, rgba(139, 92, 246, 0.2) 100%);
  color: var(--color-exam);
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
  background: linear-gradient(135deg, var(--color-exam-light) 0%, rgba(139, 92, 246, 0.15) 100%);
  color: var(--color-exam);
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
  border-color: var(--color-exam) !important;
  color: var(--color-exam) !important;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.88) 0%, rgba(251, 249, 255, 0.82) 100%);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(124, 58, 237, 0.14);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 48px rgba(124, 58, 237, 0.14);
  overflow: hidden;
}

[data-theme="dark"] .chat-container {
  background: linear-gradient(180deg, rgba(33, 26, 54, 0.86) 0%, rgba(28, 22, 46, 0.92) 100%);
  border-color: rgba(167, 139, 250, 0.16);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.42);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xl);
}

.chat-content {
  background: linear-gradient(180deg, rgba(247, 240, 255, 0.55) 0%, rgba(255, 255, 255, 0) 52%);
}

[data-theme="dark"] .chat-content {
  background: linear-gradient(180deg, rgba(41, 31, 66, 0.38) 0%, rgba(41, 31, 66, 0) 52%);
}

.chat-icon::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 40px;
  height: 40px;
  background-color: rgba(97, 0, 205, 0.06);
  border-radius: 50%;
  z-index: -1;
  animation: pulse var(--transition-breathe);
}

.chat-title h1 {
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
  letter-spacing: 0.4px;
}

.chat-description {
  width: 100%;
  margin-bottom: 30px;
  font-size: 15px;
  color: var(--text-secondary);
  padding: 0 2px;
  line-height: 1.6;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 12px;
  scrollbar-width: thin;
  scrollbar-color: var(--color-mid-gray) transparent;
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
  background-color: rgba(97, 0, 205, 0.03);
  border-radius: 18px;
  padding: 28px 32px;
  margin-bottom: 30px;
  animation: fadeScale 0.8s ease-out;
  border: 1px solid rgba(97, 0, 205, 0.1);
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
    rgba(97, 0, 205, 0.15) 30%, 
    rgba(97, 0, 205, 0.15) 70%, 
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
      rgba(97, 0, 205, 0.03) 0%, 
      transparent 60%
    ),
    radial-gradient(
      circle at 90% 90%, 
      rgba(97, 0, 205, 0.05) 0%, 
      transparent 60%
    );
  pointer-events: none;
}

.welcome-message:hover {
  transform: translateY(-3px);
  box-shadow: 0 10px 30px rgba(97, 0, 205, 0.12);
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
  background: linear-gradient(90deg, rgba(97, 0, 205, 0.4), transparent);
}

.welcome-message-content p {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 14px;
  line-height: 1.6;
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
  background-color: rgba(97, 0, 205, 0.5);
  box-shadow: 0 0 0 4px rgba(97, 0, 205, 0.08);
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

.chat-input :deep(.arco-btn-primary) {
  background: linear-gradient(135deg, var(--color-exam) 0%, #A78BFA 100%) !important;
  border: none;
}

.chat-input :deep(.arco-btn-primary):hover {
  background: linear-gradient(135deg, #A78BFA 0%, var(--color-exam) 100%) !important;
  box-shadow: 0 8px 20px rgba(139, 92, 246, 0.3);
}

.chat-input :deep(.arco-input-search) {
  background: var(--color-bg-card) !important;
  border-color: var(--border-color) !important;
}

[data-theme="dark"] .chat-input :deep(.arco-input-search) {
  background: var(--bg-primary) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

.chat-input :deep(.arco-input-search):focus-within {
  border-color: var(--color-exam) !important;
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.15);
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
  color: var(--color-exam);
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