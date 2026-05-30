import {
  Button,
  Card,
  Descriptions,
  Form,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message
} from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { createTask, deleteTask, getTask, pageTasks, updateTask, updateTaskStatus } from '../api/backend';
import { recentTasksQueryStore } from '../storage/indexedDb';
import type {
  CreateTaskRequest,
  PageResponse,
  TaskItem,
  TaskPriority,
  TasksQuery,
  TaskStatus,
  UpdateTaskRequest
} from '../types';

interface TaskManagementProps {
  token: string;
}

type TaskFormValues = {
  title: string;
  description?: string;
  assigneeUserId?: number | null;
  priority: TaskPriority;
  dueTime?: string;
};

const DEFAULT_QUERY: TasksQuery = {
  current: 1,
  size: 10,
  scope: 'my',
  status: null,
  assigneeUserId: null
};

const DEFAULT_PAGE: PageResponse<TaskItem> = {
  current: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: []
};

const TASK_STATUS_OPTIONS: Array<{ label: string; value: TaskStatus; color: string }> = [
  { label: '待处理', value: 'TODO', color: 'default' },
  { label: '进行中', value: 'IN_PROGRESS', color: 'processing' },
  { label: '已完成', value: 'DONE', color: 'success' },
  { label: '已取消', value: 'CANCELLED', color: 'warning' }
];

const TASK_PRIORITY_OPTIONS: Array<{ label: string; value: TaskPriority; color: string }> = [
  { label: '低', value: 'LOW', color: 'blue' },
  { label: '中', value: 'MEDIUM', color: 'gold' },
  { label: '高', value: 'HIGH', color: 'red' }
];

const TASK_STATUS_META = Object.fromEntries(
  TASK_STATUS_OPTIONS.map((item) => [item.value, item])
) as Record<TaskStatus, (typeof TASK_STATUS_OPTIONS)[number]>;

const TASK_PRIORITY_META = Object.fromEntries(
  TASK_PRIORITY_OPTIONS.map((item) => [item.value, item])
) as Record<TaskPriority, (typeof TASK_PRIORITY_OPTIONS)[number]>;

/**
 * 任务管理页。
 *
 * 该页面承接 v0.5.1 新增的 task-service：用同一个页面完成任务查询、创建、编辑、
 * 状态流转、详情查看和逻辑删除。查询条件会保存到 IndexedDB，方便刷新页面后继续学习同一组筛选条件。
 */
