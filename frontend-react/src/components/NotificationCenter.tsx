import { Badge, Button, Card, Form, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useEffect, useState } from 'react';
import {
  fetchUnreadCount,
  markAllNotificationsRead,
  markNotificationRead,
  pageNotifications
} from '../api/backend';
import { recentNotificationsQueryStore } from '../storage/indexedDb';
import type { NotificationItem, NotificationsQuery, PageResponse } from '../types';

interface NotificationCenterProps {
  token: string;
}

const DEFAULT_QUERY: NotificationsQuery = {
  current: 1,
  size: 10,
  readStatus: null
};

const DEFAULT_PAGE: PageResponse<NotificationItem> = {
  current: 1,
  size: 10,
  total: 0,
  pages: 0,
  records: []
};

const READ_STATUS_OPTIONS = [
  { label: '未读', value: 0 },
  { label: '已读', value: 1 }
];

const TYPE_LABELS: Record<string, string> = {
  SYSTEM: '系统',
  TASK: '任务',
  USER: '用户'
};

/**
 * 通知中心页。
 *
 * 该页面承接 notification-service：展示当前用户通知、未读数量、单条已读和全部已读。
 * 查询条件会保存到 IndexedDB，保持 React 端与用户列表、任务列表一致的本地缓存学习路径。
 */
export function NotificationCenter({ token }: NotificationCenterProps) {
  const [messageApi, contextHolder] = message.useMessage();
  const [queryForm] = Form.useForm<NotificationsQuery>();
  const [query, setQuery] = useState<NotificationsQuery>(DEFAULT_QUERY);
  const [page, setPage] = useState<PageResponse<NotificationItem>>(DEFAULT_PAGE);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [readingId, setReadingId] = useState<number | null>(null);
  const [readingAll, setReadingAll] = useState(false);

  useEffect(() => {
    void restoreQueryAndLoad();
  }, []);

  async function restoreQueryAndLoad() {
    const recent = await recentNotificationsQueryStore.get();
    const nextQuery = recent ?? { ...DEFAULT_QUERY };
    setQuery(nextQuery);
    queryForm.setFieldsValue(nextQuery);
    await loadNotifications(nextQuery);
  }

  async function loadNotifications(nextQuery: NotificationsQuery = query) {
    setLoading(true);
    try {
      const normalizedQuery = normalizeQuery(nextQuery);
      const [pageData, unreadData] = await Promise.all([
        pageNotifications(token, normalizedQuery),
        fetchUnreadCount(token)
      ]);
      setPage(pageData);
      setUnreadCount(unreadData.count);
      setQuery(normalizedQuery);
      await recentNotificationsQueryStore.put({ ...normalizedQuery, id: 'recent' });
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '加载通知列表失败');
    } finally {
      setLoading(false);
    }
  }

  async function handleMarkRead(notification: NotificationItem) {
    if (notification.readStatus === 1) {
      return;
    }
    setReadingId(notification.id);
    try {
      await markNotificationRead(token, notification.id);
      messageApi.success('通知已标记为已读');
      await loadNotifications(query);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '标记通知已读失败');
    } finally {
      setReadingId(null);
    }
  }

  async function handleMarkAllRead() {
    setReadingAll(true);
    try {
      const result = await markAllNotificationsRead(token);
      messageApi.success(`已将 ${result.count} 条通知标记为已读`);
      await loadNotifications(query);
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '全部已读失败');
    } finally {
      setReadingAll(false);
    }
  }

  function handleTableChange(pagination: TablePaginationConfig) {
    void loadNotifications({
      ...query,
      current: pagination.current ?? 1,
      size: pagination.pageSize ?? 10
    });
  }

  const columns: ColumnsType<NotificationItem> = [
    { title: 'ID', dataIndex: 'id', width: 80 },
    {
      title: '标题',
      dataIndex: 'title',
      minWidth: 180,
      render: (value, record) => (
        <Space direction="vertical" size={2}>
          <Typography.Text strong={record.readStatus === 0}>{value}</Typography.Text>
          <Typography.Text type="secondary" className="table-subtext">
            {record.content}
          </Typography.Text>
        </Space>
      )
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
      render: (value: string) => <Tag color={value === 'TASK' ? 'blue' : 'default'}>{TYPE_LABELS[value] ?? value}</Tag>
    },
    {
      title: '业务',
      dataIndex: 'bizType',
      width: 150,
      render: (_, record) => `${record.bizType || 'GENERAL'}${record.bizId ? ` #${record.bizId}` : ''}`
    },
    {
      title: '状态',
      dataIndex: 'readStatus',
      width: 110,
      render: (value: number) =>
        value === 0 ? <Badge status="processing" text="未读" /> : <Badge status="default" text="已读" />
    },
    { title: '创建时间', dataIndex: 'createdAt', minWidth: 170, render: displayDateTime },
    { title: '已读时间', dataIndex: 'readAt', minWidth: 170, render: displayDateTime },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      render: (_, record) => (
        <Button
          size="small"
          disabled={record.readStatus === 1}
          loading={readingId === record.id}
          onClick={() => void handleMarkRead(record)}
        >
          标记已读
        </Button>
      )
    }
  ];

  return (
    <div className="page-stack">
      {contextHolder}
      <Card variant="borderless" className="toolbar-card">
        <div className="toolbar-title">
          <div>
            <Typography.Text className="hero-kicker">Notification Inbox</Typography.Text>
            <Typography.Title level={3}>通知中心</Typography.Title>
            <Typography.Paragraph type="secondary">
              当前未读 <Typography.Text strong>{unreadCount}</Typography.Text> 条。任务创建、负责人变更和状态流转都会生成通知。
            </Typography.Paragraph>
          </div>
          <Space>
            <Button onClick={() => void loadNotifications(query)}>刷新</Button>
            <Popconfirm title="确认将当前用户全部通知标记为已读？" onConfirm={() => void handleMarkAllRead()}>
              <Button type="primary" loading={readingAll}>
                全部已读
              </Button>
            </Popconfirm>
          </Space>
        </div>

        <Form
          form={queryForm}
          layout="inline"
          className="query-form"
          initialValues={DEFAULT_QUERY}
          onFinish={(values) => void loadNotifications({ ...query, ...values, current: 1 })}
        >
          <Form.Item label="已读状态" name="readStatus">
            <Select allowClear placeholder="全部通知" style={{ width: 150 }} options={READ_STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                查询
              </Button>
              <Button
                onClick={() => {
                  queryForm.setFieldsValue(DEFAULT_QUERY);
                  void loadNotifications({ ...DEFAULT_QUERY });
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
          scroll={{ x: 1080 }}
        />
      </Card>
    </div>
  );
}

function normalizeQuery(query: NotificationsQuery): NotificationsQuery {
  return {
    current: query.current || 1,
    size: query.size || 10,
    readStatus: query.readStatus ?? null
  };
}

function displayDateTime(value?: string | null) {
  return value ? value.replace('T', ' ') : '-';
}
