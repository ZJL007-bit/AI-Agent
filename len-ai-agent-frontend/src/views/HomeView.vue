<template>
  <div class="home-view">
    <div class="home-container">
      <!-- 欢迎区 -->
      <div class="welcome-section">
        <div class="welcome-card">
          <div class="welcome-content">
            <div class="welcome-icon">
              <icon-robot />
            </div>
            <div class="welcome-text">
              <h1>欢迎使用智能矩阵应用平台</h1>
              <p>选择下方智能体开始对话之旅</p>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 应用卡片区 -->
      <div class="apps-section">
        <h2 class="section-title">智能体应用</h2>
        
        <div class="apps-grid">
          <div 
            v-for="app in aiApps" 
            :key="app.key"
            class="app-card"
            :class="`app-${app.key}`"
            @click="goTo(app.route)"
          >
            <div class="app-card-header">
              <div class="app-icon" :class="`icon-${app.key}`">
                <component :is="app.icon" />
              </div>
              <div class="app-badge" :class="`badge-${app.key}`">
                {{ app.badge }}
              </div>
            </div>
            
            <div class="app-card-body">
              <h3 class="app-title">{{ app.title }}</h3>
              <p class="app-desc">{{ app.description }}</p>
            </div>
            
            <div class="app-card-footer">
              <span class="start-btn">
                开始对话
                <icon-right />
              </span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 特性展示 -->
      <div class="features-section">
        <h2 class="section-title">平台特性</h2>
        
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <icon-bulb />
            </div>
            <h4>智能对话</h4>
            <p>基于先进AI技术，提供智能交互体验</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <icon-safe />
            </div>
            <h4>安全可靠</h4>
            <p>数据加密存储，保护用户隐私</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <icon-thunderbolt />
            </div>
            <h4>快速响应</h4>
            <p>高效算法优化，即时获取答案</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <icon-settings />
            </div>
            <h4>持续迭代</h4>
            <p>不断优化升级，提供更好服务</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import { 
  IconRobot, 
  IconHeart, 
  IconBulb,
  IconBook,
  IconHeartFill,
  IconRight,
  IconSafe,
  IconThunderbolt,
  IconSettings
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'HomeView',
  components: {
    IconRobot,
    IconHeart,
    IconBulb,
    IconRight,
    IconBook,
    IconHeartFill,
    IconSafe,
    IconThunderbolt,
    IconSettings
  },
  setup() {
    const router = useRouter();
    
    const aiApps = [
      {
        key: 'love',
        title: 'AI 恋爱大师',
        description: '你的专属恋爱顾问，提供情感、约会和关系方面的建议与支持。',
        badge: '情感',
        icon: IconHeart,
        route: '/menu/love-app'
      },
      {
        key: 'manus',
        title: 'AI工具智能体',
        description: '强大的智能助手，可以回答问题，提供建议、协助创作和分析信息。',
        badge: 'AI',
        icon: IconBulb,
        route: '/menu/manus-app'
      },
      {
        key: 'exam',
        title: '智慧答题助手',
        description: '可以回答问题、解析知识点，帮助你高效准确地完成各类考试和测验。',
        badge: '学习',
        icon: IconBook,
        route: '/menu/exam-app'
      },
      {
        key: 'health',
        title: '云医通健康助手',
        description: '您的健康顾问，提供健康知识、生活方式建议和基础医疗信息咨询。',
        badge: '健康',
        icon: IconHeartFill,
        route: '/menu/health-app'
      }
    ];
    
    const goTo = (route) => {
      router.push(route);
    };
    
    return {
      aiApps,
      goTo
    };
  }
};
</script>

<style scoped>
.home-view {
  height: 100%;
  overflow-y: auto;
  padding: var(--space-xl);
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* 欢迎区 */
.welcome-section {
  margin-bottom: var(--space-2xl);
}

.welcome-card {
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-card);
  position: relative;
  overflow: hidden;
}

.welcome-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--color-love), var(--color-manus), var(--color-exam), var(--color-health));
  background-size: 300% 100%;
  animation: gradientShift 3s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
}

.welcome-icon {
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary-light) 0%, rgba(37, 99, 235, 0.2) 100%);
  border-radius: var(--radius-lg);
  color: var(--color-primary);
  font-size: 32px;
  flex-shrink: 0;
  position: relative;
  animation: float 3s ease-in-out infinite;
}

