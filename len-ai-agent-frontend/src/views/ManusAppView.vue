<template>
  <div class="app-page manus-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon manus-icon">
          <icon-bulb />
        </div>
        <div class="title-text">
          <h1>AI工具智能体</h1>
          <span class="title-badge">AI</span>
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
              <icon-bulb />
            </div>
            <h2>你好，我是AI工具智能体！</h2>
            <p>我可以帮助你:</p>
            <ul class="feature-list">
              <li>
                <span class="feature-icon">💻</span>
                邮件发送
              </li>
              <li>
                <span class="feature-icon">🔍 </span>
                网络搜索与信息检索
              </li>
              <li>
                <span class="feature-icon">⏰</span>
                日期时间查询
              </li>
              <li>
                <span class="feature-icon">📊</span>
                PDF 文档生成
              </li>
              <li>
                <span class="feature-icon">🎨</span>
                HTML 页面生成
              </li>
              <li>
                <span class="feature-icon">✍️</span>
                个性化分析报告生成
              </li>
              <li>
                <span class="feature-icon">📥</span>
                资源下载
              </li>
              <li>
                <span class="feature-icon">📄 </span>
                文件操作（创建、读取、写入）
              </li>
              <li>
                <span class="feature-icon">🛑 </span>
                智能终止交互
              </li>
            </ul>
            <p class="welcome-cta">请告诉我你需要什么帮助，或者直接提出你的问题！</p>
          </div>
        </div>

        <chat-message v-for="(message, index) in messages" :key="index" :text="message.content"
          :is-user="message.isUser" :sender-name="message.isUser ? '我' : '超级智能体'" :timestamp="message.timestamp" />

        <div v-if="isLoading" class="typing-indicator">
          <div class="typing-dots">
            <span></span>
            <span></span>
            <span></span>
          </div>
          <span>超级智能体正在回复...</span>
        </div>
      </div>

      <div class="chat-input-area">
        <a-input-search placeholder="告诉我你需要什么帮助..." button-text="发送" search-button @search="sendMessage"
          v-model="inputValue" :loading="isLoading" :disabled="isLoading" @keydown="handleKeydown"
          class="input-search manus-input" />
      </div>
    </div>
  </div>
</template>

<script>
import { onBeforeUnmount, ref, watch, nextTick } from 'vue';
import ChatMessage from '@/components/ChatMessage.vue';
import { connectToManusChat } from '@/services/api';
import { getOrCreateChatId } from '@/utils/uuid';
import { IconBulb, IconDelete } from '@arco-design/web-vue/es/icon';
import { Modal } from '@arco-design/web-vue';

