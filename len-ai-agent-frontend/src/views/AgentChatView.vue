<template>
  <div class="app-page coffee-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon coffee-icon">
          <icon-star />
        </div>
        <div class="title-text">
          <h1>咖啡智能客服</h1>
          <span class="title-badge">服务</span>
        </div>
      </div>
      <div class="page-actions">
        <a-button type="outline" class="new-session-btn" @click="newSession">
          <template #icon>
            <icon-plus />
          </template>
          新建会话
        </a-button>
        <a-button type="outline" class="history-btn" @click="openSessionDrawer">
          <template #icon>
            <icon-history />
          </template>
          会话记录
        </a-button>
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
        <div v-if="messages.length === 0" class="welcome-card">
          <div class="welcome-glow"></div>
          <div class="welcome-content">
            <div class="welcome-icon">
              <icon-star />
            </div>
            <h2>你好，我是咖啡店AI智能客服！</h2>
            <p>我可以帮助你:</p>
            <ul class="feature-list">
              <li>
                <span class="feature-icon">☕</span>
                查询饮品信息、价格和口味，为你推荐最适合的咖啡
              </li>
              <li>
                <span class="feature-icon">🛒</span>
                一键下单，快速选购心仪饮品，支持自定义数量
              </li>
              <li>
                <span class="feature-icon">📋</span>
                查看历史订单记录，随时了解订单状态
              </li>
              <li>
                <span class="feature-icon">🔍</span>
                智能知识检索，为你解答咖啡相关的各种问题
              </li>
              <li>
                <span class="feature-icon">💁</span>
                转接人工客服，获取专属一对一服务
              </li>
            </ul>
            <p class="welcome-cta">请告诉我你想喝什么，或者有什么咖啡相关的问题！</p>
          </div>
        </div>

        <chat-message v-for="(message, index) in messages" :key="index" :text="message.content"
          :is-user="message.isUser" :sender-name="message.isUser ? '我' : '智能客服'" :timestamp="message.timestamp" />

        <!-- 支付按钮 - 在最后一条机器人消息且有支付信息时显示 -->
        <div v-if="showPaymentBar" class="payment-bar">
          <div class="payment-bar-card">
            <div class="payment-bar-info">
              <span class="payment-bar-text">💳 订单 <strong>{{ paymentInfo.orderNo }}</strong> — ¥{{ paymentInfo.orderTotal }}</span>
              <span class="payment-bar-detail">
                {{ paymentInfo.orderProductSummary }} · {{ paymentInfo.orderItemCount }}件 · {{ statusText(paymentInfo.orderStatus) }}
              </span>
            </div>
            <div class="payment-bar-actions">
              <a-button type="primary" class="pay-btn" @click="showPaymentDialog = true">
                去支付 <icon-right />
              </a-button>
              <a-button type="outline" class="cancel-order-btn" :loading="cancellingOrder" @click="handleCancelOrder">
                取消订单
              </a-button>
            </div>
          </div>
        </div>

        <div v-if="isLoading" class="typing-indicator">
          <a-spin dot />
          <span>{{ currentStatus || '客服正在回复...' }}</span>
        </div>
      </div>

      <div class="chat-input">
        <div class="input-row">
          <a-textarea placeholder="输入您的问题，如：有什么咖啡？我要一杯拿铁..." v-model="inputValue" :disabled="isLoading"
            @keydown="handleKeydown" :auto-size="{ minRows: 1, maxRows: 4 }" class="chat-textarea" />
        </div>
        <div class="input-actions">
          <a-button type="outline" class="transfer-btn" @click="transferToHuman" :disabled="isLoading">
            <icon-user-group />
            转人工
          </a-button>
          <a-button type="primary" class="send-btn" @click="sendMessage" :loading="isLoading"
            :disabled="!inputValue.trim()">
            发送
          </a-button>
        </div>
      </div>
    </div>

    <!-- 会话记录抽屉 -->
    <a-drawer v-model:visible="showSessionDrawer" title="会话记录" :width="380" placement="right" :footer="false" class="session-drawer">
      <div v-if="sessionList.length === 0" class="session-empty">
        <icon-empty />
        <p>暂无历史会话</p>
      </div>
      <div v-else class="session-list">
        <div
          v-for="item in sessionList"
          :key="item.sessionId"
          class="session-item"
          :class="{ 'session-active': item.sessionId === chatId }"
          @click="switchToSession(item.sessionId)"
        >
          <div class="session-item-main">
            <span class="session-item-title">{{ item.title || '新会话' }}</span>
            <span class="session-item-meta">{{ item.messageCount || 0 }} 条消息 · {{ relativeTime(item.updateTime || item.createTime) }}</span>
          </div>
          <div class="session-item-status">
            <span v-if="item.sessionId === chatId" class="current-badge">当前</span>
            <icon-right v-else />
          </div>
        </div>
      </div>
    </a-drawer>

    <!-- 支付选择弹窗 -->
    <a-modal v-model:visible="showPaymentDialog" title="选择支付方式" :footer="false" width="420px" modal-class="payment-modal">
      <div class="payment-content">
        <p class="payment-info">订单 <strong>{{ paymentInfo.orderNo }}</strong> · 金额 <strong>¥{{ paymentInfo.orderTotal }}</strong></p>
        <div class="payment-options">
          <div class="payment-option" @click="handlePay('alipay')">
            <div class="pay-icon alipay-icon">支</div>
            <span>支付宝</span>
            <icon-right />
          </div>
          <div class="payment-option" @click="handlePay('wechat')">
            <div class="pay-icon wechat-icon">微</div>
            <span>微信支付</span>
            <icon-right />
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
import { onBeforeUnmount, ref, watch, nextTick, inject } from 'vue';
import ChatMessage from '@/components/ChatMessage.vue';
import { agentChat, agentCancelOrder, agentClearSession, agentGetSessions } from '@/services/api';
import { getOrCreateChatId, generateUUID } from '@/utils/uuid';
import { IconStar, IconDelete, IconUserGroup, IconRight, IconPlus, IconHistory, IconEmpty } from '@arco-design/web-vue/es/icon';
import { Modal, Message } from '@arco-design/web-vue';

