<template>
  <div class="message" :class="{ 'user-message': isUser, 'ai-message': !isUser, 'step-message': isStepMessage }">
    <div class="message-avatar" v-if="!isUser">
      <a-avatar :size="32" class="avatar-ai">
        <IconRobot />
      </a-avatar>
    </div>
    <div class="message-content">
      <div class="message-header">
        <span class="message-sender">{{ senderName }}</span>
        <span class="message-time">{{ formattedTime }}</span>
      </div>
      <div class="message-text">
        <pre v-if="isStepMessage && !hasDocumentLink">{{ text }}</pre>
        <div v-else-if="hasDocumentLink" class="document-message">
          <div v-html="processedText"></div>
          <div class="document-action-buttons">
            <a-button type="primary" status="success" size="small" @click="openDocumentLink">
              <template #icon>
                <icon-file-pdf v-if="documentType === 'pdf'" />
                <icon-file-html v-else-if="documentType === 'html'" />
              </template>
              {{ documentButtonText }}
            </a-button>
          </div>
        </div>
        <template v-else>{{ text }}</template>
      </div>
    </div>
    <div class="message-avatar" v-if="isUser">
      <a-avatar :size="32" class="avatar-user">
        <IconUser />
      </a-avatar>
    </div>
  </div>
</template>

<script>
import { h } from 'vue';
import { IconRobot, IconUser, IconFilePdf } from '@arco-design/web-vue/es/icon';
import { Message } from '@arco-design/web-vue';

// 创建HTML文件图标组件，使用h函数代替JSX语法
const IconFileHtml = {
  name: 'IconFileHtml',
  render() {
    return h('svg', {
      viewBox: '0 0 48 48',
      fill: 'none', 
      xmlns: 'http://www.w3.org/2000/svg',
      stroke: 'currentColor',
      class: 'arco-icon',
      'stroke-width': 4,
      'stroke-linecap': 'butt',
      'stroke-linejoin': 'miter'
    }, [
      h('path', {
        d: 'M10 4h20l8 8v32H10V4z',
        fill: 'var(--color-fill-4)'
      }),
      h('path', {
        d: 'M24 30l-4-4 4-4M32 30l4-4-4-4M30 18l-4 16',
        'stroke-linecap': 'round',
        'stroke-linejoin': 'round'
      })
    ]);
  }
};

