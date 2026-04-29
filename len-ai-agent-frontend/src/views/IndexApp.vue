<template>
  <div class="index-app">
    <!-- Navigation -->
    <nav class="navbar">
      <div class="navbar-content">
        <div class="navbar-brand">
          <div class="brand-icon">
            <icon-robot />
          </div>
          <span class="brand-name">AI Agent</span>
        </div>
        <div class="navbar-links">
          <a href="#features" class="nav-link">功能</a>
          <a href="#pricing" class="nav-link">定价</a>
          <a href="#testimonials" class="nav-link">客户案例</a>
          <a href="#contact" class="nav-link">联系我们</a>
          <a href="/menu/home" class="btn-cta cursor-pointer">立即体验</a>
        </div>
        <button class="navbar-toggle" @click="mobileMenuOpen = !mobileMenuOpen">
          <icon-menu v-if="!mobileMenuOpen" />
          <icon-close v-else />
        </button>
      </div>
    </nav>

    <!-- Mobile Menu -->
    <div v-show="mobileMenuOpen" class="mobile-menu" @click="mobileMenuOpen = false">
      <div class="mobile-menu-content" @click.stop>
        <a href="#features" class="mobile-link">功能</a>
        <a href="#pricing" class="mobile-link">定价</a>
        <a href="#testimonials" class="mobile-link">客户案例</a>
        <a href="#contact" class="mobile-link">联系我们</a>
        <button class="btn-cta cursor-pointer" @click="goToMenu">立即体验</button>
      </div>
    </div>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-background">
        <div class="hero-gradient"></div>
        <div class="hero-grid"></div>
      </div>
      <div class="hero-content">
        <div class="hero-text">
          <div class="hero-badge">
            <span class="status-dot status-online"></span>
            10,000+ 用户的AI伙伴
          </div>
          <h1 class="hero-title">
            智能矩阵应用平台<br>
            <span class="gradient-text">重新定义AI交互</span>
          </h1>
          <p class="hero-description">
            覆盖情感、健康、电商等多场景AI智能体，从深夜心事到清晨咖啡，让AI成为你生活中无所不在的贴心伙伴。
          </p>
          <div class="hero-actions">
            <a href="/menu/home" class="btn-primary glow-effect cursor-pointer">
              开始探索
            </a>
            <a href="#" class="btn-secondary cursor-pointer" @click.prevent="showDemo">
              观看演示
            </a>
          </div>
          <div class="hero-trust">
            <div class="trust-item">
              <icon-check-circle />
              <span>完全免费</span>
            </div>
            <div class="trust-item">
              <icon-check-circle />
              <span>无需注册</span>
            </div>
            <div class="trust-item">
              <icon-check-circle />
              <span>隐私保护</span>
            </div>
          </div>
        </div>
        <div class="hero-visual">
          <div class="dashboard-mockup">
            <div class="mockup-header">
              <div class="mockup-dot bg-red-500"></div>
              <div class="mockup-dot bg-yellow-500"></div>
              <div class="mockup-dot bg-green-500"></div>
            </div>
            <div class="mockup-body">
              <div class="mockup-stats">
                <div class="stat-card">
                  <p class="stat-label">咖啡店客服</p>
                  <p class="stat-value">2,918</p>
                  <p class="stat-change positive">智能点单</p>
                </div>
                <div class="stat-card">
                  <p class="stat-label">AI 恋爱大师</p>
                  <p class="stat-value">3,247</p>
                  <p class="stat-change positive">对话次数</p>
                </div>
                <div class="stat-card">
                  <p class="stat-label">超级智能体</p>
                  <p class="stat-value">5,891</p>
                  <p class="stat-change positive">解答问题</p>
                </div>
              </div>
              <div class="mockup-chart">
                <p class="chart-placeholder">智能对话交互区域</p>
              </div>
            </div>
          </div>
          <div class="hero-glow"></div>
        </div>
      </div>
    </section>

    <!-- Logo Cloud -->
    <section class="logo-section">
      <p class="logo-title">深受用户信赖</p>
      <div class="logo-grid">
        <div class="logo-item" style="color: var(--color-coffee);">咖啡店客服</div>
        <div class="logo-item" style="color: var(--color-love);">恋爱大师</div>
        <div class="logo-item" style="color: var(--color-manus);">工具智体</div>
        <div class="logo-item" style="color: var(--color-health);">健康顾问</div>
        <div class="logo-item" style="color: var(--color-primary);">万能助手</div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="features-section">
      <div class="section-header">
        <span class="section-badge">智能体矩阵</span>
        <h2 class="section-title">多场景AI智能体</h2>
        <p class="section-description">覆盖情感、知识、商业、健康等多元场景，每个智能体都带着各自的温度与专业，只为更懂你的每一次对话</p>
      </div>
      <div class="features-grid">
        <div class="feature-card hover-lift">
          <div class="feature-icon" style="background: var(--color-coffee-gradient);">
            <icon-star />
          </div>
          <h3 class="feature-title">电商智能客服</h3>
          <p class="feature-description">全天候在线营业的AI店长，听懂每位顾客的喜好，从点单到下单一气呵成，让每一杯咖啡都恰到好处。</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon" style="background: var(--color-love-gradient);">
            <icon-heart />
          </div>
          <h3 class="feature-title">AI 恋爱大师</h3>
          <p class="feature-description">你的专属恋爱顾问，提供情感、约会和关系方面的建议与支持。</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon" style="background: var(--color-manus-gradient);">
            <icon-bulb />
          </div>
          <h3 class="feature-title">工具智能体</h3>
          <p class="feature-description">强大的AI助手，发送邮件、协助创作、网页搜索、html生成和智能建议。</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon" style="background: var(--color-health-gradient);">
            <icon-heart-fill />
          </div>
          <h3 class="feature-title">健康顾问</h3>
          <p class="feature-description">专业健康知识、生活方式建议和基础医疗信息咨询服务。</p>
        </div>
        <div class="feature-card hover-lift">
          <div class="feature-icon">
            <icon-robot />
          </div>
          <h3 class="feature-title">AI 万能助手</h3>
          <p class="feature-description">全能型AI助手，处理各类问题，提供个性化解决方案。</p>
        </div>
      </div>
    </section>

    <!-- Stats Section -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-number">10,000+</div>
          <div class="stat-label">活跃用户</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">50,000+</div>
          <div class="stat-label">智能对话</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">99.9%</div>
          <div class="stat-label">响应成功率</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">4.9/5</div>
          <div class="stat-label">用户满意度</div>
        </div>
      </div>
    </section>

    <!-- Testimonials -->
    <section id="testimonials" class="testimonials-section">
      <div class="section-header">
        <span class="section-badge">用户心声</span>
        <h2 class="section-title">用户的真实反馈</h2>
      </div>
      <div class="testimonials-grid">
        <div class="testimonial-card">
          <div class="testimonial-rating">
            <icon-star v-for="i in 5" :key="i" />
          </div>
          <p class="testimonial-content">"咖啡店AI客服太方便了，它会记住我常点的口味，聊着天就把单下了，社恐人狂喜！"</p>
          <div class="testimonial-author">
            <div class="author-avatar" style="background: var(--color-coffee-light); color: var(--color-coffee);">客
            </div>
            <div class="author-info">
              <p class="author-name">咖啡星人</p>
              <p class="author-title">忠实顾客</p>
            </div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-rating">
            <icon-star v-for="i in 5" :key="i" />
          </div>
          <p class="testimonial-content">"AI恋爱大师帮我理清了很多感情上的困惑，回答很贴心温暖。"</p>
          <div class="testimonial-author">
            <div class="author-avatar" style="background: var(--color-love-light); color: var(--color-love);">恋</div>
            <div class="author-info">
              <p class="author-name">小恋</p>
              <p class="author-title">大学生</p>
            </div>
          </div>
        </div>
        <div class="testimonial-card">
          <div class="testimonial-rating">
            <icon-star v-for="i in 5" :key="i" />
          </div>
          <p class="testimonial-content">"超级智能体回答问题超级专业，帮我写代码效率提高太多了！"</p>
          <div class="testimonial-author">
            <div class="author-avatar" style="background: var(--color-manus-light); color: var(--color-manus);">超</div>
            <div class="author-info">
              <p class="author-name">超哥</p>
              <p class="author-title">程序员</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Showcase -->
    <section id="pricing" class="pricing-section">
      <div class="section-header">
        <span class="section-badge">特色功能</span>
        <h2 class="section-title">为什么选择我们</h2>
      </div>
      <div class="pricing-grid">
        <div class="pricing-card">
          <div class="feature-icon" style="background: var(--color-love-gradient);">
            <icon-heart />
          </div>
          <h3 class="pricing-title">情感智能</h3>
          <p class="pricing-subtitle">深度理解你的情绪</p>
          <ul class="pricing-features">
            <li>
              <icon-check />
              共情式对话
            </li>
            <li>
              <icon-check />
              个性化建议
            </li>
            <li>
              <icon-check />
              24/7在线
            </li>
          </ul>
          <a href="/menu/home" class="btn-pricing cursor-pointer">立即体验</a>
        </div>
        <div class="pricing-card popular">
          <div class="popular-badge">免费使用</div>
          <div class="feature-icon" style="background: var(--color-manus-gradient);">
            <icon-bulb />
          </div>
          <h3 class="pricing-title">电商客服助手</h3>
          <p class="pricing-subtitle">一站式智能解决方案</p>
          <ul class="pricing-features">
            <li>
              <icon-check />
              智能点单
            </li>
            <li>
              <icon-check />
              订单追踪
            </li>
            <li>
              <icon-check />
              敏感词过滤
            </li>
            <li>
              <icon-check />
              多轮对话
            </li>
            <li>
              <icon-check />
              RAG检索
            </li>
          </ul>
          <a href="/menu/home" class="btn-pricing primary cursor-pointer">开始探索</a>
        </div>
        <div class="pricing-card">
          <div class="feature-icon" style="background: var(--color-health-gradient);">
            <icon-heart-fill />
          </div>
          <h3 class="pricing-title">工具智能体</h3>
          <p class="pricing-subtitle">你的智能工具箱</p>
          <ul class="pricing-features">
            <li>
              <icon-check />
              邮件发送
            </li>
            <li>
              <icon-check />
              生成html网页
            </li>
            <li>
              <icon-check />
              网页搜索
            </li>
            <li>
              <icon-check />
              PDF 文档生成
            </li>
          </ul>
          <a href="/menu/home" class="btn-pricing cursor-pointer">立即体验</a>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section id="contact" class="cta-section">
      <h2 class="cta-title">开启你的AI之旅</h2>
      <p class="cta-description">选择一个智能体，开始你的智能交互体验</p>
      <a href="/menu/home" class="btn-cta-submit cursor-pointer">
        立即体验
      </a>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-brand">
          <div class="brand-icon">
            <icon-robot />
          </div>
          <span class="brand-name">AI Agent</span>
          <p class="footer-description">智能矩阵应用平台 - 让AI成为你的贴心伙伴</p>
        </div>
        <div class="footer-links">
          <div class="footer-column">
            <h4>智能体</h4>
            <a href="#">咖啡店客服</a>
            <a href="#">恋爱大师</a>
            <a href="#">工具智能体</a>
            <a href="#">健康顾问</a>
          </div>
          <div class="footer-column">
            <h4>关于</h4>
            <a href="#">关于我们</a>
            <a href="#">更新日志</a>
            <a href="#">反馈建议</a>
          </div>
          <div class="footer-column">
            <h4>支持</h4>
            <a href="#">使用帮助</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        © 2026 AI Agent. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  IconRobot,
  IconMenu,
  IconClose,
  IconCheckCircle,
  IconCheck,
  IconStar,
  IconBulb,
  IconHeart,
  IconHeartFill
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'IndexApp',
  components: {
    IconRobot,
    IconMenu,
    IconClose,
    IconCheckCircle,
    IconCheck,
    IconStar,
    IconBulb,
    IconHeart,
    IconHeartFill
  },
  setup() {
    const router = useRouter();
    const mobileMenuOpen = ref(false);
    const email = ref('');

    const showDemo = () => {
      alert('演示功能');
    };

    const handleSubmit = () => {
      if (email.value) {
        alert('提交成功: ' + email.value);
        email.value = '';
      }
    };

    const goToMenu = () => {
      router.push('/menu/home');
    };

    return {
      mobileMenuOpen,
      email,
      showDemo,
      handleSubmit,
      goToMenu
    };
  }
};
</script>

