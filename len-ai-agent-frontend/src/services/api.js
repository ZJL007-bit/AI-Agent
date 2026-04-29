import axios from 'axios';
import { EventSourcePolyfill } from 'event-source-polyfill';
// 如果上面的导入方式不起作用，可以改用原生 EventSource
const EventSource = window.EventSource || EventSourcePolyfill;

const API_BASE_URL = '/api';

// 创建 axios 实例
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// AI 恋爱大师应用的 SSE 连接
export const connectToLoveAppSse = (message, chatId, onMessage, onError) => {
  const url = `${API_BASE_URL}/ai/love_app/chat/sse?message=${encodeURIComponent(message)}&chatId=${encodeURIComponent(chatId)}`;
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    if (onMessage && event.data) {
      onMessage(event.data);
    }
  };
  
  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };
  
  return eventSource;
};

// AI 超级智能体应用的 SSE 连接
export const connectToManusChat = (message, onMessage, onError) => {
  const url = `${API_BASE_URL}/ai/manus/chat?message=${encodeURIComponent(message)}`;
  const eventSource = new EventSource(url);
  
  eventSource.onmessage = (event) => {
    if (onMessage && event.data) {
      onMessage(event.data);
    }
  };
  
  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };
  
  return eventSource;
};

// 云医通健康助手应用的 SSE 连接
export const connectToHealthChat = (message, onMessage, onError) => {
  const url = `${API_BASE_URL}/ai/health/chat?message=${encodeURIComponent(message)}`;
  const eventSource = new EventSource(url);

  eventSource.onmessage = (event) => {
    if (onMessage && event.data) {
      onMessage(event.data);
    }
  };

  eventSource.onerror = (error) => {
    if (onError) {
      onError(error);
    }
    eventSource.close();
  };

  return eventSource;
};

// ==================== 咖啡店智能客服 API ====================

// POST + SSE 聊天接口
export const agentChat = (input, sessionId, onEvent, onError, onDone) => {
  const url = `${API_BASE_URL}/agent/chat`;

  fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ message: input, sessionId, userId: '' })
  }).then(response => {
    if (!response.ok) {
      onError && onError(new Error('HTTP ' + response.status));
      return;
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';
    let currentEventType = '';

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          onDone && onDone();
          return;
        }
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        for (const line of lines) {
          if (line.startsWith('event:')) {
            currentEventType = line.slice(6).trim();
          } else if (line.startsWith('data:')) {
            try {
              const data = JSON.parse(line.slice(5).trim());
              onEvent && onEvent({ event: currentEventType, data: data });
            } catch (e) { /* ignore parse errors */ }
          } else if (line.trim() === '') {
            currentEventType = '';
          }
        }
        read();
      }).catch(err => {
        onError && onError(err);
      });
    }
    read();
  }).catch(err => {
    onError && onError(err);
  });
};

// 取消订单
export const agentCancelOrder = (orderNo) => {
  return apiClient.post('/agent/chat/cancel', { orderNo });
};

// 提交反馈
export const agentFeedback = (sessionId, score, comment) => {
  return apiClient.post('/agent/feedback', { sessionId, score, comment });
};

// 会话管理
export const agentClearSession = (sessionId) => apiClient.delete('/agent/chat/session', { data: { sessionId } });
export const agentGetSessions = (sessionIds) => apiClient.get('/agent/chat/sessions', { params: { sessionIds: sessionIds.join(',') } });

// 管理后台接口
export const adminGetProducts = () => apiClient.get('/agent/admin/products');
export const adminCreateProduct = (product) => apiClient.post('/agent/admin/products', product);
export const adminUpdateProduct = (id, product) => apiClient.put(`/agent/admin/products/${id}`, product);
export const adminDeleteProduct = (id) => apiClient.delete(`/agent/admin/products/${id}`);
export const adminGetOrders = () => apiClient.get('/agent/admin/orders');
export const adminGetSessions = (page = 1, size = 10, status = '') =>
  apiClient.get('/agent/admin/sessions', { params: { page, size, ...(status ? { status } : {}) } });
export const adminDeleteSession = (id) => apiClient.delete(`/agent/admin/sessions/${id}`);
export const adminCloseSession = (id) => apiClient.put(`/agent/admin/sessions/${id}/close`);
export const adminGetSensitiveWords = () => apiClient.get('/agent/admin/sensitive-words');
export const adminAddSensitiveWord = (word) => apiClient.post('/agent/admin/sensitive-words', word);
export const adminUpdateSensitiveWord = (id, word) => apiClient.put(`/agent/admin/sensitive-words/${id}`, word);
export const adminDeleteSensitiveWord = (id) => apiClient.delete(`/agent/admin/sensitive-words/${id}`);
export const adminReloadSensitiveWords = () => apiClient.post('/agent/admin/sensitive-words/reload');