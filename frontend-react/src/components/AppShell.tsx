import { Layout, Menu, Typography, Button, Space, Avatar, Tag } from 'antd';
import type { MenuProps } from 'antd';
import type { ReactNode } from 'react';
import type { UserProfile } from '../types';

type ViewKey = 'dashboard' | 'users' | 'tasks' | 'notifications';

interface AppShellProps {
  currentUser: UserProfile;
  activeView: ViewKey;
  onViewChange: (view: ViewKey) => void;
  onLogout: () => void;
  children: ReactNode;
}

const menuItems: MenuProps['items'] = [
  { key: 'dashboard', label: '首页概览' },
  { key: 'users', label: '用户管理' },
  { key: 'tasks', label: '任务管理' },
  { key: 'notifications', label: '通知中心' }
];

/**
 * 管理端统一布局。
 *
 * 当前没有引入 react-router，先用轻量 view state 完成页面切换。
 * v0.5.3 页面增加到四个，但仍然可以用 view state 讲清楚“菜单 -> 页面组件”的最小闭环；
 * 等后续页面继续增多或需要浏览器地址栏路由时，再单独引入路由库会更自然。
 */
export function AppShell({ currentUser, activeView, onViewChange, onLogout, children }: AppShellProps) {
  return (
    <Layout className="app-shell">
      <Layout.Sider className="app-sider" breakpoint="lg" collapsedWidth={0}>
        <div className="brand">
          <div className="brand-mark">JD</div>
          <div>
            <div className="brand-title">Java Demo</div>
            <div className="brand-subtitle">React Admin</div>
          </div>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[activeView]}
          items={menuItems}
          onClick={({ key }) => onViewChange(key as ViewKey)}
        />
      </Layout.Sider>

      <Layout className="app-main">
        <Layout.Header className="app-header">
          <div>
            <Typography.Text className="header-eyebrow">Microservice Learning Console</Typography.Text>
            <Typography.Title level={3}>用户、任务与通知练习台</Typography.Title>
          </div>
          <Space>
            <Avatar className="user-avatar">{currentUser.nickname?.slice(0, 1) || currentUser.username.slice(0, 1)}</Avatar>
            <div className="header-user">
              <Typography.Text strong>{currentUser.nickname || currentUser.username}</Typography.Text>
              <Tag color={currentUser.status === 1 ? 'green' : 'orange'}>{currentUser.role || 'USER'}</Tag>
            </div>
            <Button onClick={onLogout}>退出</Button>
          </Space>
        </Layout.Header>
        <Layout.Content className="app-content">{children}</Layout.Content>
      </Layout>
    </Layout>
  );
}
