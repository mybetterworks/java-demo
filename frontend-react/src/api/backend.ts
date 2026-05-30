import { request } from './client';
import type {
  CreateTaskRequest,
  CreateUserRequest,
  LoginRequest,
  LoginResponse,
  MarkAllReadResponse,
  NotificationItem,
  NotificationsQuery,
  PageResponse,
  TaskItem,
  TasksQuery,
  TaskStatus,
  UnreadCountResponse,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
  UpdateUserRequest,
  UserProfile,
  UsersQuery
} from '../types';

export function loginApi(data: LoginRequest) {
  return request<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export function fetchCurrentUser(token: string) {
  return request<UserProfile>('/api/users/me', { token });
}

export function pageUsers(token: string, query: UsersQuery) {
  const params = new URLSearchParams({
    current: String(query.current),
    size: String(query.size)
  });
  if (query.username?.trim()) {
    params.set('username', query.username.trim());
  }
  if (query.status !== undefined && query.status !== null) {
    params.set('status', String(query.status));
  }

  return request<PageResponse<UserProfile>>(`/api/users?${params.toString()}`, { token });
}

export function createUser(token: string, data: CreateUserRequest) {
  return request<UserProfile>('/api/users', {
    method: 'POST',
    token,
    body: JSON.stringify(data)
  });
}

export function updateUser(token: string, id: number, data: UpdateUserRequest) {
  return request<UserProfile>(`/api/users/${id}`, {
    method: 'PUT',
    token,
    body: JSON.stringify(data)
  });
}

export function deleteUser(token: string, id: number) {
  return request<void>(`/api/users/${id}`, {
    method: 'DELETE',
    token
  });
}

export function pageTasks(token: string, query: TasksQuery) {
  const params = new URLSearchParams({
    current: String(query.current),
    size: String(query.size)
  });
  if (query.status) {
    params.set('status', query.status);
  }
  if (query.scope === 'all' && query.assigneeUserId) {
    params.set('assigneeUserId', String(query.assigneeUserId));
  }

  /**
   * 任务列表有两个入口：
   * - /api/tasks/my：学习“当前用户上下文”如何在后端解析 JWT 后参与查询。
   * - /api/tasks：学习后台管理视角下的通用分页查询。
   * 页面层只关心 scope，真正的路径差异收敛在 API 封装里，React/Vue 两端也保持同样语义。
   */
  const path = query.scope === 'my' ? '/api/tasks/my' : '/api/tasks';
  return request<PageResponse<TaskItem>>(`${path}?${params.toString()}`, { token });
}

export function getTask(token: string, id: number) {
  return request<TaskItem>(`/api/tasks/${id}`, { token });
}

export function createTask(token: string, data: CreateTaskRequest) {
  return request<TaskItem>('/api/tasks', {
    method: 'POST',
    token,
    body: JSON.stringify(data)
  });
}

export function updateTask(token: string, id: number, data: UpdateTaskRequest) {
  return request<TaskItem>(`/api/tasks/${id}`, {
    method: 'PUT',
    token,
    body: JSON.stringify(data)
  });
}

export function updateTaskStatus(token: string, id: number, status: TaskStatus) {
  const payload: UpdateTaskStatusRequest = { status };
  return request<TaskItem>(`/api/tasks/${id}/status`, {
    method: 'PUT',
    token,
    body: JSON.stringify(payload)
  });
}

export function deleteTask(token: string, id: number) {
  return request<void>(`/api/tasks/${id}`, {
    method: 'DELETE',
    token
  });
}

export function pageNotifications(token: string, query: NotificationsQuery) {
  const params = new URLSearchParams({
    current: String(query.current),
    size: String(query.size)
  });
  if (query.readStatus !== undefined && query.readStatus !== null) {
    params.set('readStatus', String(query.readStatus));
  }

  return request<PageResponse<NotificationItem>>(`/api/notifications/my?${params.toString()}`, { token });
}

export function fetchUnreadCount(token: string) {
  return request<UnreadCountResponse>('/api/notifications/my/unread-count', { token });
}

export function markNotificationRead(token: string, id: number) {
  return request<NotificationItem>(`/api/notifications/${id}/read`, {
    method: 'PUT',
    token
  });
}

export function markAllNotificationsRead(token: string) {
  return request<MarkAllReadResponse>('/api/notifications/read-all', {
    method: 'PUT',
    token
  });
}