<style scoped>
.index-app {
  height: 100%;
  overflow-y: auto;
  background: var(--color-bg-base);
}

/* ===== Navbar ===== */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(20px);
}

.navbar-content {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #0EA5E9 100%);
  border-radius: 10px;
  color: white;
  font-size: 20px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  color: white;
}

.navbar-links {
  display: flex;
  align-items: center;
  gap: 32px;
}

.nav-link {
  color: var(--gray-300);
  font-size: 15px;
  font-weight: 500;
  transition: color 0.2s;
}

.nav-link:hover {
  color: white;
}

.btn-cta {
  display: inline-flex;
  align-items: center;
  padding: 10px 20px;
  background: #F97316;
  color: white;
  font-weight: 600;
  border-radius: 8px;
  transition: all 0.2s;
  animation: glow 2s infinite;
}

.btn-cta:hover {
  background: #EA580C;
}

@keyframes glow {

  0%,
  100% {
    box-shadow: 0 0 0 0 rgba(234, 88, 12, 0.4);
  }

  50% {
    box-shadow: 0 0 20px 4px rgba(234, 88, 12, 0.2);
  }
}

.navbar-toggle {
  display: none;
  color: white;
  font-size: 24px;
}

/* Mobile Menu */
.mobile-menu {
  position: fixed;
  inset: 0;
  z-index: 90;
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(20px);
}