export default {
  name: 'ChatMessage',
  components: {
    IconRobot,
    IconUser,
    IconFilePdf,
    IconFileHtml
  },
  props: {
    text: {
      type: String,
      required: true
    },
    isUser: {
      type: Boolean,
      default: false
    },
    senderName: {
      type: String,
      default: 'AI'
    },
    timestamp: {
      type: Number,
      default: () => Date.now()
    }
  },
  computed: {
    formattedTime() {
      const date = new Date(this.timestamp);
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    },
    isStepMessage() {
      return !this.isUser && this.text && this.text.trim().match(/^Step \d+: 工具\[[\w]+\]/);
    },
    documentType() {
      // 判断文档类型：pdf 或 html
      if (this.hasHtmlLink) return 'html';
      if (this.hasPdfLink) return 'pdf';
      return '';
    },
    documentButtonText() {
      // 根据文档类型返回不同的按钮文本
      if (this.documentType === 'pdf') return '查看PDF文档';
      if (this.documentType === 'html') return '查看HTML页面';
      return '查看文档';
    },
    hasPdfLink() {
      // 检查消息是否包含PDF下载链接
      return !this.isUser && this.text && 
        (this.text.includes('[点击下载PDF](') || 
         this.text.includes('- 下载链接:') &&
         this.text.includes('/api/files/pdf/') ||
         this.text.match(/Step \d+: 工具\[generatePDF\]结果:/));
    },
    hasHtmlLink() {
      // 检查消息是否包含HTML链接
      return !this.isUser && this.text && 
        (this.text.includes('[点击查看HTML页面](') || 
         this.text.includes('HTML生成成功') ||
         this.text.includes('generateHtmlWithEmbeddedPages') ||
         this.text.includes('- 下载链接:') && 
         this.text.includes('/api/files/html/') ||
         this.text.match(/Step \d+: 工具\[generateHtml\]结果:/) ||
         this.text.match(/Step \d+: 工具\[generateHtmlWithEmbeddedPages\]结果:/) ||
         this.text.match(/Step \d+: 工具\[HtmlGenerationTool\]结果:/));
    },
    hasDocumentLink() {
      // 检查消息是否包含任意文档链接（PDF或HTML）
      return this.hasPdfLink || this.hasHtmlLink;
    },
    // 转换后的文本，将各种API路径替换为localhost:8102开头的完整URL
    transformedText() {
      if (!this.text) return '';
      
      // 处理PDF链接
      let processed = this.text.replace(/\[点击下载PDF\]\((\/api\/files\/pdf\/[^)]+)\)/g, 
        (match, p1) => `[点击下载PDF](localhost:8102${p1})`);
      
      // 处理HTML链接
      processed = processed.replace(/\[点击查看HTML页面\]\((\/api\/files\/html\/[^)]+)\)/g, 
        (match, p1) => `[点击查看HTML页面](localhost:8102${p1})`);
      
      return processed;
    },
    documentLink() {
      // 提取文档下载链接（PDF或HTML）
      if (!this.hasDocumentLink) return '';
      
      // 使用转换后的文本进行匹配
      const text = this.transformedText;
      
      // 尝试匹配PDF的Markdown格式链接
      if (this.hasPdfLink) {
        let markdownLinkMatch = text.match(/\[点击下载PDF\]\((localhost:8102\/api\/files\/pdf\/[^)]+)\)/);
      if (markdownLinkMatch && markdownLinkMatch[1]) {
          return markdownLinkMatch[1]; // 返回localhost:8102/api/files/pdf/xxx.pdf格式
      }
      
        // 尝试匹配"下载链接:"后面的PDF URL格式
        let downloadLinkMatch = text.match(/下载链接:.*?(localhost:8102\/api\/files\/pdf\/[^\s\n]+)/);
      if (downloadLinkMatch && downloadLinkMatch[1]) {
        return downloadLinkMatch[1];
        }
        
        // 尝试从generatePDF工具结果中提取
        let pdfToolMatch = text.match(/工具\[generatePDF\]结果:.*?(\/api\/files\/pdf\/[^\s\n]+)/);
        if (pdfToolMatch && pdfToolMatch[1]) {
          return 'localhost:8102' + pdfToolMatch[1];
        }
      }
      
      // 尝试匹配HTML的Markdown格式链接
      if (this.hasHtmlLink) {
        // 处理常规的Markdown链接格式
        let htmlMarkdownMatch = text.match(/\[点击查看HTML页面\]\((localhost:8102\/api\/files\/html\/[^)]+)\)/);
        if (htmlMarkdownMatch && htmlMarkdownMatch[1]) {
          return htmlMarkdownMatch[1]; // 返回localhost:8102/api/files/html/xxx.html格式
        }
        
        // 尝试匹配"下载链接:"后面的HTML URL格式
        let htmlDownloadMatch = text.match(/下载链接:.*?(localhost:8102\/api\/files\/html\/[^\s\n]+)/);
        if (htmlDownloadMatch && htmlDownloadMatch[1]) {
          return htmlDownloadMatch[1];
        }
        
        // 从原始文本中尝试提取HTML路径
        let htmlPathMatch = this.text.match(/下载链接:.*?\[(.*?)\]\((\/api\/files\/html\/[^\s\n)]+)\)/);
        if (htmlPathMatch && htmlPathMatch[2]) {
          return 'localhost:8102' + htmlPathMatch[2];
        }
        
        // 尝试从工具结果中提取
        let htmlToolMatch = text.match(/工具\[(generateHtml|HtmlGenerationTool|generateHtmlWithEmbeddedPages)\]结果:.*?(\/api\/files\/html\/[^\s\n]+)/);
        if (htmlToolMatch && htmlToolMatch[2]) {
          return 'localhost:8102' + htmlToolMatch[2];
        }
        
        // 从包含generateHtmlWithEmbeddedPages的消息中提取
        let embedHtmlMatch = this.text.match(/generateHtmlWithEmbeddedPages.*?\[(.*?)\]\((\/api\/files\/html\/[^\s\n)]+)\)/);
        if (embedHtmlMatch && embedHtmlMatch[2]) {
          return 'localhost:8102' + embedHtmlMatch[2];
        }
        
        // 匹配任何包含/api/files/html/的链接
        let anyHtmlMatch = this.text.match(/(\/api\/files\/html\/[^\s\n)"']+)/);
        if (anyHtmlMatch && anyHtmlMatch[1]) {
          return 'localhost:8102' + anyHtmlMatch[1];
        }
      }
      
      return '';
    },
    processedText() {
      // 将文本转换为HTML，突出显示文档相关信息
      if (!this.hasDocumentLink) return this.text;
      
      let processed = this.transformedText;
      
      // 处理PDF相关文本
      if (this.hasPdfLink) {
        // 如果是工具结果消息，特殊处理
        if (this.text.match(/Step \d+: 工具\[generatePDF\]结果:/)) {
          return '<strong class="success-text">PDF文件已生成成功！</strong><br>点击下方按钮查看';
        }
        
        processed = processed
        .replace(/PDF生成成功！/g, '<strong class="success-text">PDF生成成功！</strong>')
        .replace(/文件名:/g, '<strong>文件名:</strong>')
        .replace(/本地路径:/g, '<strong>本地路径:</strong>')
          .replace(/下载链接:/g, '<strong>下载链接:</strong>')
          .replace(/Step \d+: 工具\[generatePDF\]结果:/g, '<strong class="success-text">PDF生成成功！</strong>');
      
      // 将Markdown链接转换为普通文本，因为我们已经有了下载按钮
      processed = processed.replace(/\[点击下载PDF\]\(.*?\)/g, '');
      }
      
      // 处理HTML相关文本
      if (this.hasHtmlLink) {
        // 如果是工具结果消息或包含generateHtmlWithEmbeddedPages，特殊处理
        if (this.text.match(/Step \d+: 工具\[(generateHtml|HtmlGenerationTool|generateHtmlWithEmbeddedPages)\]结果:/) || 
            this.text.includes('generateHtmlWithEmbeddedPages')) {
          return '<strong class="success-text">HTML生成成功！</strong><br>点击下方按钮查看';
        }
        
        processed = processed
          .replace(/HTML生成成功！/g, '<strong class="success-text">HTML生成成功！</strong>')
          .replace(/文件名:/g, '<strong>文件名:</strong>')
          .replace(/下载链接:/g, '<strong>下载链接:</strong>')
          .replace(/Step \d+: 工具\[(generateHtml|HtmlGenerationTool|generateHtmlWithEmbeddedPages)\]结果:/g, '<strong class="success-text">HTML生成成功！</strong>');
        
        // 将Markdown链接转换为普通文本，因为我们已经有了查看按钮
        processed = processed.replace(/\[点击查看HTML页面\]\(.*?\)/g, '');
      }
      
      return processed;
    }
  },
  methods: {
    openDocumentLink() {
      if (!this.documentLink) {
        Message.error(`无法找到${this.documentType.toUpperCase()}文件链接`);
        return;
      }
      
      let fullUrl = this.documentLink;
      
      // 检查链接是否已包含域名
      if (this.documentLink.startsWith('localhost:8102')) {
        // 已包含域名，添加http://前缀
        fullUrl = 'http://' + this.documentLink;
      } else if (this.documentLink.startsWith('/api')) {
        // 不包含域名，使用指定的后端地址
        fullUrl = 'http://localhost:8102' + this.documentLink;
      }
      
      // 在新标签页中打开文档
      window.open(fullUrl, '_blank');
      
      Message.success(`正在打开${this.documentType.toUpperCase()}文件...`);
    }
  }
}
</script>

