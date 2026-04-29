<template>
  <div class="app-layout" :data-theme="theme">
    <!-- 顶部导航栏 -->
    <header class="app-header">
      <div class="header-left">
        <div class="logo-group" @click="goHome">
          <div class="logo-icon">
            <icon-robot />
          </div>
          <div class="logo-text">
            <span class="logo-name">AI-Agent</span>
            <span class="logo-desc">智能矩阵应用平台</span>
          </div>
        </div>
      </div>

      <div class="header-center">
        <div class="breadcrumb">
          <span class="breadcrumb-current">{{ currentPageTitle }}</span>
        </div>
      </div>

      <div class="header-right">
        <div class="header-actions">
          <a-tooltip content="深色模式" position="bottom">
            <div class="action-btn" @click="toggleTheme">
              <icon-sun v-if="theme === 'dark'" />
              <icon-moon v-else />
            </div>
          </a-tooltip>
        </div>
        <div class="user-menu">
          <a-avatar :size="32" class="user-avatar">
            <icon-user />
          </a-avatar>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <div class="app-body">
      <!-- 侧边栏 -->
      <aside class="app-sidebar">
        <div class="sidebar-menu">
          <div class="menu-group">
            <div class="menu-group-title">导航</div>
            <div class="menu-item menu-item-home" :class="{ active: isActiveRoute('/menu/home') }"
              @click="goTo('/menu/home')">
              <div class="menu-icon">
                <icon-home />
              </div>
              <span class="menu-text">主页</span>
            </div>
          </div>

          <div class="menu-divider"></div>

          <div class="menu-group">
            <div class="menu-group-title">智能体应用</div>

            <div class="menu-item menu-item-coffee" :class="{ active: isActiveRoute('/menu/coffee-shop') }"
              @click="goTo('/coffee-shop')">
              <div class="menu-icon coffee">
                <icon-star />
              </div>
              <span class="menu-text">咖啡店智能客服</span>
              <span class="menu-badge">服务</span>
            </div>
            <div class="menu-item menu-item-love" :class="{ active: isActiveRoute('/menu/love-app') }"
              @click="goTo('/menu/love-app')">
              <div class="menu-icon love">
                <icon-heart />
              </div>
              <span class="menu-text">AI 恋爱大师</span>
              <span class="menu-badge">情感</span>
            </div>

            <div class="menu-item menu-item-manus" :class="{ active: isActiveRoute('/menu/manus-app') }"
              @click="goTo('/menu/manus-app')">
              <div class="menu-icon manus">
                <icon-robot />
              </div>
              <span class="menu-text">AI 工具智能体</span>
              <span class="menu-badge">AI</span>
            </div>

            <div class="menu-item menu-item-health" :class="{ active: isActiveRoute('/menu/health-app') }"
              @click="goTo('/menu/health-app')">
              <div class="menu-icon health">
                <icon-heart-fill />
              </div>
              <span class="menu-text">云医通健康助手</span>
              <span class="menu-badge">健康</span>
            </div>


          </div>
        </div>

        <div class="sidebar-footer">
          <div class="version-info">
            <span>Len-AI v1.0.0</span>
          </div>
        </div>
      </aside>

      <!-- 内容区 -->
      <main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  IconHeart,
  IconRobot,
  IconHome,
  IconHeartFill,
  IconSun,
  IconMoon,
  IconUser,
  IconStar
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'App',
  components: {
    IconHeart,
    IconRobot,
    IconHome,
    IconHeartFill,
    IconSun,
    IconMoon,
    IconUser,
    IconStar
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const theme = ref('light');

    const pageTitles = {
      '/': '主页',
      '/home': '主页',
      '/menu/home': '主页',
      '/coffee-shop': '咖啡店智能客服',
      '/menu/love-app': 'AI 恋爱大师',
      '/menu/manus-app': 'AI 工具智能体',
      '/menu/health-app': '云医通健康助手'
      
    };

    const currentPageTitle = computed(() => {
      return pageTitles[route.path] || '主页';
    });

    const currentRoute = computed(() => route.path);

    const isActiveRoute = (targetPath) => {
      if (targetPath === '/menu/home') {
        return ['/', '/home', '/menu/home'].includes(currentRoute.value);
      }
      return currentRoute.value === targetPath || currentRoute.value.startsWith(`${targetPath}/`);
    };

    onMounted(() => {
      const savedTheme = localStorage.getItem('theme');
      if (savedTheme) {
        theme.value = savedTheme;
        document.documentElement.setAttribute('data-theme', savedTheme);
      }
    });

    const toggleTheme = () => {
      theme.value = theme.value === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', theme.value);
      document.documentElement.setAttribute('data-theme', theme.value);
    };

    const goTo = (path) => {
      router.push(path);
    };

    const goHome = () => {
      router.push('/');
    };

    return {
      theme,
      currentPageTitle,
      currentRoute,
      isActiveRoute,
      toggleTheme,
      goTo,
      goHome
    };
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  height: 100%;
}