.mobile-menu-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.mobile-link {
  font-size: 20px;
  color: white;
}

@media (max-width: 768px) {
  .navbar-links {
    display: none;
  }

  .navbar-toggle {
    display: block;
  }
}

/* ===== Hero Section ===== */
.hero-section {
  position: relative;
  min-height: 100vh;
  padding: 100px 24px 80px;
  display: flex;
  align-items: center;
  overflow: hidden;
}

.hero-background {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, #0F172A 0%, #1E293B 100%);
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 20%, rgba(37, 99, 235, 0.2) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(14, 165, 233, 0.15) 0%, transparent 50%);
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(37, 99, 235, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(37, 99, 235, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
}

.hero-content {
  position: relative;
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 48px;
  align-items: center;
}

.hero-text {
  color: white;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 99px;
  font-size: 14px;
  color: #60A5FA;
  margin-bottom: 24px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-online {
  background: #22C55E;
  animation: pulse 2s infinite;
}

.hero-title {
  font-size: 56px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 24px;
}

.gradient-text {
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #0EA5E9 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 18px;
  color: var(--gray-400);
  margin-bottom: 32px;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  padding: 16px 32px;
  background: #F97316;
  color: white;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.2s;
}

.btn-primary:hover {
  background: #EA580C;
  transform: translateY(-2px);
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  padding: 16px 32px;
  border: 1px solid var(--gray-600);
  color: white;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: var(--gray-800);
}

.hero-trust {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: var(--gray-500);
}

.trust-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.trust-item :deep(svg) {
  color: #22C55E;
  width: 20px;
  height: 20px;
}

/* Hero Visual */
.hero-visual {
  position: relative;
  display: flex;
  justify-content: center;
}

.dashboard-mockup {
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-20px);
  }
}

