<template>
  <div class="app-page admin-app">
    <div class="page-header">
      <div class="page-title-group">
        <div class="title-icon admin-icon">
          <icon-settings />
        </div>
        <div class="title-text">
          <h1>客服管理后台</h1>
          <span class="title-badge">Admin</span>
        </div>
      </div>
    </div>

    <div class="admin-container">
      <a-tabs v-model:active-key="activeTab">
        <a-tab-pane key="products" title="产品管理">
          <template #title>
            <span><icon-apps /> 产品管理</span>
          </template>
          <div class="tab-content">
            <div class="tab-toolbar">
              <a-button type="primary" @click="showProductDialog(null)">
                <icon-plus /> 新增产品
              </a-button>
            </div>
            <a-table :data="products" :columns="productColumns" :loading="loading.products" row-key="id" :pagination="false">
              <template #category="{ record }">
                <a-tag :color="categoryColor(record.category)" size="small">{{ record.category }}</a-tag>
              </template>
              <template #price="{ record }">
                ¥{{ record.price }}
              </template>
              <template #stock="{ record }">
                <a-tag :color="record.stock > 10 ? 'green' : 'red'" size="small">{{ record.stock }}</a-tag>
              </template>
              <template #actions="{ record }">
                <a-space>
                  <a-button type="text" size="small" @click="showProductDialog(record)">编辑</a-button>
                  <a-popconfirm content="确定删除该产品？" @ok="deleteProduct(record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table>
          </div>
        </a-tab-pane>

        <a-tab-pane key="orders" title="订单管理">
          <template #title>
            <span><icon-list /> 订单管理</span>
          </template>
          <div class="tab-content">
            <a-table :data="orders" :columns="orderColumns" :loading="loading.orders" row-key="id" :pagination="false">
              <template #status="{ record }">
                <a-tag :color="statusColor(record.status)" size="small">{{ statusText(record.status) }}</a-tag>
              </template>
              <template #totalAmount="{ record }">
                ¥{{ record.totalAmount }}
              </template>
              <template #createTime="{ record }">
                {{ formatTime(record.createTime) }}
              </template>
            </a-table>
          </div>
        </a-tab-pane>

        <a-tab-pane key="sessions" title="会话历史">
          <template #title>
            <span><icon-message /> 会话历史</span>
          </template>
          <div class="tab-content">
            <div class="tab-toolbar">
              <a-space>
                <a-select v-model="sessionStatusFilter" style="width: 140px" placeholder="状态筛选" allow-clear @change="onSessionFilterChange">
                  <a-option value="">全部状态</a-option>
                  <a-option value="ACTIVE">进行中</a-option>
                  <a-option value="CLOSED">已关闭</a-option>
                  <a-option value="TRANSFERRED">已转人工</a-option>
                </a-select>
                <span class="session-total">共 {{ sessionTotal }} 条</span>
              </a-space>
            </div>
            <a-table :data="sessions" :columns="sessionColumns" :loading="loading.sessions" row-key="id" :pagination="false">
              <template #sessionId="{ record }">
                <a-tooltip :content="record.sessionId">
                  <span class="mono-text">{{ truncateText(record.sessionId, 10) }}</span>
                </a-tooltip>
              </template>
              <template #title="{ record }">
                <span class="ellipsis-text" :title="record.title">{{ record.title || '-' }}</span>
              </template>
              <template #status="{ record }">
                <a-tag :color="sessionStatusColor(record.status)" size="small">{{ record.statusText || record.status }}</a-tag>
              </template>
              <template #satisfactionScore="{ record }">
                <span v-if="record.satisfactionScore != null">{{ record.satisfactionScore }}</span>
                <span v-else class="text-muted">-</span>
              </template>
              <template #createTime="{ record }">
                {{ formatTime(record.createTime) }}
              </template>
              <template #actions="{ record }">
                <a-space>
                  <a-button v-if="record.status === 'ACTIVE'" type="text" size="small" status="warning" @click="handleCloseSession(record.id)">
                    <icon-check /> 关闭
                  </a-button>
                  <a-popconfirm content="确定删除该会话？" @ok="handleDeleteSession(record.id)">
                    <a-button type="text" size="small" status="danger">
                      <icon-delete /> 删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table>
            <div class="session-pagination">
              <a-pagination
                v-model:current="sessionPage"
                v-model:page-size="sessionPageSize"
                :total="sessionTotal"
                show-total
                show-page-size
                @change="fetchSessions"
                @page-size-change="fetchSessions"
              />
            </div>
          </div>
        </a-tab-pane>

        <a-tab-pane key="words" title="敏感词管理">
          <template #title>
            <span><icon-exclamation-circle /> 敏感词管理</span>
          </template>
          <div class="tab-content">
            <div class="tab-toolbar">
              <a-space>
                <a-input v-model="newWord" placeholder="输入新敏感词" style="width: 200px" />
                <a-select v-model="newSeverity" style="width: 100px" placeholder="等级">
                  <a-option value="LOW">低</a-option>
                  <a-option value="MEDIUM">中</a-option>
                  <a-option value="HIGH">高</a-option>
                </a-select>
                <a-button type="primary" @click="addWord" :loading="loading.words">
                  <icon-plus /> 添加
                </a-button>
                <a-button @click="reloadWords" :loading="loading.words">
                  <icon-refresh /> 热更新
                </a-button>
              </a-space>
            </div>
            <a-table :data="sensitiveWords" :columns="wordColumns" :loading="loading.words" row-key="id" :pagination="false">
              <template #severity="{ record }">
                <a-tag :color="{ LOW: 'blue', MEDIUM: 'orange', HIGH: 'red' }[record.severity] || 'gray'" size="small">
                  {{ { LOW: '低', MEDIUM: '中', HIGH: '高' }[record.severity] || record.severity }}
                </a-tag>
              </template>
              <template #actions="{ record }">
                <a-space>
                  <a-button type="text" size="small" @click="showEditWord(record)">编辑</a-button>
                  <a-popconfirm content="确定删除该敏感词？" @ok="removeWord(record.id)">
                    <a-button type="text" size="small" status="danger">删除</a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table>

            <a-modal v-model:visible="editWordVisible" title="编辑敏感词" @ok="saveEditWord" width="400px">
              <a-form layout="vertical">
                <a-form-item label="敏感词">
                  <a-input v-model="editWordForm.word" />
                </a-form-item>
                <a-form-item label="严重等级">
                  <a-select v-model="editWordForm.severity">
                    <a-option value="LOW">低</a-option>
                    <a-option value="MEDIUM">中</a-option>
                    <a-option value="HIGH">高</a-option>
                  </a-select>
                </a-form-item>
              </a-form>
            </a-modal>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 产品编辑对话框 -->
    <a-modal v-model:visible="productDialog.visible" :title="productDialog.isEdit ? '编辑产品' : '新增产品'" @ok="saveProduct" width="500px">
      <a-form :model="productDialog.form" layout="vertical">
        <a-form-item label="产品名称">
          <a-input v-model="productDialog.form.name" placeholder="请输入产品名" />
        </a-form-item>
        <a-form-item label="品类">
          <a-select v-model="productDialog.form.category" placeholder="请选择品类">
            <a-option value="COFFEE">咖啡</a-option>
            <a-option value="TEA">茶饮</a-option>
            <a-option value="PASTRY">糕点</a-option>
            <a-option value="OTHER">其他</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="价格">
          <a-input-number v-model="productDialog.form.price" :min="0" :precision="2" placeholder="价格" style="width:100%" />
        </a-form-item>
        <a-form-item label="库存">
          <a-input-number v-model="productDialog.form.stock" :min="0" placeholder="库存" style="width:100%" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model="productDialog.form.description" placeholder="产品描述" :auto-size="{ minRows: 2 }" />
        </a-form-item>
        <a-form-item label="别名（分号分隔）">
          <a-input v-model="productDialog.form.alias" placeholder="用于搜索匹配" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import { onMounted, reactive, ref } from 'vue';