.welcome-icon::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--color-primary), var(--color-accent));
  opacity: 0.3;
  filter: blur(10px);
  z-index: -1;
}

.welcome-text h1 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: var(--space-sm);
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--color-primary) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-text p {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 区块标题 */
.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: var(--space-lg);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.section-title::before {
  content: '';
  width: 4px;
  height: 16px;
  background: var(--color-primary);
  border-radius: 2px;
}

/* 应用卡片 */
.apps-section {
  margin-bottom: var(--space-2xl);
}

.apps-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-lg);
}

@media (max-width: 768px) {
  .apps-grid {
    grid-template-columns: 1fr;
  }
}

.app-card {
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  position: relative;
  overflow: hidden;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, transparent 0%, rgba(255,255,255,0.1) 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.app-card:hover {
  border-color: transparent;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
}

.app-card:hover::before {
  opacity: 1;
}

.app-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-md);
}

.app-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  font-size: 24px;
  transition: all var(--transition-base);
}

.icon-love {
  background: linear-gradient(135deg, var(--color-love-light) 0%, rgba(225, 29, 72, 0.1) 100%);
  color: var(--color-love);
}

.app-card:hover .icon-love {
  background: var(--color-love);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(225, 29, 72, 0.4);
}

.icon-manus {
  background: linear-gradient(135deg, var(--color-manus-light) 0%, rgba(8, 145, 178, 0.1) 100%);
  color: var(--color-manus);
}

.app-card:hover .icon-manus {
  background: var(--color-manus);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(8, 145, 178, 0.4);
}

.icon-exam {
  background: linear-gradient(135deg, var(--color-exam-light) 0%, rgba(124, 58, 237, 0.1) 100%);
  color: var(--color-exam);
}

.app-card:hover .icon-exam {
  background: var(--color-exam);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(124, 58, 237, 0.4);
}

.icon-health {
  background: linear-gradient(135deg, var(--color-health-light) 0%, rgba(5, 150, 105, 0.1) 100%);
  color: var(--color-health);
}

.app-card:hover .icon-health {
  background: var(--color-health);
  color: white;
  transform: scale(1.1);
  box-shadow: 0 4px 15px rgba(5, 150, 105, 0.4);
}

.app-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 99px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  transition: all var(--transition-base);
}

.badge-love {
  background: linear-gradient(135deg, var(--color-love-light) 0%, rgba(225, 29, 72, 0.1) 100%);
  color: var(--color-love);
}

.app-card:hover .badge-love {
  background: var(--color-love);
  color: white;
}

.badge-manus {
  background: linear-gradient(135deg, var(--color-manus-light) 0%, rgba(8, 145, 178, 0.1) 100%);
  color: var(--color-manus);
}

.app-card:hover .badge-manus {
  background: var(--color-manus);
  color: white;
}

.badge-exam {
  background: linear-gradient(135deg, var(--color-exam-light) 0%, rgba(124, 58, 237, 0.1) 100%);
  color: var(--color-exam);
}

.app-card:hover .badge-exam {
  background: var(--color-exam);
  color: white;
}

.badge-health {
  background: linear-gradient(135deg, var(--color-health-light) 0%, rgba(5, 150, 105, 0.1) 100%);
  color: var(--color-health);
}

.app-card:hover .badge-health {
  background: var(--color-health);
  color: white;
}

.app-card-body {
  margin-bottom: var(--space-md);
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: var(--space-sm);
  transition: color var(--transition-base);
}

.app-card:hover .app-title {
  color: var(--color-primary);
}

.app-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

.app-card-footer {
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-color);
  position: relative;
}

.app-card:hover .app-card-footer {
  border-top-color: transparent;
}

.start-btn {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-primary);
  transition: all var(--transition-base);
}

.app-card:hover .start-btn {
  gap: var(--space-md);
}

/* 特性区 */
.features-section {
  margin-bottom: var(--space-xl);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}

@media (max-width: 992px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}

.feature-card {
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: var(--space-lg);
  text-align: center;
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--space-md);
  background: var(--gray-100);
  border-radius: var(--radius-md);
  color: var(--color-primary);
  font-size: 24px;
}

[data-theme="dark"] .feature-icon {
  background: var(--gray-800);
}

.feature-card h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: var(--space-sm);
}

.feature-card p {
  font-size: 12px;
  color: var(--text-secondary);
  line-height: 1.5;
}
</style>