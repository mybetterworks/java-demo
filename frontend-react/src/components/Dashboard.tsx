import { Card, Col, Descriptions, Row, Statistic, Typography } from 'antd';
import type { UserProfile } from '../types';

interface DashboardProps {
  currentUser: UserProfile;
}

/**
 * 首页用于确认“登录态已经恢复并能读取当前用户”。
 * 这比单纯跳转到列表更适合作为学习项目的健康面板。
 */
export function Dashboard({ currentUser }: DashboardProps) {
  return (
    <div className="page-stack">
      <section className="page-hero-card">
        <Typography.Text className="hero-kicker">Session Restored</Typography.Text>
        <Typography.Title level={2}>当前用户已通过 JWT 接入后端</Typography.Title>
        <Typography.Paragraph>
          刷新页面后，前端会从 IndexedDB 读取 token，再调用 `/api/users/me` 校验并恢复当前登录用户。
        </Typography.Paragraph>
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
    </div>
  );
}