export default {
  name: 'AgentChatView',
  components: {
    ChatMessage,
    IconStar,
    IconDelete,
    IconUserGroup,
    IconRight,
    IconPlus,
    IconHistory,
    IconEmpty
  },
  setup() {
    const CHAT_ID_KEY = 'coffee_agent_chat_id';
    const chatId = ref('');
    const inputValue = ref('');
    const isLoading = ref(false);
    const messages = ref([]);
    const currentResponse = ref('');
    const currentStatus = ref('');
    const showPaymentBar = ref(false);
    const showPaymentDialog = ref(false);
    const cancellingOrder = ref(false);
    const paymentInfo = ref({ orderNo: '', orderTotal: '0' });
    const showSessionDrawer = ref(false);
    const sessionList = ref([]);

    const SESSION_LIST_KEY = 'coffee_session_list';

    const statusText = (s) => ({ PENDING: '待确认', CONFIRMED: '已确认', PREPARING: '制作中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s);

    const NODE_STATUS_MAP = {
      preprocessNode: '正在理解您的问题...',
      intentClassifierNode: '正在分析您的意图...',
      productDialogNode: '正在为您查询饮品信息...',
      orderDialogNode: '正在处理您的订单...',
      orderHistoryNode: '正在查询您的订单记录...',
      chitchatNode: '正在回复...',
      knowledgeRetrievalNode: '正在检索相关知识...',
      transferToHumanNode: '正在为您转接人工客服...',
      errorHandlerNode: '正在处理...'
    };

    // 监听父组件（CoffeeShopView）的清空会话事件
    const clearTrigger = inject('clearTrigger', ref(0));
    watch(clearTrigger, (newVal) => {
      if (newVal > 0) doClearSession();
    });

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

    const scrollToBottom = () => {
      nextTick(() => {
        const container = document.querySelector('.chat-content');
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    };

    const sendMessage = () => {
      const text = inputValue.value.trim();
      if (!text || isLoading.value) return;

      messages.value.push({
        content: text,
        isUser: true,
        timestamp: Date.now()
      });
      inputValue.value = '';
      isLoading.value = true;
      currentResponse.value = '';
      showPaymentBar.value = false;

      // 暂存支付信息（在 response 事件中设置，在 finish 中使用）
      let pendingPayment = null;

      agentChat(
        text,
        chatId.value,
        (event) => {
          if (event.event === 'status') {
            currentStatus.value = NODE_STATUS_MAP[event.data.node] || '正在处理...';
          } else if (event.event === 'response') {
            currentResponse.value += event.data.content || '';
            if (event.data.paymentEnabled) {
              pendingPayment = {
                orderNo: event.data.orderNo,
                orderTotal: event.data.orderTotal,
                orderStatus: event.data.orderStatus,
                orderCreateTime: event.data.orderCreateTime,
                orderProductSummary: event.data.orderProductSummary,
                orderItemCount: event.data.orderItemCount
              };
            }
          } else if (event.event === 'error') {
            currentResponse.value = event.data.message || '系统异常，请稍后重试';
          }
        },
        (err) => {
          console.error('Agent chat error:', err);
          currentResponse.value = '抱歉，连接异常，请稍后重试。';
          finishResponse();
        },
        () => {
          finishResponse(pendingPayment);
        }
      );
    };

    const finishResponse = (pendingPayment) => {
      if (currentResponse.value) {
        messages.value.push({
          content: currentResponse.value,
          isUser: false,
          timestamp: Date.now()
        });
      }
      // 如果有支付信息则显示支付按钮条
      if (pendingPayment) {
        paymentInfo.value = pendingPayment;
        showPaymentBar.value = true;
      }
      currentResponse.value = '';
      currentStatus.value = '';
      isLoading.value = false;
      saveMessages();
    };

    const transferToHuman = () => {
      inputValue.value = '转人工';
      sendMessage();
    };

    const doClearSession = () => {
      // 保存当前会话到记录列表
      saveCurrentSessionToList();
      // 关闭服务端会话
      const oldChatId = chatId.value;
      agentClearSession(oldChatId).catch(() => { /* 忽略服务端错误 */ });
      // 清除本地消息
      messages.value = [];
      localStorage.removeItem(`chat_messages_${oldChatId}`);
      // 生成新的本地会话ID
      const newChatId = generateUUID();
      localStorage.setItem(CHAT_ID_KEY, newChatId);
      chatId.value = newChatId;
      showPaymentBar.value = false;
    };

    const clearMessages = () => {
      Modal.confirm({
        title: '确认清除',
        content: '确定要清除所有聊天记录并关闭当前会话吗？此操作不可撤销。',
        okText: '确定',
        cancelText: '取消',
        modalClass: 'chat-confirm-modal chat-confirm-coffee',
        onOk: () => {
          doClearSession();
        }
      });
    };

    const newSession = () => {
      if (messages.value.length === 0) {
        // 无消息时直接生成新会话
        doClearSession();
        return;
      }
      Modal.confirm({
        title: '新建会话',
        content: '当前会话将被关闭并保存到历史记录，确定要开始新会话吗？',
        okText: '确定',
        cancelText: '取消',
        modalClass: 'chat-confirm-modal chat-confirm-coffee',
        onOk: () => {
          doClearSession();
        }
      });
    };

    // ==================== 会话记录管理 ====================

    const getSessionList = () => {
      try {
        return JSON.parse(localStorage.getItem(SESSION_LIST_KEY) || '[]');
      } catch { return []; }
    };

    const saveCurrentSessionToList = () => {
      if (messages.value.length === 0) return;
      const list = getSessionList();
      const firstUserMsg = messages.value.find(m => m.isUser);
      const title = firstUserMsg ? firstUserMsg.content.slice(0, 30) : '新会话';
      const idx = list.findIndex(s => s.sessionId === chatId.value);
      const entry = {
        sessionId: chatId.value,
        title,
        messageCount: messages.value.length,
        updateTime: Date.now()
      };
      if (idx >= 0) {
        list[idx] = { ...list[idx], ...entry };
      } else {
        entry.createTime = Date.now();
        list.unshift(entry);
      }
      if (list.length > 50) list.length = 50;
      localStorage.setItem(SESSION_LIST_KEY, JSON.stringify(list));
    };

    const syncSessionsFromServer = async () => {
      const localList = getSessionList();
      if (localList.length === 0) return;
      const ids = localList.map(s => s.sessionId);
      try {
        const res = await agentGetSessions(ids);
        const serverSessions = res.data?.data || [];
        for (const ss of serverSessions) {
          const local = localList.find(l => l.sessionId === ss.sessionId);
          if (local && ss.title) {
            local.title = ss.title;
            local.intent = ss.intent;
            local.status = ss.status;
            if (ss.createTime) local.createTime = new Date(ss.createTime).getTime();
          }
        }
        localStorage.setItem(SESSION_LIST_KEY, JSON.stringify(localList));
      } catch { /* 服务端同步失败不影响本地使用 */ }
    };

    const openSessionDrawer = async () => {
      saveCurrentSessionToList();
      await syncSessionsFromServer();
      sessionList.value = getSessionList();
      showSessionDrawer.value = true;
    };

    const switchToSession = (sessionId) => {
      if (sessionId === chatId.value) {
        showSessionDrawer.value = false;
        return;
      }
      saveCurrentSessionToList();
      saveMessages();
      chatId.value = sessionId;
      localStorage.setItem(CHAT_ID_KEY, sessionId);
      showPaymentBar.value = false;
      messages.value = [];
      loadMessages();
      showSessionDrawer.value = false;
    };

    const relativeTime = (ts) => {
      if (!ts) return '';
      const diff = Date.now() - ts;
      if (diff < 60000) return '刚刚';
      if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
      if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
      if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
      return new Date(ts).toLocaleDateString('zh-CN');
    };

    const handleKeydown = (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage();
      }
    };

    const handlePay = (type) => {
      showPaymentDialog.value = false;
      const name = type === 'alipay' ? '支付宝' : '微信支付';
      Message.info(`${name}支付功能暂未开发，敬请期待！`);
    };

    const handleCancelOrder = () => {
      Modal.confirm({
        title: '确认取消订单',
        content: `确定要取消订单 ${paymentInfo.value.orderNo} 吗？取消后库存会自动恢复。`,
        okText: '确定取消',
        cancelText: '暂不取消',
        modalClass: 'chat-confirm-modal chat-confirm-coffee',
        onOk: async () => {
          cancellingOrder.value = true;
          try {
            const res = await agentCancelOrder(paymentInfo.value.orderNo);
            if (res.data.code === 200) {
              Message.success('订单已取消');
              showPaymentBar.value = false;
            } else {
              Message.error(res.data.message || '取消订单失败');
            }
          } catch (e) {
            Message.error(e.response?.data?.message || '取消订单失败，请稍后重试');
          } finally {
            cancellingOrder.value = false;
          }
        }
      });
    };

    watch(messages, () => {
      scrollToBottom();
    }, { deep: true });

    onBeforeUnmount(() => {
      saveMessages();
      saveCurrentSessionToList();
    });

    return {
      inputValue,
      isLoading,
      messages,
      currentStatus,
      showPaymentBar,
      showPaymentDialog,
      cancellingOrder,
      paymentInfo,
      showSessionDrawer,
      sessionList,
      chatId,
      sendMessage,
      clearMessages,
      newSession,
      handleKeydown,
      transferToHuman,
      handlePay,
      handleCancelOrder,
      statusText,
      openSessionDrawer,
      switchToSession,
      relativeTime
    };
  }
};
</script>

<style scoped>
.coffee-app {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-lg);
  flex-shrink: 0;
}

.page-title-group {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.title-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 22px;
}

.coffee-icon {
  background: var(--color-coffee-gradient);
  color: white;
}

.title-text h1 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.title-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 99px;
  background: var(--color-coffee-light);
  color: var(--color-coffee);
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  overflow: hidden;
  position: relative;
}

.chat-content {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-lg) var(--space-xl);
}

