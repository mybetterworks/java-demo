import { onMounted, reactive, ref, unref } from 'vue';
import { ElMessage } from 'element-plus';
import { createTask, deleteTask, getTask, pageTasks, updateTask, updateTaskStatus } from '../api/backend';
import { recentTasksQueryStore } from '../storage/localStorage';

const DEFAULT_QUERY = {
  current: 1,
  size: 10,
  scope: 'my',
  status: null,
  assigneeUserId: null
};

const DEFAULT_TASK_FORM = {
  title: '',
  description: '',
  assigneeUserId: null,
  priority: 'MEDIUM',
  dueTime: ''
};

export const taskStatusOptions = [
  { label: '待处理', value: 'TODO', tagType: 'info' },
  { label: '进行中', value: 'IN_PROGRESS', tagType: 'primary' },
  { label: '已完成', value: 'DONE', tagType: 'success' },
  { label: '已取消', value: 'CANCELLED', tagType: 'warning' }
];

export const taskPriorityOptions = [
  { label: '低', value: 'LOW', tagType: 'info' },
  { label: '中', value: 'MEDIUM', tagType: 'warning' },
  { label: '高', value: 'HIGH', tagType: 'danger' }
];

const taskRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { max: 120, message: '任务标题最多 120 个字符', trigger: 'blur' }
  ],
  description: [{ max: 2000, message: '任务描述最多 2000 个字符', trigger: 'blur' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
};

/**
 * 任务管理组合式函数。
 *
 * 与 React 页面的业务路径保持一致：恢复查询条件、分页查询、创建/编辑任务、状态流转、详情查看和逻辑删除。
 * Vue 端把这些状态和动作放在 composable 中，页面 SFC 主要负责模板，便于和 React 的组件状态写法做对比学习。
 */
export function useTaskManagement(tokenRef) {
  const taskFormRef = ref();
  const loading = ref(true);
  const saving = ref(false);
  const dialogVisible = ref(false);
  const editingTask = ref(null);
  const detailVisible = ref(false);
  const detailLoading = ref(false);
  const detailTask = ref(null);
  const statusChangingId = ref(null);

  const queryForm = reactive({ ...DEFAULT_QUERY });
  const query = reactive({ ...DEFAULT_QUERY });
  const page = reactive({
    current: 1,
    size: 10,
    total: 0,
    pages: 0,
    records: []
  });
  const taskForm = reactive({ ...DEFAULT_TASK_FORM });

  onMounted(() => {
    void restoreQueryAndLoad();
  });

  async function restoreQueryAndLoad() {
    const recent = await recentTasksQueryStore.get();
    const nextQuery = recent ?? { ...DEFAULT_QUERY };
    applyQuery(nextQuery);
    await loadTasks(nextQuery);
  }

  async function loadTasks(nextQuery = query) {
    loading.value = true;
    try {
      const normalizedQuery = normalizeQuery(nextQuery);
      const data = await pageTasks(unref(tokenRef), normalizedQuery);
      page.current = data.current;
      page.size = data.size;
      page.total = data.total;
      page.pages = data.pages;
      page.records = data.records;
      applyQuery(normalizedQuery);
      await recentTasksQueryStore.put({ ...normalizedQuery, id: 'recent' });
    } catch (error) {
      ElMessage.error(error?.message || '加载任务列表失败');
    } finally {
      loading.value = false;
    }
  }

  function handleQuery() {
    void loadTasks({
      ...query,
      scope: queryForm.scope,
      status: queryForm.status,
      assigneeUserId: queryForm.assigneeUserId,
      current: 1
    });
  }

  function handleReset() {
    applyQuery({ ...DEFAULT_QUERY });
    void loadTasks({ ...DEFAULT_QUERY });
  }

  function handleSizeChange(size) {
    void loadTasks({
      ...query,
      current: 1,
      size
    });
  }

  function handleCurrentChange(current) {
    void loadTasks({
      ...query,
      current
    });
  }

  function openCreateDialog() {
    editingTask.value = null;
    Object.assign(taskForm, { ...DEFAULT_TASK_FORM });
    dialogVisible.value = true;
  }

  function openEditDialog(task) {
    editingTask.value = task;
    Object.assign(taskForm, {
      title: task.title,
      description: task.description || '',
      assigneeUserId: task.assigneeUserId,
      priority: task.priority || 'MEDIUM',
      dueTime: task.dueTime || ''
    });
    dialogVisible.value = true;
  }

  async function openDetailDialog(task) {
    detailVisible.value = true;
    detailLoading.value = true;
    try {
      /**
       * 详情弹窗主动调用 /api/tasks/{id}，用于验证详情接口链路。
       * 当前字段和表格接近，但后续扩展附件、审计记录时可以直接复用这个入口。
       */
      detailTask.value = await getTask(unref(tokenRef), task.id);
    } catch (error) {
      ElMessage.error(error?.message || '加载任务详情失败');
      detailVisible.value = false;
    } finally {
      detailLoading.value = false;
    }
  }

  async function handleSave() {
    const valid = await taskFormRef.value?.validate().catch(() => false);
    if (!valid) {
      return;
    }

    saving.value = true;
    try {
      const payload = normalizeTaskPayload(taskForm);
      if (editingTask.value) {
        await updateTask(unref(tokenRef), editingTask.value.id, payload);
        ElMessage.success('任务已更新');
      } else {
        await createTask(unref(tokenRef), payload);
        ElMessage.success('任务已创建，并已触发通知');
      }

      dialogVisible.value = false;
      await loadTasks(query);
    } catch (error) {
      ElMessage.error(error?.message || '保存任务失败');
    } finally {
      saving.value = false;
    }
  }

  async function handleStatusChange(task, nextStatus) {
    if (task.status === nextStatus) {
      return;
    }

    statusChangingId.value = task.id;
    try {
      await updateTaskStatus(unref(tokenRef), task.id, nextStatus);
      ElMessage.success('任务状态已更新，并已生成通知');
      await loadTasks(query);
    } catch (error) {
      ElMessage.error(error?.message || '更新任务状态失败');
    } finally {
      statusChangingId.value = null;
    }
  }

  async function handleDelete(task) {
    try {
      await deleteTask(unref(tokenRef), task.id);
      ElMessage.success(`已逻辑删除任务「${task.title}」`);
      await loadTasks(query);
    } catch (error) {
      ElMessage.error(error?.message || '删除任务失败');
    }
  }

  function applyQuery(nextQuery) {
    Object.assign(query, normalizeQuery(nextQuery));
    Object.assign(queryForm, {
      scope: query.scope,
      status: query.status,
      assigneeUserId: query.assigneeUserId
    });
  }

  return {
    queryForm,
    page,
    taskForm,
    taskRules,
    taskFormRef,
    loading,
    saving,
    dialogVisible,
    editingTask,
    detailVisible,
    detailLoading,
    detailTask,
    statusChangingId,
    taskStatusOptions,
    taskPriorityOptions,
    handleQuery,
    handleReset,
    handleSizeChange,
    handleCurrentChange,
    openCreateDialog,
    openEditDialog,
    openDetailDialog,
    handleSave,
    handleStatusChange,
    handleDelete,
    taskStatusLabel,
    taskStatusTagType,
    taskPriorityLabel,
    taskPriorityTagType,
    formatDateTime
  };
}

function normalizeQuery(query) {
  return {
    current: query.current || 1,
    size: query.size || 10,
    scope: query.scope || 'my',
    status: query.status ?? null,
    assigneeUserId: query.assigneeUserId ?? null
  };
}

function normalizeTaskPayload(values) {
  /**
   * 表单留空的字段不发送给后端，保留后端默认行为：
   * 负责人不填时默认当前用户，描述和截止时间不填时不占用无意义空字符串。
   */
  const payload = {
    title: values.title.trim(),
    priority: values.priority
  };
  if (values.description?.trim()) {
    payload.description = values.description.trim();
  }
  if (values.assigneeUserId) {
    payload.assigneeUserId = values.assigneeUserId;
  }
  if (values.dueTime?.trim()) {
    payload.dueTime = values.dueTime.trim();
  }
  return payload;
}

function taskStatusLabel(status) {
  return taskStatusOptions.find((item) => item.value === status)?.label || status || '-';
}

function taskStatusTagType(status) {
  return taskStatusOptions.find((item) => item.value === status)?.tagType || 'info';
}

function taskPriorityLabel(priority) {
  return taskPriorityOptions.find((item) => item.value === priority)?.label || priority || '-';
}

function taskPriorityTagType(priority) {
  return taskPriorityOptions.find((item) => item.value === priority)?.tagType || 'info';
}

function formatDateTime(value) {
  return value ? String(value).replace('T', ' ') : '-';
}
