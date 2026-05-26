import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, Typography, message } from 'antd';
import { loginApi } from '../api/backend';
import type { LoginResponse } from '../types';

interface LoginPageProps {
  onLogin: (response: LoginResponse) => Promise<void>;
}

interface LoginFormValues {
  username: string;
  password: string;
}

/**
 * 登录页只负责收集账号密码和触发登录，不直接操作 IndexedDB。
 * 这样登录成功后的状态保存统一交给 App，后续替换登录方式时页面不会变得臃肿。
 */
export function LoginPage({ onLogin }: LoginPageProps) {
  const [form] = Form.useForm<LoginFormValues>();
  const [messageApi, contextHolder] = message.useMessage();

  async function handleFinish(values: LoginFormValues) {
    try {
      const response = await loginApi(values);
      await onLogin(response);
      messageApi.success('登录成功，欢迎回来');
    } catch (error) {
      messageApi.error(error instanceof Error ? error.message : '登录失败，请稍后再试');
    }
  }

  return (
    <main className="login-scene">
      {contextHolder}
      <section className="login-hero">
        <div className="hero-kicker">v0.3 React Admin</div>
        <Typography.Title level={1}>把微服务学习做成可触摸的系统</Typography.Title>
        <Typography.Paragraph>
          这一版先让浏览器真正接入后端登录和用户管理 API。后面接网关、缓存、消息和观测时，这个前端会成为稳定的验证入口。
        </Typography.Paragraph>
      </section>

      <Card className="login-card" variant="borderless">
        <Typography.Title level={3}>登录管理端</Typography.Title>
        <Typography.Paragraph type="secondary">
          使用后端已有用户登录。登录成功后 token 会保存到 IndexedDB，用于刷新页面后恢复会话。
        </Typography.Paragraph>
        <Form form={form} layout="vertical" onFinish={handleFinish} initialValues={{ username: 'alice' }}>
          <Form.Item
            label="用户名"
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="例如 alice" autoComplete="username" />
          </Form.Item>
          <Form.Item
            label="密码"
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="请输入密码" autoComplete="current-password" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block size="large">
            登录并进入系统
          </Button>
        </Form>
      </Card>
    </main>
  );
}
