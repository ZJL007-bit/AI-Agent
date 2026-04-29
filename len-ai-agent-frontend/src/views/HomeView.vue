<template>
  <div class="home">
    <div class="home-container">
      <!-- ======== Hero ======== -->
      <section class="hero">
        <div class="hero-badge">
          <span class="badge-pulse"></span>AI 智能体平台
        </div>
        <h1 class="hero-title">
          选择你的<span class="title-grad">AI 伙伴</span>
        </h1>
        <p class="hero-sub">
          四大智能体协同运作，覆盖商业、情感、工具、健康<br/>每个场景，都有一位懂你的 AI 在等候
        </p>
        <div class="hero-nums">
          <div class="hn"><strong>4</strong> <span>智能体</span></div>
          <i></i>
          <div class="hn"><strong>7×24</strong> <span>全天在线</span></div>
          <i></i>
          <div class="hn"><strong>10K+</strong> <span>活跃用户</span></div>
        </div>
      </section>

      <!-- ======== 应用卡片 ======== -->
      <section class="sec">
        <div class="sec-head">
          <h2>智能体矩阵</h2>
          <p>点击卡片，开启你的 AI 对话之旅</p>
        </div>
        <div class="apps-grid">
          <div
            v-for="(app, i) in aiApps"
            :key="app.key"
            class="app-card"
            :style="{ animationDelay: i * 0.08 + 's' }"
            @click="goTo(app.route)"
          >
            <div class="ac-bar" :class="'bar-' + app.key"></div>
            <div class="ac-icon" :class="'icon-' + app.key">
              <component :is="app.icon" />
            </div>
            <div class="ac-main">
              <div class="ac-head">
                <h3>{{ app.title }}</h3>
                <span class="ac-badge" :class="'badge-' + app.key">{{ app.badge }}</span>
              </div>
              <p>{{ app.description }}</p>
              <div class="ac-foot">
                <div class="ac-tags">
                  <span v-for="tag in app.tags" :key="tag">{{ tag }}</span>
                </div>
                <span class="ac-go">进入 <icon-right /></span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ======== 数据 ======== -->
      <section class="sec">
        <div class="stats">
          <div class="st" v-for="s in stats" :key="s.label">
            <div class="st-icon"><component :is="s.icon" /></div>
            <div class="st-num">{{ s.value }}</div>
            <div class="st-lbl">{{ s.label }}</div>
          </div>
        </div>
      </section>

      <!-- ======== 能力 ======== -->
      <section class="sec">
        <div class="sec-head">
          <h2>平台能力</h2>
          <p>以先进 AI 技术为底座，提供卓越的智能服务体验</p>
        </div>
        <div class="hl-grid">
          <div class="hl" v-for="h in highlights" :key="h.title">
            <div class="hl-icon"><component :is="h.icon" /></div>
            <div class="hl-text">
              <h4>{{ h.title }}</h4>
              <p>{{ h.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <!-- ======== CTA ======== -->
      <section class="sec">
        <div class="cta">
          <div class="cta-orb"></div>
          <div>
            <h3>准备好开始了吗？</h3>
            <p>每个智能体都是一扇通往 AI 世界的大门</p>
          </div>
          <a-button type="primary" size="large" class="cta-btn" @click="goTo('/coffee-shop')">
            <icon-star /> 开始体验
          </a-button>
        </div>
      </section>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router';
import {
  IconRobot, IconHeart, IconBulb, IconHeartFill,
  IconRight, IconStar, IconMessage, IconUserGroup,
  IconCheckCircle, IconCommand, IconSafe, IconThunderbolt, IconBarChart
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'HomeView',
  components: {
    IconRobot, IconHeart, IconBulb, IconRight, IconHeartFill,
    IconStar, IconMessage, IconUserGroup, IconCheckCircle,
    IconCommand, IconSafe, IconThunderbolt, IconBarChart
  },
  setup() {
    const router = useRouter();
    const goTo = (r) => router.push(r);

    const aiApps = [
      { key: 'coffee', title: '咖啡店智能客服', badge: '服务', icon: IconStar, route: '/coffee-shop',
        description: '像一位懂咖啡的老友，从推荐到下单，三言两语完成点单。支持多轮对话、订单追踪与人工转接。',
        tags: ['智能点单', 'RAG 检索', '订单管理'] },
      { key: 'love', title: 'AI 恋爱大师', badge: '情感', icon: IconHeart, route: '/menu/love-app',
        description: '不愿对朋友开口的心事，AI 会耐心听完，然后给你温暖而理性的回应。',
        tags: ['情感分析', '约会建议', '关系咨询'] },
      { key: 'manus', title: 'AI 工具智能体', badge: 'AI', icon: IconBulb, route: '/menu/manus-app',
        description: '邮件、搜索、HTML 生成、数据分析 —— 把琐事交给 AI，你只管做决定。',
        tags: ['邮件发送', '网页搜索', 'HTML 生成'] },
      { key: 'health', title: '云医通健康助手', badge: '健康', icon: IconHeartFill, route: '/menu/health-app',
        description: '健康问题不用百度吓自己，AI 给你专业、冷静、有据可查的健康参考。',
        tags: ['健康咨询', '生活建议', '医疗信息'] }
    ];

    const stats = [
      { icon: IconUserGroup, value: '12,000+', label: '月活用户' },
      { icon: IconMessage, value: '86,000+', label: '累计对话' },
      { icon: IconCheckCircle, value: '99.7%', label: '响应成功率' },
      { icon: IconCommand, value: '4', label: '智能体集群' }
    ];

    const highlights = [
      { icon: IconThunderbolt, title: '毫秒级响应', desc: 'SSE 流式传输，字级实时回显，对话体验流畅无等待。' },
      { icon: IconSafe, title: '隐私优先', desc: '会话数据加密存储，敏感词自动过滤，每一次对话都安全可控。' },
      { icon: IconBarChart, title: '持续进化', desc: '模型在线热更新，RAG 知识库实时同步，能力边界每天在扩展。' },
      { icon: IconCommand, title: '多场景覆盖', desc: '电商客服、情感陪伴、工具智能、健康咨询 —— 一个平台全覆盖。' }
    ];

    return { aiApps, stats, highlights, goTo };
  }
};
</script>

<style scoped>
/* ================================================================
   ROOT
   ================================================================ */
.home {
  height: 100%;
  overflow-y: auto;
  background: linear-gradient(175deg, #f8fafd 0%, #f1f5fb 30%, #f5f7fc 60%, #fafbfe 100%);
}

.home-container {
  max-width: 1080px;
  margin: 0 auto;
  padding: 56px 24px 80px;
}

/* ================================================================
   HERO
   ================================================================ */
.hero {
  text-align: center;
  padding: 32px 0 60px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #5b6cf0;
  background: linear-gradient(135deg, rgba(99,102,241,0.06), rgba(139,92,246,0.04));
  border: 1px solid rgba(99,102,241,0.12);
  padding: 6px 16px;
  border-radius: 99px;
  margin-bottom: 24px;
}

.badge-pulse {
  width: 7px; height: 7px;
  border-radius: 50%;
  background: #6366f1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.12);
  animation: pulse-dot 2.5s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { box-shadow: 0 0 0 3px rgba(99,102,241,0.12); }
  50%      { box-shadow: 0 0 0 8px rgba(99,102,241,0.04); }
}

.hero-title {
  font-size: 50px;
  font-weight: 780;
  color: #0c1222;
  line-height: 1.14;
  margin: 0 0 20px;
  letter-spacing: -1.8px;
}

.title-grad {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 45%, #a855f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-sub {
  font-size: 16px;
  color: #6b7280;
  line-height: 1.8;
  max-width: 500px;
  margin: 0 auto 36px;
}

.hero-nums {
  display: inline-flex;
  align-items: center;
  gap: 0;
  background: #fff;
  border: 1px solid #e8ecf2;
  border-radius: 14px;
  padding: 6px 10px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.03);
}

.hn {
  display: flex;
  align-items: baseline;
  gap: 5px;
  padding: 10px 22px;
}

.hn strong {
  font-size: 23px;
  font-weight: 750;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.hn span {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 500;
}

.hero-nums i {
  width: 1px; height: 22px;
  background: #e8ecf2;
}

/* ================================================================
   SECTIONS
   ================================================================ */
.sec + .sec { margin-top: 72px; }

.sec-head {
  margin-bottom: 36px;
}

.sec-head h2 {
  font-size: 26px;
  font-weight: 720;
  color: #0f172a;
  margin: 0 0 8px;
  letter-spacing: -0.5px;
}

.sec-head p {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

/* ================================================================
   APP CARDS
   ================================================================ */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px;
}

@media (max-width: 768px) { .apps-grid { grid-template-columns: 1fr; } }

.app-card {
  position: relative;
  display: flex;
  gap: 20px;
  background: #fff;
  border: 1px solid #e8ecf2;
  border-radius: 18px;
  padding: 26px 26px 22px;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
  overflow: hidden;
  animation: cardIn 0.5s ease-out both;
}

@keyframes cardIn {
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* 左侧彩色条 */
.ac-bar {
  position: absolute;
  left: 0; top: 16px; bottom: 16px;
  width: 3px;
  border-radius: 0 3px 3px 0;
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.bar-coffee { background: linear-gradient(180deg, #d4a574, #b45309); }
.bar-love   { background: linear-gradient(180deg, #f9a8d4, #db2777); }
.bar-manus  { background: linear-gradient(180deg, #67e8f9, #0891b2); }
.bar-health { background: linear-gradient(180deg, #6ee7b7, #059669); }

.app-card:hover .ac-bar {
  top: 0; bottom: 0; width: 4px; border-radius: 0;
}

.app-card:hover {
  border-color: #d4d8df;
  transform: translateY(-3px);
  box-shadow: 0 12px 36px rgba(0,0,0,0.07);
}

/* 图标 */
.ac-icon {
  width: 48px; height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 24px;
  flex-shrink: 0;
  transition: all 0.35s;
}

.icon-coffee { background: #fef7f0; color: #b45309; }
.icon-love   { background: #fdf2f5; color: #db2777; }
.icon-manus  { background: #f0fafc; color: #0891b2; }
.icon-health { background: #f0faf5; color: #059669; }

.app-card:hover .icon-coffee { box-shadow: 0 0 0 6px rgba(180,83,9,0.06); }
.app-card:hover .icon-love   { box-shadow: 0 0 0 6px rgba(219,39,119,0.06); }
.app-card:hover .icon-manus  { box-shadow: 0 0 0 6px rgba(8,145,178,0.06); }
.app-card:hover .icon-health { box-shadow: 0 0 0 6px rgba(5,150,105,0.06); }

/* 内容 */
.ac-main { flex: 1; min-width: 0; display: flex; flex-direction: column; }

.ac-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.ac-head h3 {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
  margin: 0;
}

.ac-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 99px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  flex-shrink: 0;
}

.badge-coffee { background: #fef7f0; color: #b45309; }
.badge-love   { background: #fdf2f5; color: #db2777; }
.badge-manus  { background: #f0fafc; color: #0891b2; }
.badge-health { background: #f0faf5; color: #059669; }

.ac-main p {
  font-size: 13.5px;
  color: #6b7280;
  line-height: 1.7;
  margin: 0 0 16px;
  flex: 1;
}

.ac-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid #f1f5f9;
}

.ac-tags { display: flex; gap: 6px; }

.ac-tags span {
  font-size: 11px;
  padding: 4px 10px;
  background: #f8fafc;
  border-radius: 6px;
  color: #94a3b8;
  font-weight: 500;
  transition: all 0.25s;
}

.app-card:hover .ac-tags span {
  background: #eef2ff;
  color: #6366f1;
}

.ac-go {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
  opacity: 0;
  transform: translateX(-6px);
  transition: all 0.3s ease;
}

.app-card:hover .ac-go { opacity: 1; transform: translateX(0); }

/* ================================================================
   STATS
   ================================================================ */
.stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 2px;
  background: #fff;
  border: 1px solid #e8ecf2;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

@media (max-width: 768px) { .stats { grid-template-columns: repeat(2, 1fr); } }

.st {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 26px 16px;
  background: #fff;
  transition: background 0.2s;
}

.st:hover { background: #fafbfe; }

.st-icon {
  width: 42px; height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 20px;
  color: #6366f1;
  background: #eef2ff;
}

.st-num {
  font-size: 26px;
  font-weight: 750;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.st-lbl {
  font-size: 13px;
  color: #94a3b8;
}

/* ================================================================
   HIGHLIGHTS
   ================================================================ */
.hl-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 640px) { .hl-grid { grid-template-columns: 1fr; } }

.hl {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  background: #fff;
  border: 1px solid #e8ecf2;
  border-radius: 14px;
  padding: 24px;
  transition: all 0.3s;
  box-shadow: 0 1px 2px rgba(0,0,0,0.02);
}

.hl:hover {
  border-color: #d4d8df;
  box-shadow: 0 6px 24px rgba(0,0,0,0.05);
  transform: translateY(-1px);
}

.hl-icon {
  width: 42px; height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-size: 20px;
  flex-shrink: 0;
  color: #6366f1;
  background: #eef2ff;
  transition: all 0.3s;
}

.hl:hover .hl-icon { background: #e0e7ff; }

.hl-text h4 {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
}

.hl-text p {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.65;
  margin: 0;
}

/* ================================================================
   CTA
   ================================================================ */
.cta {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  background: linear-gradient(135deg, #f5f3ff 0%, #eef2ff 50%, #f0f9ff 100%);
  border: 1px solid #e0e7ff;
  border-radius: 18px;
  padding: 42px 46px;
  overflow: hidden;
}

.cta-orb {
  position: absolute;
  right: 120px; top: -60px;
  width: 280px; height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99,102,241,0.06) 0%, transparent 70%);
  pointer-events: none;
}

.cta h3 {
  font-size: 22px;
  font-weight: 720;
  color: #0f172a;
  margin: 0 0 6px;
  position: relative; z-index: 1;
}

.cta p {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
  position: relative; z-index: 1;
}

.cta-btn {
  background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%) !important;
  border: none !important;
  font-weight: 600 !important;
  padding: 14px 34px !important;
  height: auto !important;
  border-radius: 14px !important;
  font-size: 16px !important;
  box-shadow: 0 6px 24px rgba(99,102,241,0.28) !important;
  flex-shrink: 0;
  position: relative; z-index: 1;
  transition: all 0.3s !important;
}

.cta-btn:hover {
  transform: translateY(-2px) !important;
  box-shadow: 0 10px 34px rgba(99,102,241,0.4) !important;
}

/* ================================================================
   RESPONSIVE
   ================================================================ */
@media (max-width: 1024px) {
  .hero-title { font-size: 40px; }
  .cta { flex-direction: column; text-align: center; padding: 32px 28px; }
  .cta-orb { right: 50%; transform: translateX(50%); }
}

@media (max-width: 640px) {
  .home-container { padding: 36px 16px 56px; }
  .hero { padding: 12px 0 36px; }
  .hero-title { font-size: 31px; }
  .hero-sub { font-size: 14px; }
  .hero-nums { flex-wrap: wrap; justify-content: center; }
  .hero-nums i { display: none; }
  .sec + .sec { margin-top: 48px; }
  .app-card { flex-direction: column; gap: 12px; padding: 20px 18px 18px; }
  .sec-head h2 { font-size: 22px; }
}
</style>