<style scoped>
/* 添加新的动画效果 */
@keyframes messageAppear {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.message {
  display: flex;
  margin-bottom: 30px;
  align-items: flex-start;
  animation: messageAppear 0.4s ease-out;
  transition: all var(--transition-smooth);
}

.user-message {
  flex-direction: row-reverse;
}

.ai-message {
  flex-direction: row;
}

/* 步骤消息特殊样式 */
.ai-message.step-message {
  position: relative;
  margin-left: 20px;
  border-left: 2px dashed var(--color-primary-light);
  animation: fadeScale 0.25s ease-out forwards;
}

.ai-message.step-message::before {
  content: '';
  position: absolute;
  left: -6px;
  top: 15px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--color-primary);
  box-shadow: 0 0 8px var(--color-primary-light);
}

.message-avatar {
  margin: 0 16px;
  flex-shrink: 0;
  transform: scale(1);
  transition: transform var(--transition-quick);
}

.message:hover .message-avatar {
  transform: scale(1.05);
}

.message-content {
  max-width: 70%;
  padding: 18px 24px;
  border-radius: 16px;
  position: relative;
  word-break: break-word;
  line-height: 1.6;
  backdrop-filter: blur(10px);
  max-height: 500px;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: var(--color-mid-gray) transparent;
}