export default {
  name: 'ManusAppView',
  components: {
    ChatMessage,
    IconBulb,
    IconDelete
  },
  setup() {
    const CHAT_ID_KEY = 'manus_app_chat_id';
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

    const clearMessages = () => {
      Modal.confirm({
        modalClass: 'chat-confirm-modal chat-confirm-manus',
        title: '确认清除聊天记录',
        content: '此操作不可恢复，是否继续？',
        okText: '确认清除',
        cancelText: '取消',
        okButtonProps: { status: 'danger' },
        onOk: () => {
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
      });
    };

    const scrollToBottom = () => {
      nextTick(() => {
        const chatContent = document.querySelector('.chat-content');
        if (chatContent) {
          chatContent.scrollTo({ top: chatContent.scrollHeight, behavior: 'smooth' });
        }
      });
    };

    watch(() => messages.value.length, scrollToBottom);

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

      const eventSource = connectToManusChat(
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

    const handleKeydown = (e) => {
      if (e.key === 'Enter') {
        if (e.shiftKey) {
          inputValue.value += '\n';
          e.preventDefault();
        } else if (!isLoading.value) {
          sendMessage(inputValue.value);
          e.preventDefault();
        }
      }
    };

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
    radial-gradient(circle at 8% 10%, rgba(34, 211, 238, 0.16) 0%, rgba(34, 211, 238, 0) 42%),
    radial-gradient(circle at 88% 84%, rgba(14, 165, 233, 0.16) 0%, rgba(14, 165, 233, 0) 46%),
    linear-gradient(150deg, #f3fbff 0%, #edf8ff 46%, #f7fcff 100%);
  position: relative;
  overflow: hidden;
  isolation: isolate;
}

[data-theme="dark"] .app-page {
  background:
    radial-gradient(circle at 14% 8%, rgba(8, 145, 178, 0.26) 0%, rgba(8, 145, 178, 0) 44%),
    radial-gradient(circle at 88% 90%, rgba(6, 182, 212, 0.2) 0%, rgba(6, 182, 212, 0) 50%),
    linear-gradient(150deg, #0c1419 0%, #0e1b22 52%, #10181f 100%);
}

.app-page::before,
.app-page::after {
  content: '';
  position: absolute;
  pointer-events: none;
  z-index: 0;
}

.app-page::before {
  width: 380px;
  height: 380px;
  right: -120px;
  top: -140px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(34, 211, 238, 0.2) 0%, rgba(34, 211, 238, 0) 68%);
}

.app-page::after {
  width: 420px;
  height: 420px;
  left: -160px;
  bottom: -180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.16) 0%, rgba(14, 165, 233, 0) 72%);
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

.manus-icon {
  background: linear-gradient(135deg, var(--color-manus-light) 0%, rgba(6, 182, 212, 0.2) 100%);
  color: var(--color-manus);
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
  background: linear-gradient(135deg, var(--color-manus-light) 0%, rgba(6, 182, 212, 0.15) 100%);
  color: var(--color-manus);
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
  border-color: var(--color-manus) !important;
  color: var(--color-manus) !important;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.85) 0%, rgba(250, 254, 255, 0.8) 100%);
  backdrop-filter: blur(14px);
  border: 1px solid rgba(8, 145, 178, 0.14);
  border-radius: var(--radius-xl);
  box-shadow: 0 24px 48px rgba(8, 145, 178, 0.12);
  overflow: hidden;
}

[data-theme="dark"] .chat-container {
  background: linear-gradient(180deg, rgba(18, 29, 36, 0.84) 0%, rgba(16, 25, 31, 0.9) 100%);
  border-color: rgba(34, 211, 238, 0.14);
  box-shadow: 0 24px 52px rgba(0, 0, 0, 0.4);
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-xl);
  background: linear-gradient(180deg, rgba(239, 251, 255, 0.45) 0%, rgba(255, 255, 255, 0) 52%);
}

[data-theme="dark"] .chat-content {
  background: linear-gradient(180deg, rgba(9, 24, 31, 0.4) 0%, rgba(9, 24, 31, 0) 52%);
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
  background: radial-gradient(circle at 0% 0%, rgba(0, 212, 255, 0.08) 0%, transparent 60%);
  pointer-events: none;
}

.welcome-content {
  position: relative;
}

.welcome-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-manus-light);
  color: var(--color-manus);
  border-radius: var(--radius-md);
  font-size: 24px;
  margin-bottom: 16px;
}

.welcome-content h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
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
  color: var(--color-manus);
  font-weight: 500;
}

.chat-input-area {
  padding: var(--space-lg) var(--space-xl);
  border-top: 1px solid var(--border-color);
  background: var(--color-gray-50);
}

[data-theme="dark"] .chat-input-area {
  background: var(--bg-primary);
  border-top-color: rgba(255, 255, 255, 0.08);
}

.input-search :deep(.arco-btn-primary) {
  background: linear-gradient(135deg, var(--color-manus) 0%, #22D3EE 100%) !important;
  border: none;
}

.input-search :deep(.arco-btn-primary):hover {
  background: linear-gradient(135deg, #22D3EE 0%, var(--color-manus) 100%) !important;
  box-shadow: 0 8px 20px rgba(6, 182, 212, 0.3);
}

.input-search :deep(.arco-input-search) {
  background: var(--color-bg-card) !important;
  border-color: var(--border-color) !important;
}

[data-theme="dark"] .input-search :deep(.arco-input-search) {
  background: var(--bg-primary) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}

.input-search :deep(.arco-input-search):focus-within {
  border-color: var(--color-manus) !important;
  box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.15);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  color: var(--text-secondary);
  font-size: 14px;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-manus);
  animation: typingBounce 1.4s ease-in-out infinite;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingBounce {

  0%,
  60%,
  100% {
    transform: translateY(0);
  }

  30% {
    transform: translateY(-8px);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 发送按钮动画 */
.chat-input-area :deep(.arco-btn-primary) {
  position: relative;
  overflow: hidden;
}

.chat-input-area :deep(.arco-btn-primary)::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.chat-input-area :deep(.arco-btn-primary:hover)::before {
  left: 100%;
}

.chat-input-area :deep(.arco-btn-primary:hover) {
  box-shadow: 0 4px 15px rgba(8, 145, 178, 0.4);
  transform: translateY(-1px);
}
</style>