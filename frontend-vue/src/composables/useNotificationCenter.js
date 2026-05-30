import { onMounted, reactive, ref, unref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  pageNotifications
} from '../api/backend';
import { recentNotificationsQueryStore } from '../storage/localStorage';

const DEFAULT_QUERY = {
  current: 1,
  size: 10,
  readStatus: null
};

export const readStatusOptions = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 }
];

const typeLabels = {
  SYSTEM: '系统',
  TASK: '任务',
  USER: '用户'
};

/**
 * 通知中心组合式函数。
 *
 * 与 React 通知中心保持一致：恢复筛选条件、分页查询通知、刷新未读数、单条已读和全部已读。
 * 通知查询和未读数并行请求，页面打开时能同时验证列表接口和统计接口。
 */
export function useNotificationCenter(tokenRef) {
  const loading = ref(true);
  const readingId = ref(null);
  const readingAll = ref(false);
  const unreadCount = ref(0);

  const queryForm = reactive({ ...DEFAULT_QUERY });
  const query = reactive({ ...DEFAULT_QUERY });
  const page = reactive({
    current: 1,
    size: 10,
    total: 0,
    pages: 0,
    records: []
  });

  onMounted(() => {
    void restoreQueryAndLoad();
  });

  async function restoreQueryAndLoad() {
    const recent = await recentNotificationsQueryStore.get();
    const nextQuery = recent ?? { ...DEFAULT_QUERY };
    applyQuery(nextQuery);
    await loadNotifications(nextQuery);
  }

  async function loadNotifications(nextQuery = query) {
    loading.value = true;
    try {
      const normalizedQuery = normalizeQuery(nextQuery);
      const [pageData, unreadData] = await Promise.all([
        pageNotifications(unref(tokenRef), normalizedQuery),
        fetchUnreadCount(unref(tokenRef))
      ]);
      page.current = pageData.current;
      page.size = pageData.size;
      page.total = pageData.total;
      page.pages = pageData.pages;
      page.records = pageData.records;
      unreadCount.value = unreadData.count;
      applyQuery(normalizedQuery);
      await recentNotificationsQueryStore.put({ ...normalizedQuery, id: 'recent' });
    } catch (error) {
      ElMessage.error(error?.message || '加载通知列表失败');
    } finally {
      loading.value = false;
    }
  }

  function handleQuery() {
    void loadNotifications({
      ...query,
      readStatus: queryForm.readStatus,
      current: 1
    });
  }

  function handleReset() {
    applyQuery({ ...DEFAULT_QUERY });
    void loadNotifications({ ...DEFAULT_QUERY });
  }

  function handleSizeChange(size) {
    void loadNotifications({
      ...query,
      current: 1,
      size
    });
  }

  function handleCurrentChange(current) {
    void loadNotifications({
      ...query,
      current
    });
  }

  async function handleMarkRead(notification) {
    if (notification.readStatus === 1) {
      return;
    }

    readingId.value = notification.id;
    try {
      await markNotificationRead(unref(tokenRef), notification.id);
      ElMessage.success('通知已标记为已读');
      await loadNotifications(query);
    } catch (error) {
      ElMessage.error(error?.message || '标记通知已读失败');
    } finally {
      readingId.value = null;
    }
  }

  async function handleMarkAllRead() {
    readingAll.value = true;
    try {
      const result = await markAllNotificationsRead(unref(tokenRef));
      ElMessage.success(`已将 ${result.count} 条通知标记为已读`);
      await loadNotifications(query);
    } catch (error) {
      ElMessage.error(error?.message || '全部已读失败');
    } finally {
      readingAll.value = false;
    }
  }

  function applyQuery(nextQuery) {
    Object.assign(query, normalizeQuery(nextQuery));
    Object.assign(queryForm, {
      readStatus: query.readStatus
    });
  }

  return {
    queryForm,
    page,
    unreadCount,
    loading,
    readingId,
    readingAll,
    readStatusOptions,
    handleQuery,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    handleMarkRead,
    handleMarkAllRead,
    loadNotifications,
    notificationTypeLabel,
    notificationTypeTagType,
    formatDateTime
  };
}

function normalizeQuery(query) {
  return {
    current: query.current || 1,
    size: query.size || 10,
    readStatus: query.readStatus ?? null
  };
}

function notificationTypeLabel(type) {
  return typeLabels[type] || type || '-';
}

function notificationTypeTagType(type) {
  return type === 'TASK' ? 'primary' : 'info';
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ') : '-';
}