.mockup-header {
  display: flex;
  gap: 6px;
  padding: 12px 16px;
  background: var(--gray-800);
  border-radius: 8px 8px 0 0;
}

.mockup-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.bg-red-500 {
  background: #EF4444;
}

.bg-yellow-500 {
  background: #EAB308;
}

.bg-green-500 {
  background: #22C55E;
}

.mockup-body {
  background: var(--gray-900);
  border-radius: 8px;
  padding: 24px;
}

.mockup-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 16px;
}

.stat-label {
  font-size: 14px;
  color: var(--gray-400);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: white;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 14px;
}

.stat-change.positive {
  color: #22C55E;
}

.mockup-chart {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-placeholder {
  color: var(--gray-500);
  font-size: 14px;
}

.hero-glow {
  position: absolute;
  bottom: -32px;
  left: -32px;
  width: 96px;
  height: 96px;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #0EA5E9 100%);
  border-radius: 50%;
  opacity: 0.2;
  filter: blur(60px);
}

@media (max-width: 1024px) {
  .hero-content {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    display: none;
  }

  .hero-title {
    font-size: 40px;
  }
}

/* ===== Logo Section ===== */
.logo-section {
  padding: 48px 24px;
  background: white;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
}

.logo-title {
  text-align: center;
  color: var(--gray-500);
  font-size: 14px;
  margin-bottom: 32px;
}

.logo-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 48px;
  opacity: 0.6;
}

