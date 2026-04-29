<template>
  <div class="coffee-shop-app">
    <!-- 顶部导航栏：固定 -->
    <header class="top-bar">
      <div class="top-left">
        <a-button type="text" class="back-btn" @click="goHome">
          <template #icon><icon-left /></template>
          主页
        </a-button>
      </div>
      <div class="top-center">
        <div class="brand">
          <div class="brand-icon">
            <icon-star />
          </div>
          <div class="brand-text">
            <span class="brand-name">咖啡店智能客服</span>
            <span class="brand-badge">AI 服务</span>
          </div>
        </div>
      </div>
      <div class="top-right">
        <div class="view-toggle">
          <a-button-group>
            <a-button
              :type="currentView === 'chat' ? 'primary' : 'outline'"
              size="small"
              @click="currentView = 'chat'"
            >
              <icon-message /> 用户视角
            </a-button>
            <a-button
              :type="currentView === 'admin' ? 'primary' : 'outline'"
              size="small"
              @click="currentView = 'admin'"
            >
              <icon-settings /> 管理后台
            </a-button>
          </a-button-group>
        </div>
      </div>
    </header>

    <!-- 主体：flex-1，内部不再滚动，只让 chat-content 滚动 -->
    <div class="main-body">
      <!-- 智能体介绍：固定 -->
      <div v-if="currentView === 'chat'" class="agent-intro">
        <div class="intro-card">
          <div class="intro-icon-wrap">
            <icon-star />
          </div>
          <div class="intro-content">
            <h3>咖啡店 AI 智能客服</h3>
            <p>基于大语言模型的智能点单助手，支持自然语言对话式下单、饮品推荐、订单查询、转接人工等功能。</p>
            <div class="intro-tags">
              <a-tag color="orangered">☕ 智能点单</a-tag>
              <a-tag color="arcoblue">💬 多轮对话</a-tag>
              <a-tag color="green">📋 订单追踪</a-tag>
              <a-tag color="purple">🔍 RAG 检索</a-tag>
              <a-tag color="red">🛡️ 敏感词过滤</a-tag>
              <a-tag color="magenta">👨‍💼 转人工</a-tag>
            </div>
          </div>
          <div class="intro-action">
            <a-button type="outline" class="clear-session-btn" @click="handleClearSession">
              <template #icon><icon-delete /></template>
              清空会话
            </a-button>
          </div>
        </div>
      </div>

      <!-- 视图内容：flex-1，内部 chat-input 固定底部，chat-content 滚动 -->
      <div class="view-panel">
        <keep-alive>
          <AgentChatView v-if="currentView === 'chat'" key="chat" />
          <AgentAdminView v-else key="admin" />
        </keep-alive>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, provide } from 'vue';
import { useRouter } from 'vue-router';
import AgentChatView from '@/views/AgentChatView.vue';
import AgentAdminView from '@/views/AgentAdminView.vue';
import { IconStar, IconMessage, IconSettings, IconLeft, IconDelete } from '@arco-design/web-vue/es/icon';

export default {
  name: 'CoffeeShopView',
  components: {
    AgentChatView,
    AgentAdminView,
    IconStar,
    IconMessage,
    IconSettings,
    IconLeft,
    IconDelete
  },
  setup() {
    const router = useRouter();
    const currentView = ref('chat');
    const clearTrigger = ref(0);
    provide('clearTrigger', clearTrigger);

    const goHome = () => {
      router.push('/menu/home');
    };

    const handleClearSession = () => {
      clearTrigger.value++;
    };

    return {
      currentView,
      goHome,
      handleClearSession
    };
  }
};
</script>

<style scoped>
/* ========== 根容器：撑满 + 禁止整体滚动 ========== */
.coffee-shop-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: var(--color-bg-base);
}

/* ========== 顶部导航栏：固定高度 ========== */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  height: 56px;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  z-index: 10;
}

.top-left,
.top-right {
  flex: 0 0 200px;
  display: flex;
  align-items: center;
}

.top-right {
  justify-content: flex-end;
}

.top-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.back-btn {
  color: var(--text-secondary);
  font-size: 13px;
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.back-btn:hover {
  color: var(--color-coffee);
  background: var(--color-coffee-light);
}

/* 品牌 */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-coffee-gradient);
  border-radius: 10px;
  color: white;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(139, 69, 19, 0.3);
}

.brand-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.brand-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 2px 7px;
  border-radius: 99px;
  background: var(--color-coffee-light);
  color: var(--color-coffee);
}

/* 视角切换 */
.view-toggle :deep(.arco-btn-group) {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border-radius: var(--radius-md);
}

.view-toggle :deep(.arco-btn-primary) {
  background: var(--color-coffee-gradient);
  border: none;
}

.view-toggle :deep(.arco-btn-outline) {
  border-color: var(--gray-200);
  color: var(--text-secondary);
}

