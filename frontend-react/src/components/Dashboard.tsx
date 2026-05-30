import { Button, Card, Col, Descriptions, Row, Space, Statistic, Typography } from 'antd';
import type { UserProfile } from '../types';

type ViewKey = 'dashboard' | 'users' | 'tasks' | 'notifications';

interface DashboardProps {
  currentUser: UserProfile;
  onNavigate: (view: ViewKey) => void;
}

/**
 * 首页用于确认“登录态已经恢复并能读取当前用户”。
 * 这比单纯跳转到列表更适合作为学习项目的健康面板。
 */
export function Dashboard({ currentUser, onNavigate }: DashboardProps) {
  return (
    <div className="page-stack">
      <section className="page-hero-card">
        <Typography.Text className="hero-kicker">Session Restored</Typography.Text>
        <Typography.Title level={2}>当前用户已通过 JWT 接入后端</Typography.Title>
        <Typography.Paragraph>
          刷新页面后，前端会从 IndexedDB 读取 token，再调用 `/api/users/me` 校验并恢复当前登录用户。
          v0.5.3 开始，React 管理端也可以通过 Gateway 操作任务服务和通知服务。
        </Typography.Paragraph>
        <Space wrap>
          <Button type="primary" onClick={() => onNavigate('tasks')}>
            进入任务管理
          </Button>
          <Button onClick={() => onNavigate('notifications')}>查看通知中心</Button>
        </Space>
      </section>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <Card variant="borderless">
            <Statistic title="用户 ID" value={currentUser.id} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card variant="borderless">
            <Statistic title="状态" value={currentUser.status === 1 ? '启用' : '禁用'} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card variant="borderless">
            <Statistic title="角色" value={currentUser.role || 'USER'} />
          </Card>
        </Col>
      </Row>

      <Card variant="borderless" title="当前登录用户">
        <Descriptions column={{ xs: 1, md: 2 }} bordered>
          <Descriptions.Item label="用户名">{currentUser.username}</Descriptions.Item>
          <Descriptions.Item label="昵称">{currentUser.nickname || '-'}</Descriptions.Item>
          <Descriptions.Item label="最近登录">{currentUser.lastLoginAt || '-'}</Descriptions.Item>
          <Descriptions.Item label="创建时间">{currentUser.createdAt || '-'}</Descriptions.Item>
        </Descriptions>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <Card variant="borderless" title="任务管理入口">
            <Typography.Paragraph type="secondary">
              创建任务、分配负责人、切换任务状态，并验证 task-service 到 notification-service 的同步通知链路。
            </Typography.Paragraph>
            <Button onClick={() => onNavigate('tasks')}>打开任务管理</Button>
          </Card>
        </Col>
        <Col xs={24} md={12}>
          <Card variant="borderless" title="通知中心入口">
            <Typography.Paragraph type="secondary">
              查询当前用户通知、查看未读数、单条已读和全部已读，为后续 WebSocket 实时推送做页面承接。
            </Typography.Paragraph>
            <Button onClick={() => onNavigate('notifications')}>打开通知中心</Button>
          </Card>
        </Col>
      </Row>
    </div>
  );
}