/* 布局 */
.app-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-base);
}

/* 顶部导航 */
.app-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--space-xl);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-group {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  cursor: pointer;
}

.logo-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary), #3B82F6);
  border-radius: var(--radius-md);
  color: white;
  font-size: 18px;
}

.logo-text {
  display: flex;
  flex-direction: column;
}

.logo-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.2;
}

.logo-desc {
  font-size: 11px;
  color: var(--text-tertiary);
}

.header-center {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.breadcrumb-current {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.action-btn:hover {
  background: var(--gray-100);
  color: var(--text-primary);
}

[data-theme="dark"] .action-btn:hover {
  background: var(--gray-800);
}

.user-menu {
  cursor: pointer;
}

.user-avatar {
  background: var(--color-primary-light);
  color: var(--color-primary);
  cursor: pointer;
}

/* 主体 */
.app-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.app-sidebar {
  width: 240px;
  background: var(--color-bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-menu {
  flex: 1;
  padding: var(--space-lg);
  overflow-y: auto;
}

.menu-group {
  margin-bottom: var(--space-lg);
}

.menu-group-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 0 var(--space-md);
  margin-bottom: var(--space-sm);
}

.menu-divider {
  height: 1px;
  background: var(--border-color);
  margin: var(--space-lg) 0;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--text-secondary);
}

.menu-item:hover {
  background: var(--gray-100);
  color: var(--text-primary);
}

[data-theme="dark"] .menu-item:hover {
  background: var(--gray-800);
}

.menu-item.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.menu-item.menu-item-home.active {
  background: var(--color-primary-light);
  color: var(--color-primary);
}

.menu-item.menu-item-love.active {
  background: var(--color-love-light);
  color: var(--color-love);
}

.menu-item.menu-item-manus.active {
  background: var(--color-manus-light);
  color: var(--color-manus);
}

.menu-item.menu-item-health.active {
  background: var(--color-health-light);
  color: var(--color-health);
}

.menu-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--gray-100);
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.menu-item.active .menu-icon {
  background: var(--color-primary);
  color: white;
}

.menu-icon.love {
  background: var(--color-love-light);
  color: var(--color-love);
}

.menu-item.active .menu-icon.love {
  background: var(--color-love);
  color: white;
}

.menu-icon.manus {
  background: var(--color-manus-light);
  color: var(--color-manus);
}

.menu-item.active .menu-icon.manus {
  background: var(--color-manus);
  color: white;
}

.menu-icon.health {
  background: var(--color-health-light);
  color: var(--color-health);
}

.menu-item.active .menu-icon.health {
  background: var(--color-health);
  color: white;
}

.menu-icon.coffee {
  background: var(--color-coffee-light);
  color: var(--color-coffee);
}

.menu-item.active .menu-icon.coffee {
  background: var(--color-coffee);
  color: white;
}

.menu-text {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
}

.menu-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 99px;
  background: var(--gray-100);
  color: var(--text-tertiary);
  font-weight: 500;
}

.menu-item.active .menu-badge {
  background: white;
  color: var(--color-primary);
}

.menu-item.menu-item-love.active .menu-badge {
  color: var(--color-love);
}

.menu-item.menu-item-manus.active .menu-badge {
  color: var(--color-manus);
}

.menu-item.menu-item-health.active .menu-badge {
  color: var(--color-health);
}

.sidebar-footer {
  padding: var(--space-lg);
  border-top: 1px solid var(--border-color);
}

.version-info {
  font-size: 11px;
  color: var(--text-tertiary);
  text-align: center;
}

/* 主内容区 */
.app-main {
  flex: 1;
  overflow: hidden;
  background: var(--color-bg-base);
}

/* 页面过渡 */
.page-fade-enter-active,
.page-fade-leave-active {
  transition: all 0.2s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>