.view-toggle :deep(.arco-btn-outline:hover) {
  background: var(--color-coffee-bg);
  border-color: var(--color-coffee-light);
  color: var(--color-coffee);
}

/* ========== 主体：flex-1 + overflow:hidden ========== */
.main-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: var(--space-lg);
  gap: var(--space-lg);
  min-height: 0;
}

/* 智能体介绍：flex-shrink-0 不可被压缩 */
.agent-intro {
  flex-shrink: 0;
}

.intro-card {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg) var(--space-xl);
  box-shadow: var(--shadow-sm);
}

.intro-icon-wrap {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-coffee-gradient);
  border-radius: 12px;
  color: white;
  font-size: 26px;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.25);
}

.intro-content {
  flex: 1;
  min-width: 0;
}

.intro-content h3 {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 4px;
}

.intro-content p {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 10px;
  line-height: 1.5;
}

.intro-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.intro-action {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.clear-session-btn {
  border-color: var(--gray-200);
  color: var(--text-secondary);
  font-size: 12px;
  transition: all 0.2s;
  white-space: nowrap;
}

.clear-session-btn:hover {
  border-color: #E11D48;
  color: #E11D48;
  background: rgba(225, 29, 72, 0.06);
}

/* ========== 视图面板：flex-1 + overflow:hidden ========== */
.view-panel {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  min-height: 0;
}

/* 隐藏嵌入组件自身的 header 和 welcome */
.view-panel :deep(.page-header) {
  display: none;
}

.view-panel :deep(.welcome-card) {
}

/*
  ========== 子组件布局覆写 ==========

  子组件自身使用 flex:1 + min-height:0 以适应父级 flex 容器。
  这里通过 :deep() + !important 微调内边距和溢出行为，
  确保 chat-content 是唯一的滚动区域，chat-input 固定在底部。
*/

.view-panel :deep(.coffee-app),
.view-panel :deep(.app-page) {
  flex: 1 !important;
  min-height: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
}

.view-panel :deep(.chat-container) {
  flex: 1 !important;
  border: none !important;
  border-radius: 0 !important;
  min-height: 0 !important;
  overflow: hidden !important;
}

/* 只有 chat-content 可以滚动 */
.view-panel :deep(.chat-content) {
  flex: 1 !important;
  overflow-y: auto !important;
  padding: var(--space-lg);
}

/* chat-input 固定在底部，不参与滚动 */
.view-panel :deep(.chat-input) {
  flex-shrink: 0 !important;
}

/* 管理后台 */
.view-panel :deep(.admin-app) {
  flex: 1 !important;
  min-height: 0 !important;
  padding: 0 !important;
  overflow-y: auto !important;
}

.view-panel :deep(.admin-container) {
  border: none;
  border-radius: 0;
}

/* ========== 聊天气泡 — 咖啡主题自然过渡色 ========== */

/*
  AI 消息：暖白到奶油色渐变，模拟拿铁奶泡的丝滑质感。
  170deg 角度带来轻微的倾斜光感，内阴影模拟纸张纹理。
*/
.view-panel :deep(.ai-message .message-content) {
  background: linear-gradient(170deg, #FFFDF9 0%, #FEF8F0 25%, #FCF3E6 60%, #FAEFDD 100%) !important;
  border: 1px solid #E8D5BE !important;
  box-shadow:
    0 2px 12px rgba(139, 69, 19, 0.06),
    inset 0 1px 0 rgba(255, 255, 255, 0.8) !important;
  color: #4A3728 !important;
}

/*
  用户消息：浅咖啡到暖杏色渐变，模拟烘焙咖啡豆的温润色调。
  比 AI 消息深一个色阶，对话层次一目了然。
*/
.view-panel :deep(.user-message .message-content) {
  background: linear-gradient(170deg, #F8EDDD 0%, #F3E0C5 30%, #EED7B4 70%, #E8CDA8 100%) !important;
  border: 1px solid #D4BA98 !important;
  box-shadow:
    0 2px 12px rgba(139, 69, 19, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.6) !important;
  color: #3E2E20 !important;
}

/* AI 头像：咖啡豆渐变 */
.view-panel :deep(.avatar-ai) {
  background: linear-gradient(135deg, #8B4513 0%, #C67A3D 100%) !important;
  box-shadow: 0 2px 10px rgba(139, 69, 19, 0.35) !important;
}

/* 用户头像：深焙咖啡渐变 */
.view-panel :deep(.avatar-user) {
  background: linear-gradient(135deg, #A0522D 0%, #D2875E 100%) !important;
  box-shadow: 0 2px 10px rgba(139, 69, 19, 0.35) !important;
}

/* 输入框区域：暖白底 + 咖啡色上边框 */
.view-panel :deep(.chat-input) {
  background: #FDFAF6 !important;
  border-top: 1px solid #EDD9C5 !important;
}
</style>