.welcome-card {
  background: var(--color-coffee-bg);
  border: 1px solid var(--color-coffee-light);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  margin-bottom: var(--space-xl);
  position: relative;
  overflow: hidden;
}

.welcome-glow {
  position: absolute;
  top: -30px;
  right: -30px;
  width: 120px;
  height: 120px;
  background: var(--color-coffee-gradient);
  opacity: 0.1;
  border-radius: 50%;
  filter: blur(20px);
}

.welcome-content {
  position: relative;
  z-index: 1;
}

.welcome-content .welcome-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-coffee-gradient);
  border-radius: var(--radius-md);
  color: white;
  font-size: 24px;
  margin-bottom: var(--space-md);
}

.welcome-content h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-sm);
}

.welcome-content p {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: var(--space-sm);
}

.feature-list {
  list-style: none;
  padding: 0;
  margin-bottom: var(--space-md);
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: 6px 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.feature-icon {
  font-size: 16px;
}

.welcome-cta {
  font-size: 13px;
  color: var(--color-coffee);
  font-weight: 500;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md);
  color: var(--text-tertiary);
  font-size: 13px;
}

.chat-input {
  border-top: 1px solid var(--border-color);
  padding: var(--space-md) var(--space-xl);
  background: var(--color-bg-card);
  flex-shrink: 0;
  box-shadow: 0 -1px 3px rgba(0, 0, 0, 0.03);
}