export function TaskManagement({ token }: TaskManagementProps) {
  const [messageApi, contextHolder] = message.useMessage();
  const [queryForm] = Form.useForm<TasksQuery>();
  const [taskForm] = Form.useForm<TaskFormValues>();
  const [query, setQuery] = useState<TasksQuery>(DEFAULT_QUERY);
  const [page, setPage] = useState<PageResponse<TaskItem>>(DEFAULT_PAGE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<TaskItem | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detailTask, setDetailTask] = useState<TaskItem | null>(null);
  const [statusChangingId, setStatusChangingId] = useState<number | null>(null);

  useEffect(() => {
    void restoreQueryAndLoad();
  }, []);

  async function restoreQueryAndLoad() {
    const recent = await recentTasksQueryStore.get();
    const nextQuery = recent ?? { ...DEFAULT_QUERY };
    setQuery(nextQuery);
    queryForm.setFieldsValue(nextQuery);
    await loadTasks(nextQuery);
  }

  async function loadTasks(nextQuery: TasksQuery = query) {
    setLoading(true);
    try {
      const normalizedQuery = normalizeQuery(nextQuery);
      const data = await pageTasks(token, normalizedQuery);
      setPage(data);
      setQuery(normalizedQuery);
      await recentTasksQueryStore.put({ ...normalizedQuery, id: 'recent' });
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '加载任务列表失败');
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setEditingTask(null);
    taskForm.resetFields();
    taskForm.setFieldsValue({ priority: 'MEDIUM' });
    setModalOpen(true);
  }

  function openEditModal(task: TaskItem) {
    setEditingTask(task);
    taskForm.setFieldsValue({
      title: task.title,
      description: task.description ?? '',
      assigneeUserId: task.assigneeUserId,
      priority: task.priority || 'MEDIUM',
      dueTime: task.dueTime ?? ''
    });
    setModalOpen(true);
  }

  async function openDetailModal(task: TaskItem) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      /**
       * 表格本身已经有基础字段，但详情弹窗仍调用 /api/tasks/{id}。
       * 这样可以验证“详情接口”这条链路，也方便后续给详情补充附件、审计记录等扩展字段。
       */
      setDetailTask(await getTask(token, task.id));
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '加载任务详情失败');
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleSave() {
    const values = await taskForm.validateFields();
    setSaving(true);
    try {
      if (editingTask) {
        await updateTask(token, editingTask.id, normalizeTaskPayload(values));
        messageApi.success('任务已更新');
      } else {
        await createTask(token, normalizeTaskPayload(values));
        messageApi.success('任务已创建，并已触发通知');
      }
      setModalOpen(false);
      await loadTasks(query);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '保存任务失败');
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusChange(task: TaskItem, status: TaskStatus) {
    if (task.status === status) {
      return;
    }
    setStatusChangingId(task.id);
    try {
      await updateTaskStatus(token, task.id, status);
      messageApi.success('任务状态已更新，并已生成通知');
      await loadTasks(query);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '更新任务状态失败');
    } finally {
      setStatusChangingId(null);
    }
  }

  async function handleDelete(task: TaskItem) {
    try {
      await deleteTask(token, task.id);
      messageApi.success(`已逻辑删除任务「${task.title}」`);
      await loadTasks(query);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '删除任务失败');
    }
  }

  function handleTableChange(pagination: TablePaginationConfig) {
    void loadTasks({
      ...query,
      current: pagination.current ?? 1,
      size: pagination.pageSize ?? 10
    });
  }

  const columns: ColumnsType<TaskItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: '标题',
      dataIndex: 'title',
      minWidth: 220,
      render: (value) => <Typography.Text strong>{value}</Typography.Text>
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 150,
      render: (value: TaskStatus, record) => (
        <Select
          size="small"
          value={value}
          loading={statusChangingId === record.id}
          disabled={statusChangingId === record.id}
          options={TASK_STATUS_OPTIONS.map(({ label, value: optionValue }) => ({ label, value: optionValue }))}
          onChange={(nextStatus) => void handleStatusChange(record, nextStatus)}
        />
      )
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      width: 110,
      render: (value: TaskPriority) => {
        const meta = TASK_PRIORITY_META[value] ?? TASK_PRIORITY_META.MEDIUM;
        return <Tag color={meta.color}>{meta.label}</Tag>;
      }
    },
    { title: '负责人ID', dataIndex: 'assigneeUserId', width: 110 },
    { title: '截止时间', dataIndex: 'dueTime', minWidth: 170, render: displayDateTime },
    { title: '更新时间', dataIndex: 'updatedAt', minWidth: 170, render: displayDateTime },
    {
      title: '操作',
      width: 230,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => void openDetailModal(record)}>
            详情
          </Button>
          <Button size="small" onClick={() => openEditModal(record)}>
            编辑
          </Button>
          <Popconfirm title="确认逻辑删除该任务？" onConfirm={() => void handleDelete(record)}>
            <Button size="small" danger>
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div className="page-stack">
      {contextHolder}
      <Card variant="borderless" className="toolbar-card">
        <div className="toolbar-title">
          <div>
            <Typography.Text className="hero-kicker">Task Workflow</Typography.Text>
            <Typography.Title level={3}>任务管理</Typography.Title>
            <Typography.Paragraph type="secondary">
              通过 Gateway 调用 task-service，创建任务时会同步触发 notification-service 生成通知。
            </Typography.Paragraph>
          </div>
          <Button type="primary" onClick={openCreateModal}>
            新增任务
          </Button>
        </div>

        <Form
          form={queryForm}
          layout="inline"
          className="query-form"
          initialValues={DEFAULT_QUERY}
          onFinish={(values) => void loadTasks({ ...query, ...values, current: 1 })}
        >
          <Form.Item label="范围" name="scope">
            <Select
              style={{ width: 150 }}
              options={[
                { label: '我的任务', value: 'my' },
                { label: '全部任务', value: 'all' }
              ]}
            />
          </Form.Item>
          <Form.Item label="状态" name="status">
            <Select
              allowClear
              placeholder="全部状态"
              style={{ width: 150 }}
              options={TASK_STATUS_OPTIONS.map(({ label, value }) => ({ label, value }))}
            />
          </Form.Item>
          <Form.Item label="负责人ID" name="assigneeUserId">
            <InputNumber min={1} placeholder="全部任务可用" style={{ width: 150 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                查询
              </Button>
              <Button
                onClick={() => {
                  queryForm.setFieldsValue(DEFAULT_QUERY);
                  void loadTasks({ ...DEFAULT_QUERY });
                }}
              >
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>

      <Card variant="borderless">
        <Table
          rowKey="id"
          loading={loading}
          columns={columns}
          dataSource={page.records}
          onChange={handleTableChange}
          pagination={{
            current: page.current,
            pageSize: page.size,
            total: page.total,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`
          }}
          scroll={{ x: 1180 }}
        />
      </Card>

      <Modal
        title={editingTask ? `编辑任务：${editingTask.title}` : '新增任务'}
        open={modalOpen}
        confirmLoading={saving}
        onOk={() => void handleSave()}
        onCancel={() => setModalOpen(false)}
        destroyOnHidden
        forceRender
      >
        <Form form={taskForm} layout="vertical">
          <Form.Item
            label="任务标题"
            name="title"
            rules={[
              { required: true, message: '请输入任务标题' },
              { max: 120, message: '任务标题最多 120 个字符' }
            ]}
          >
            <Input placeholder="例如 学习 v0.5.3 前端联调" />
          </Form.Item>
          <Form.Item label="任务描述" name="description" rules={[{ max: 2000, message: '任务描述最多 2000 个字符' }]}>
            <Input.TextArea rows={4} placeholder="记录任务背景、验收方式或学习目标" />
          </Form.Item>
          <Form.Item label="负责人用户 ID" name="assigneeUserId" tooltip="不填时后端会默认分配给当前登录用户">
            <InputNumber min={1} placeholder="不填默认当前用户" className="full-width-control" />
          </Form.Item>
          <Form.Item label="优先级" name="priority" rules={[{ required: true, message: '请选择优先级' }]}>
            <Select options={TASK_PRIORITY_OPTIONS.map(({ label, value }) => ({ label, value }))} />
          </Form.Item>
          <Form.Item label="截止时间" name="dueTime" tooltip="后端 LocalDateTime 使用 2026-05-28T18:00:00 这种格式">
            <Input placeholder="例如 2026-05-28T18:00:00，可不填" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="任务详情"
        open={detailOpen}
        footer={null}
        loading={detailLoading}
        onCancel={() => setDetailOpen(false)}
        destroyOnHidden
      >
        {detailTask && (
          <Descriptions column={1} bordered size="small">
            <Descriptions.Item label="任务ID">{detailTask.id}</Descriptions.Item>
            <Descriptions.Item label="标题">{detailTask.title}</Descriptions.Item>
            <Descriptions.Item label="描述">{detailTask.description || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">
              <TaskStatusTag status={detailTask.status} />
            </Descriptions.Item>
            <Descriptions.Item label="优先级">
              <TaskPriorityTag priority={detailTask.priority} />
            </Descriptions.Item>
            <Descriptions.Item label="创建人ID">{detailTask.creatorUserId}</Descriptions.Item>
            <Descriptions.Item label="负责人ID">{detailTask.assigneeUserId}</Descriptions.Item>
            <Descriptions.Item label="截止时间">{displayDateTime(detailTask.dueTime)}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{displayDateTime(detailTask.createdAt)}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{displayDateTime(detailTask.updatedAt)}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
}

function TaskStatusTag({ status }: { status: TaskStatus }) {
  const meta = TASK_STATUS_META[status] ?? TASK_STATUS_META.TODO;
  return <Tag color={meta.color}>{meta.label}</Tag>;
}

function TaskPriorityTag({ priority }: { priority: TaskPriority }) {
  const meta = TASK_PRIORITY_META[priority] ?? TASK_PRIORITY_META.MEDIUM;
  return <Tag color={meta.color}>{meta.label}</Tag>;
}

function normalizeQuery(query: TasksQuery): TasksQuery {
  return {
    current: query.current || 1,
    size: query.size || 10,
    scope: query.scope || 'my',
    status: query.status ?? null,
    assigneeUserId: query.assigneeUserId ?? null
  };
}

function normalizeTaskPayload(values: TaskFormValues): CreateTaskRequest & UpdateTaskRequest {
  /**
   * 表单里允许用户留空负责人、描述和截止时间；发送给后端前统一把空字符串转为 undefined。
   * 这样既避免后端收到无意义空值，也保留“负责人不填默认当前用户”的业务语义。
   */
  const payload: CreateTaskRequest & UpdateTaskRequest = {
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

function displayDateTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-';
}