.logo-item {
  font-size: 24px;
  font-weight: 700;
  color: var(--gray-400);
}

/* ===== Sections Common ===== */
.features-section,
.testimonials-section,
.pricing-section {
  padding: 96px 24px;
  background: var(--gray-50);
}

.section-header {
  text-align: center;
  margin-bottom: 64px;
}

.section-badge {
  display: inline-block;
  padding: 6px 16px;
  background: rgba(37, 99, 235, 0.1);
  color: var(--color-primary);
  font-size: 14px;
  font-weight: 500;
  border-radius: 99px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.section-description {
  font-size: 18px;
  color: var(--text-secondary);
  max-width: 600px;
  margin: 0 auto;
}

/* ===== Features ===== */
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1280px;
  margin: 0 auto;
}

.feature-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 32px;
  cursor: pointer;
  transition: all 0.25s;
}

.feature-card:hover {
  border-color: transparent;
  box-shadow: 0 20px 40px rgba(37, 99, 235, 0.15);
  transform: translateY(-4px);
}

.feature-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2563EB 0%, #3B82F6 50%, #0EA5E9 100%);
  border-radius: 12px;
  color: white;
  font-size: 28px;
  margin-bottom: 24px;
}

.feature-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.feature-description {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .features-grid {
    grid-template-columns: 1fr;
  }
}

/* ===== Stats ===== */
.stats-section {
  padding: 80px 24px;
  background: var(--gray-900);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 32px;
  max-width: 1280px;
  margin: 0 auto;
  text-align: center;
}

.stat-item {
  color: white;
}

.stat-number {
  font-size: 56px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  color: var(--gray-400);
  font-size: 16px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .stat-number {
    font-size: 40px;
  }
}

/* ===== Testimonials ===== */
.testimonials-section {
  background: white;
}

.testimonials-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1280px;
  margin: 0 auto;
}

.testimonial-card {
  background: white;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 32px;
}

.testimonial-rating {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
}

.testimonial-rating :deep(svg) {
  width: 20px;
  height: 20px;
  color: #FBBF24;
  fill: #FBBF24;
}

.testimonial-content {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 24px;
}

.testimonial-author {
  display: flex;
  align-items: center;
  gap: 16px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gray-200);
  border-radius: 50%;
  font-weight: 600;
  color: var(--gray-600);
}

.author-name {
  font-weight: 600;
  color: var(--text-primary);
}

.author-title {
  font-size: 14px;
  color: var(--gray-500);
}