import {
  adminGetProducts, adminCreateProduct, adminUpdateProduct, adminDeleteProduct,
  adminGetOrders, adminGetSessions, adminDeleteSession, adminCloseSession,
  adminGetSensitiveWords, adminAddSensitiveWord, adminUpdateSensitiveWord, adminDeleteSensitiveWord, adminReloadSensitiveWords
} from '@/services/api';
import {
  IconSettings, IconApps, IconList, IconMessage,
  IconExclamationCircle, IconPlus, IconRefresh, IconDelete, IconCheck
} from '@arco-design/web-vue/es/icon';

export default {
  name: 'AgentAdminView',
  components: {
    IconSettings, IconApps, IconList, IconMessage,
    IconExclamationCircle, IconPlus, IconRefresh, IconDelete, IconCheck
  },
  setup() {
    const activeTab = ref('products');
    const newWord = ref('');
    const newSeverity = ref('MEDIUM');

    const products = ref([]);
    const orders = ref([]);
    const sessions = ref([]);
    const sensitiveWords = ref([]);
    const editWordVisible = ref(false);
    const editWordForm = reactive({ id: null, word: '', severity: 'MEDIUM' });

    const sessionPage = ref(1);
    const sessionPageSize = ref(10);
    const sessionTotal = ref(0);
    const sessionStatusFilter = ref('');

    const loading = reactive({
      products: false,
      orders: false,
      sessions: false,
      words: false
    });

    const productDialog = reactive({
      visible: false,
      isEdit: false,
      form: { name: '', category: 'COFFEE', price: 0, stock: 0, description: '', alias: '' },
      editId: null
    });

    const productColumns = [
      { title: '名称', dataIndex: 'name' },
      { title: '品类', slotName: 'category' },
      { title: '价格', slotName: 'price' },
      { title: '库存', slotName: 'stock' },
      { title: '操作', slotName: 'actions', width: 160 }
    ];

    const orderColumns = [
      { title: 'ID', dataIndex: 'id', width: 80 },
      { title: '订单编号', dataIndex: 'orderNo', width: 180 },
      { title: '商品概要', dataIndex: 'productSummary' },
      { title: '数量', dataIndex: 'itemCount', width: 60 },
      { title: '金额', slotName: 'totalAmount', width: 100 },
      { title: '状态', slotName: 'status', width: 100 },
      { title: '下单时间', slotName: 'createTime', width: 170 }
    ];

    const sessionColumns = [
      { title: 'ID', dataIndex: 'id', width: 60 },
      { title: '会话ID', slotName: 'sessionId', width: 130 },
      { title: '用户ID', dataIndex: 'userId', width: 100, ellipsis: true },
      { title: '标题', slotName: 'title', width: 160, ellipsis: true },
      { title: '意图', dataIndex: 'intent', width: 80 },
      { title: '状态', slotName: 'status', width: 90 },
      { title: '评分', slotName: 'satisfactionScore', width: 60 },
      { title: '创建时间', slotName: 'createTime', width: 170 },
      { title: '操作', slotName: 'actions', width: 140 }
    ];

    const wordColumns = [
      { title: '敏感词', dataIndex: 'word' },
      { title: '严重等级', slotName: 'severity', width: 100 },
      { title: '操作', slotName: 'actions', width: 160 }
    ];

    const fetchProducts = async () => {
      loading.products = true;
      try {
        const res = await adminGetProducts();
        const page = res.data?.data;
        products.value = page?.records || [];
      } catch (e) {
        console.error('获取产品失败', e);
      }
      loading.products = false;
    };

    const fetchOrders = async () => {
      loading.orders = true;
      try {
        const res = await adminGetOrders();
        const page = res.data?.data;
        orders.value = page?.records || [];
      } catch (e) {
        console.error('获取订单失败', e);
      }
      loading.orders = false;
    };

    const fetchSessions = async () => {
      loading.sessions = true;
      try {
        const res = await adminGetSessions(sessionPage.value, sessionPageSize.value, sessionStatusFilter.value);
        const page = res.data?.data;
        sessions.value = page?.records || [];
        sessionTotal.value = page?.total || 0;
      } catch (e) {
        console.error('获取会话失败', e);
      }
      loading.sessions = false;
    };

    const onSessionFilterChange = () => {
      sessionPage.value = 1;
      fetchSessions();
    };

    const fetchWords = async () => {
      loading.words = true;
      try {
        const res = await adminGetSensitiveWords();
        const page = res.data?.data;
        sensitiveWords.value = page?.records || [];
      } catch (e) {
        console.error('获取敏感词失败', e);
      }
      loading.words = false;
    };

    const showProductDialog = (record) => {
      if (record) {
        productDialog.isEdit = true;
        productDialog.editId = record.id;
        productDialog.form = { ...record };
      } else {
        productDialog.isEdit = false;
        productDialog.editId = null;
        productDialog.form = { name: '', category: 'COFFEE', price: 0, stock: 0, description: '', alias: '' };
      }
      productDialog.visible = true;
    };

    const saveProduct = async () => {
      try {
        if (productDialog.isEdit) {
          await adminUpdateProduct(productDialog.editId, productDialog.form);
        } else {
          await adminCreateProduct(productDialog.form);
        }
        productDialog.visible = false;
        fetchProducts();
      } catch (e) {
        console.error('保存产品失败', e);
      }
    };

    const deleteProduct = async (id) => {
      try {
        await adminDeleteProduct(id);
        fetchProducts();
      } catch (e) {
        console.error('删除产品失败', e);
      }
    };

    const addWord = async () => {
      if (!newWord.value.trim()) return;
      try {
        await adminAddSensitiveWord({ word: newWord.value.trim(), severity: newSeverity.value });
        newWord.value = '';
        newSeverity.value = 'MEDIUM';
        fetchWords();
      } catch (e) {
        console.error('添加敏感词失败', e);
      }
    };

    const showEditWord = (record) => {
      editWordForm.id = record.id;
      editWordForm.word = record.word;
      editWordForm.severity = record.severity;
      editWordVisible.value = true;
    };

    const saveEditWord = async () => {
      try {
        await adminUpdateSensitiveWord(editWordForm.id, {
          word: editWordForm.word,
          severity: editWordForm.severity
        });
        editWordVisible.value = false;
        fetchWords();
      } catch (e) {
        console.error('更新敏感词失败', e);
      }
    };

    const removeWord = (id) => {
      adminDeleteSensitiveWord(id).then(fetchWords);
    };

    const reloadWords = async () => {
      try {
        await adminReloadSensitiveWords();
        fetchWords();
      } catch (e) {
        console.error('热更新失败', e);
      }
    };

    const handleCloseSession = async (id) => {
      try {
        await adminCloseSession(id);
        fetchSessions();
      } catch (e) {
        console.error('关闭会话失败', e);
      }
    };

    const handleDeleteSession = async (id) => {
      try {
        await adminDeleteSession(id);
        fetchSessions();
      } catch (e) {
        console.error('删除会话失败', e);
      }
    };

    const truncateText = (text, max) => {
      if (!text) return '';
      return text.length > max ? text.slice(0, max) + '...' : text;
    };

    onMounted(() => {
      fetchProducts();
      fetchOrders();
      fetchSessions();
      fetchWords();
    });

    return {
      activeTab, newWord, newSeverity,
      products, orders, sessions, sensitiveWords,
      editWordVisible, editWordForm,
      loading, productDialog,
      productColumns, orderColumns, sessionColumns, wordColumns,
      sessionPage, sessionPageSize, sessionTotal, sessionStatusFilter,
      showProductDialog, saveProduct, deleteProduct,
      addWord, showEditWord, saveEditWord, removeWord, reloadWords,
      fetchSessions, onSessionFilterChange,
      handleCloseSession, handleDeleteSession,
      truncateText,
      categoryColor: (c) => ({ COFFEE: 'orange', TEA: 'green', PASTRY: 'purple', OTHER: 'gray' }[c] || 'gray'),
      statusColor: (s) => ({ PENDING: 'orange', CONFIRMED: 'blue', PREPARING: 'cyan', COMPLETED: 'green', CANCELLED: 'gray' }[s] || 'gray'),
      statusText: (s) => ({ PENDING: '待确认', CONFIRMED: '已确认', PREPARING: '制作中', COMPLETED: '已完成', CANCELLED: '已取消' }[s] || s),
      sessionStatusColor: (s) => ({ ACTIVE: 'green', CLOSED: 'gray', TRANSFERRED: 'blue' }[s] || 'gray'),
      formatTime: (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'
    };
  }
};
</script>

<style scoped>
.admin-app {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: var(--space-xl);
  overflow-y: auto;
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

.admin-icon {
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

.admin-container {
  background: var(--color-bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: var(--space-lg);
}

.tab-toolbar {
  margin-bottom: var(--space-md);
}

.tab-content {
  min-height: 300px;
}

.session-total {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 32px;
}

.session-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-md);
  padding-top: var(--space-md);
  border-top: 1px solid var(--border-color);
}

.mono-text {
  font-family: 'SF Mono', 'Cascadia Code', 'Fira Code', monospace;
  font-size: 12px;
  color: var(--text-secondary);
}

.ellipsis-text {
  display: block;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-muted {
  color: var(--text-tertiary);
}
</style>
