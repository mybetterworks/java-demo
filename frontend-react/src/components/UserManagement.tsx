import { Button, Card, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag, Typography, message } from 'antd'; // 引入 Ant Design 的组件库
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'; // 引入表格列和分页配置的类型定义
import { useEffect, useState } from 'react'; // 引入 React 的 Hook，用于管理状态和副作用
import { createUser, deleteUser, pageUsers, updateUser } from '../api/backend'; // 引入后端 API 封装的函数
import { recentUsersQueryStore } from '../storage/indexedDb'; // 引入 IndexedDB 封装，用于存储最近查询条件
import type { CreateUserRequest, PageResponse, UpdateUserRequest, UserProfile, UsersQuery } from '../types'; // 引入类型定义，确保代码类型安全

// 定义组件的 Props 类型，接收 token 作为参数
interface UserManagementProps {
  token: string;
}

// 定义用户表单的值类型，包含创建和更新用户的字段
type UserFormValues = CreateUserRequest & UpdateUserRequest;

// 定义默认的查询条件
const DEFAULT_QUERY: UsersQuery = {
  current: 1, // 当前页码
  size: 10, // 每页显示的记录数
  username: '', // 用户名查询条件
  status: null // 用户状态查询条件
};

/**
 * 用户管理组件
 * 负责用户的查询、分页加载、新增、编辑和逻辑删除功能。
 */