@media (max-width: 1024px) {
  .testimonials-grid {
    grid-template-columns: 1fr;
  }
}

/* ===== Pricing ===== */
.pricing-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1024px;
  margin: 0 auto;
}

.pricing-card {
  background: white;
  border: 2px solid var(--border-color);
  border-radius: 16px;
  padding: 32px;
  position: relative;
}

.pricing-card.popular {
  border-color: var(--color-primary);
}

.popular-badge {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 16px;
  background: var(--color-primary);
  color: white;
  font-size: 14px;
  font-weight: 500;
  border-radius: 99px;
}

.pricing-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.pricing-subtitle {
  color: var(--gray-500);
  margin-bottom: 24px;
}

.pricing-price {
  margin-bottom: 24px;
}

.price-amount {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-primary);
}

.price-period {
  color: var(--gray-500);
}

.pricing-features {
  list-style: none;
  padding: 0;
  margin: 0 0 32px;
}

.pricing-features li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
  color: var(--text-secondary);
}

.pricing-features :deep(svg) {
  width: 20px;
  height: 20px;
  color: #22C55E;
}

.btn-pricing {
  display: block;
  width: 100%;
  padding: 12px;
  border: 2px solid var(--gray-900);
  border-radius: 12px;
  text-align: center;
  font-weight: 600;
  color: var(--text-primary);
  transition: all 0.2s;
}

.btn-pricing:hover {
  background: var(--gray-900);
  color: white;
}

.btn-pricing.primary {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: white;
}

.btn-pricing.primary:hover {
  background: var(--color-primary-hover);
}

@media (max-width: 1024px) {
  .pricing-grid {
    grid-template-columns: 1fr;
    max-width: 400px;
  }
}

/* ===== CTA ===== */
.cta-section {
  padding: 96px 24px;
  background: linear-gradient(180deg, #0F172A 0%, #1E293B 100%);
  text-align: center;
}

.cta-title {
  font-size: 40px;
  font-weight: 700;
  color: white;
  margin-bottom: 24px;
}

.cta-description {
  font-size: 18px;
  color: var(--gray-400);
  margin-bottom: 32px;
}

.cta-form {
  display: flex;
  gap: 16px;
  max-width: 480px;
  margin: 0 auto;
}

.cta-input {
  flex: 1;
  padding: 16px 24px;
  background: var(--gray-800);
  border: 1px solid var(--gray-700);
  border-radius: 12px;
  color: white;
  font-size: 16px;
}

.cta-input::placeholder {
  color: var(--gray-500);
}

.cta-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.btn-cta-submit {
  padding: 16px 32px;
  background: #F97316;
  color: white;
  font-weight: 600;
  border-radius: 12px;
  transition: all 0.2s;
  animation: glow 2s infinite;
}

.btn-cta-submit:hover {
  background: #EA580C;
}

@media (max-width: 640px) {
  .cta-form {
    flex-direction: column;
  }
}

/* ===== Footer ===== */
.footer {
  background: var(--gray-900);
  padding: 64px 24px 32px;
}

.footer-content {
  display: grid;
  grid-template-columns: 2fr 3fr;
  gap: 64px;
  max-width: 1280px;
  margin: 0 auto 48px;
}

.footer-description {
  color: var(--gray-400);
  font-size: 14px;
  margin-top: 16px;
}

.footer-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
}

.footer-column h4 {
  color: white;
  font-weight: 600;
  margin-bottom: 16px;
}

.footer-column a {
  display: block;
  color: var(--gray-400);
  font-size: 14px;
  padding: 8px 0;
  transition: color 0.2s;
}

.footer-column a:hover {
  color: white;
}

.footer-bottom {
  padding-top: 32px;
  border-top: 1px solid var(--gray-800);
  text-align: center;
  color: var(--gray-500);
  font-size: 14px;
}

@media (max-width: 768px) {
  .footer-content {
    grid-template-columns: 1fr;
  }

  .footer-links {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>