.user-message .message-content {
  background-color: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--border-color);
  text-align: right;
  border-top-right-radius: 4px;
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(8px);
}

[data-theme="dark"] .user-message .message-content {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.15) 0%, rgba(16, 185, 129, 0.1) 100%);
  border-color: rgba(255, 255, 255, 0.1);
}

.ai-message .message-content {
  background-color: rgba(255, 255, 255, 0.95);
  border: 1px solid var(--border-color);
  text-align: left;
  border-top-left-radius: 4px;
  box-shadow: var(--shadow-card);
  backdrop-filter: blur(8px);
}

[data-theme="dark"] .ai-message .message-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.08) 0%, rgba(255, 255, 255, 0.03) 100%);
  border-color: rgba(255, 255, 255, 0.1);
}

.ai-message.step-message .message-content {
  background-color: rgba(248, 248, 248, 0.95);
  border: 1px solid rgba(37, 99, 235, 0.3);
  font-family: 'SF Mono', 'Menlo', 'Monaco', 'Courier New', monospace;
  padding: 12px;
  border-radius: 8px;
  margin-top: 8px;
}

[data-theme="dark"] .ai-message.step-message .message-content {
  background: rgba(30, 30, 40, 0.9);
  border-color: rgba(37, 99, 235, 0.4);
}

.message-content:hover {
  box-shadow: var(--shadow-medium);
  transform: translateY(-1px);
}

/* 添加折痕效果 */
.message-content::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  background-image: linear-gradient(
    transparent 0%, 
    transparent 50%, 
    rgba(0, 0, 0, 0.03) 50%, 
    transparent 100%);
  background-size: 100% 8px;
}

[data-theme="dark"] .message-content::after {
  display: none;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.message-sender {
  font-weight: 500;
  color: var(--text-primary);
  font-size: 14px;
  letter-spacing: 0.3px;
}

[data-theme="dark"] .message-sender {
  color: #F9FAFB;
}

.message-text {
  color: var(--text-primary);
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.7;
}

[data-theme="dark"] .message-text {
  color: #E2E8F0;
}

.message-text pre {
  margin: 0;
  font-family: 'SF Mono', Monaco, Consolas, monospace;
  white-space: pre-wrap;
  background: rgba(255, 255, 255, 0.5);
  padding: 10px;
  border-radius: 8px;
  box-shadow: inset 0 0 5px rgba(0, 0, 0, 0.05);
}

[data-theme="dark"] .message-text pre {
  background: rgba(30, 30, 40, 0.8);
  color: #E2E8F0;
}

.message-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: 8px;
  opacity: 0.8;
}

[data-theme="dark"] .message-time {
  color: #94A3B8;
}

.user-message .message-header {
  flex-direction: row-reverse;
}

.user-message .message-time {
  margin-left: 0;
  margin-right: 8px;
}

/* 文档相关样式 */
.document-message {
  display: flex;
  flex-direction: column;
}

.document-action-buttons {
  margin-top: 12px;
}

.success-text {
  color: #16CA9D;
  font-weight: bold;
}

.document-message strong {
  font-weight: 600;
  color: var(--color-text-1);
  display: inline-block;
  min-width: 70px;
}

/* 允许v-html中的样式生效 */
.document-message :deep(a) {
  color: #3370FF;
  text-decoration: none;
}

.document-message :deep(a:hover) {
  text-decoration: underline;
}

/* 添加消息内容的滚动条样式 */
.message-content::-webkit-scrollbar {
  width: 4px;
}

.message-content::-webkit-scrollbar-track {
  background: transparent;
}

.message-content::-webkit-scrollbar-thumb {
  background-color: var(--color-mid-gray);
  border-radius: 10px;
}

/* Avatar styles */
.avatar-ai {
  background: linear-gradient(135deg, var(--color-primary) 0%, #0D47CC 100%) !important;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);
}

.avatar-user {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%) !important;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
}
</style> 