export function UserManagement({ token }: UserManagementProps) {
  // 定义 Ant Design 的消息提示 API
  const [messageApi, contextHolder] = message.useMessage();
  // 定义查询表单和用户表单的实例
  const [queryForm] = Form.useForm<UsersQuery>();
  const [userForm] = Form.useForm<UserFormValues>();
  // 定义组件的状态
  const [query, setQuery] = useState<UsersQuery>(DEFAULT_QUERY); // 当前查询条件
  const [page, setPage] = useState<PageResponse<UserProfile>>({
    current: 1,
    size: 10,
    total: 0,
    pages: 0,
    records: [] // 用户数据列表
  });
  const [loading, setLoading] = useState(true); // 是否正在加载数据
  const [modalOpen, setModalOpen] = useState(false); // 控制弹窗的显示状态
  const [saving, setSaving] = useState(false); // 是否正在保存用户数据
  const [editingUser, setEditingUser] = useState<UserProfile | null>(null); // 当前正在编辑的用户

  // 组件挂载时调用，恢复最近的查询条件并加载用户数据
  useEffect(() => {
    void restoreQueryAndLoad();
  }, []);

  // 异步函数：恢复最近的查询条件并加载用户数据
  async function restoreQueryAndLoad() {
    const recent = await recentUsersQueryStore.get(); // 从 IndexedDB 获取最近的查询条件
    const nextQuery = recent ?? { ...DEFAULT_QUERY }; // 如果没有最近查询条件，则使用默认值
    setQuery(nextQuery); // 设置查询条件
    queryForm.setFieldsValue(nextQuery); // 更新表单的值
    await loadUsers(nextQuery); // 加载用户数据
  }

  // 异步函数：加载用户数据
  async function loadUsers(nextQuery: UsersQuery = query) {
    setLoading(true); // 设置加载状态
    try {
      const data = await pageUsers(token, nextQuery); // 调用后端 API 获取用户数据
      setPage(data); // 更新分页数据
      setQuery(nextQuery); // 更新查询条件
      await recentUsersQueryStore.put({ ...nextQuery, id: 'recent' }); // 保存查询条件到 IndexedDB
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '加载用户列表失败'); // 显示错误提示
    } finally {
      setLoading(false); // 取消加载状态
    }
  }

  // 打开新增用户的弹窗
  function openCreateModal() {
    setEditingUser(null); // 清空当前编辑的用户
    userForm.resetFields(); // 重置表单字段
    userForm.setFieldsValue({ status: 1, role: 'USER' }); // 设置默认值
    setModalOpen(true); // 显示弹窗
  }

  // 打开编辑用户的弹窗
  function openEditModal(user: UserProfile) {
    setEditingUser(user); // 设置当前编辑的用户
    userForm.setFieldsValue({
      nickname: user.nickname,
      status: user.status,
      role: user.role || 'USER'
    }); // 填充表单字段
    setModalOpen(true); // 显示弹窗
  }

  // 异步函数：保存用户数据
  async function handleSave() {
    const values = await userForm.validateFields(); // 校验表单字段
    setSaving(true); // 设置保存状态
    try {
      if (editingUser) {
        // 如果是编辑用户
        await updateUser(token, editingUser.id, {
          nickname: values.nickname,
          status: values.status,
          role: values.role
        }); // 调用更新用户的 API
        messageApi.success('用户信息已更新'); // 显示成功提示
      } else {
        // 如果是新增用户
        await createUser(token, values); // 调用创建用户的 API
        messageApi.success('用户已创建'); // 显示成功提示
      }
      setModalOpen(false); // 关闭弹窗
      await loadUsers(query); // 重新加载用户数据
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '保存用户失败'); // 显示错误提示
    } finally {
      setSaving(false); // 取消保存状态
    }
  }

  // 异步函数：删除用户
  async function handleDelete(user: UserProfile) {
    try {
      await deleteUser(token, user.id); // 调用删除用户的 API
      messageApi.success(`已逻辑删除用户 ${user.username}`); // 显示成功提示
      await loadUsers(query); // 重新加载用户数据
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '删除用户失败'); // 显示错误提示
    }
  }

  // 处理表格分页、排序等变化
  function handleTableChange(pagination: TablePaginationConfig) {
    void loadUsers({
      ...query,
      current: pagination.current ?? 1,
      size: pagination.pageSize ?? 10
    }); // 更新查询条件并加载数据
  }

  // 定义表格的列配置
  const columns: ColumnsType<UserProfile> = [
    { title: 'ID', dataIndex: 'id', width: 80 }, // ID 列
    { title: '用户名', dataIndex: 'username' }, // 用户名列
    { title: '昵称', dataIndex: 'nickname', render: (value) => value || '-' }, // 昵称列
    {
      title: '状态',
      dataIndex: 'status',
      render: (value) => <Tag color={value === 1 ? 'green' : 'orange'}>{value === 1 ? '启用' : '禁用'}</Tag> // 状态列
    },
    { title: '角色', dataIndex: 'role', render: (value) => <Tag color="cyan">{value || 'USER'}</Tag> }, // 角色列
    { title: '最近登录', dataIndex: 'lastLoginAt', render: (value) => value || '-' }, // 最近登录列
    {
      title: '操作',
      width: 180,
      render: (_, record) => (
          <Space>
            <Button size="small" onClick={() => openEditModal(record)}>
              编辑
            </Button>
            <Popconfirm title="确认逻辑删除该用户？" onConfirm={() => void handleDelete(record)}>
              <Button size="small" danger>
                删除
              </Button>
            </Popconfirm>
          </Space>
      ) // 操作列
    }
  ];

  return (
      <div className="page-stack">
        {contextHolder} {/* 消息提示的上下文 */}
        <Card variant="borderless" className="toolbar-card">
          <div className="toolbar-title">
            <div>
              <Typography.Text className="hero-kicker">User CRUD</Typography.Text>
              <Typography.Title level={3}>用户列表</Typography.Title>
            </div>
            <Button type="primary" onClick={openCreateModal}>
              新增用户
            </Button>
          </div>

          <Form
              form={queryForm}
              layout="inline"
              className="query-form"
              initialValues={DEFAULT_QUERY}
              onFinish={(values) => void loadUsers({ ...query, ...values, current: 1 })}
          >
            <Form.Item label="用户名" name="username">
              <Input allowClear placeholder="按用户名模糊搜索" />
            </Form.Item>
            <Form.Item label="状态" name="status">
              <Select
                  allowClear
                  placeholder="全部状态"
                  style={{ width: 140 }}
                  options={[
                    { label: '启用', value: 1 },
                    { label: '禁用', value: 0 }
                  ]}
              />
            </Form.Item>
            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit">
                  查询
                </Button>
                <Button
                    onClick={() => {
                      queryForm.setFieldsValue(DEFAULT_QUERY);
                      void loadUsers({ ...DEFAULT_QUERY });
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
              scroll={{ x: 980 }}
          />
        </Card>

        <Modal
            title={editingUser ? `编辑用户：${editingUser.username}` : '新增用户'}
            open={modalOpen}
            confirmLoading={saving}
            onOk={() => void handleSave()}
            onCancel={() => setModalOpen(false)}
            destroyOnHidden
            forceRender
        >
          <Form form={userForm} layout="vertical">
            {!editingUser && (
                <>
                  <Form.Item
                      label="用户名"
                      name="username"
                      rules={[
                        { required: true, message: '请输入用户名' },
                        { min: 3, message: '用户名至少 3 个字符' }
                      ]}
                  >
                    <Input placeholder="例如 bob" />
                  </Form.Item>
                  <Form.Item
                      label="初始密码"
                      name="password"
                      rules={[
                        { required: true, message: '请输入初始密码' },
                        { min: 6, message: '密码至少 6 个字符' }
                      ]}
                  >
                    <Input.Password placeholder="至少 6 位" />
                  </Form.Item>
                </>
            )}
            <Form.Item label="昵称" name="nickname" rules={[{ max: 64, message: '昵称最多 64 个字符' }]}>
              <Input placeholder="用户展示名" />
            </Form.Item>
            <Form.Item label="状态" name="status" rules={[{ required: true, message: '请选择状态' }]}>
              <Select
                  options={[
                    { label: '启用', value: 1 },
                    { label: '禁用', value: 0 }
                  ]}
              />
            </Form.Item>
            <Form.Item label="角色" name="role" rules={[{ required: true, message: '请选择角色' }]}>
              <Select
                  options={[
                    { label: 'USER', value: 'USER' },
                    { label: 'ADMIN', value: 'ADMIN' }
                  ]}
              />
            </Form.Item>
          </Form>
        </Modal>
      </div>
  );
}