.input-row {
  margin-bottom: var(--space-sm);
}

.chat-textarea {
  width: 100%;
}

.chat-textarea :deep(textarea) {
  background: var(--gray-50);
  border-color: var(--gray-200);
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 14px;
  transition: all 0.2s;
}

.chat-textarea :deep(textarea):focus {
  border-color: var(--color-coffee);
  box-shadow: 0 0 0 2px rgba(139, 69, 19, 0.1);
}

.chat-textarea :deep(textarea):hover {
  border-color: var(--gray-300);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-sm);
}

.transfer-btn {
  border-color: var(--color-coffee);
  color: var(--color-coffee);
}

.transfer-btn:hover {
  background: var(--color-coffee-light);
  border-color: var(--color-coffee);
  color: var(--color-coffee);
}

.send-btn {
  background: var(--color-coffee-gradient);
  border: none;
}

.send-btn:hover {
  background: linear-gradient(135deg, #A0522D 0%, #E07B39 100%);
}

.clear-btn {
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.clear-btn:hover {
  border-color: #EF4444;
  color: #EF4444;
}

.new-session-btn {
  border-color: var(--color-coffee);
  color: var(--color-coffee);
}

.new-session-btn:hover {
  background: var(--color-coffee-light);
  border-color: var(--color-coffee);
  color: var(--color-coffee);
}

.history-btn {
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.history-btn:hover {
  border-color: var(--color-coffee);
  color: var(--color-coffee);
  background: var(--color-coffee-bg);
}

/* 支付按钮条 */
.payment-bar {
  display: flex;
  justify-content: center;
  padding: var(--space-sm) 0 var(--space-md);
}

.payment-bar-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: var(--space-md);
  background: linear-gradient(135deg, #FFF8F0 0%, #FFF3E6 100%);
  border: 1px solid var(--color-coffee-light);
  border-radius: var(--radius-lg);
  padding: var(--space-md) var(--space-lg);
}

.payment-bar-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.payment-bar-text {
  font-size: 14px;
  color: var(--text-primary);
}

.payment-bar-detail {
  font-size: 12px;
  color: var(--text-tertiary);
}

.payment-bar-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.pay-btn {
  background: var(--color-coffee-gradient);
  border: none;
  display: flex;
  align-items: center;
  gap: 4px;
}

.pay-btn:hover {
  background: linear-gradient(135deg, #A0522D 0%, #E07B39 100%);
}

.cancel-order-btn {
  border-color: #EF4444;
  color: #EF4444;
}

.cancel-order-btn:hover {
  background: #FEF2F2;
  border-color: #DC2626;
  color: #DC2626;
}

/* 支付弹窗 */
.payment-content {
  text-align: center;
}

.payment-info {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: var(--space-lg);
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.payment-option {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.payment-option:hover {
  border-color: var(--color-coffee);
  background: var(--color-coffee-bg);
}

.pay-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  font-size: 16px;
  font-weight: 700;
  color: white;
  flex-shrink: 0;
}

.alipay-icon {
  background: linear-gradient(135deg, #1677FF, #69B1FF);
}

.wechat-icon {
  background: linear-gradient(135deg, #07C160, #6FDB6A);
}

.payment-option span {
  flex: 1;
  text-align: left;
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

/* 会话记录抽屉 */
.session-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
  color: var(--text-tertiary);
  font-size: 14px;
  gap: var(--space-md);
}

.session-empty :deep(.arco-icon) {
  font-size: 48px;
  color: var(--gray-300);
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  gap: var(--space-md);
}

.session-item:hover {
  border-color: var(--color-coffee);
  background: var(--color-coffee-bg);
}

.session-active {
  border-color: var(--color-coffee);
  background: var(--color-coffee-light);
}

.session-item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
  flex: 1;
}

.session-item-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-item-meta {
  font-size: 12px;
  color: var(--text-tertiary);
}

.session-item-status {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  color: var(--text-tertiary);
  font-size: 12px;
}

.current-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 99px;
  background: var(--color-coffee-light);
  color: var(--color-coffee);
  font-weight: 600;
}
</style>
