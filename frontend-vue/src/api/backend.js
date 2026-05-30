import { request } from './client';

export function loginApi(data) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(data)
  });
}

export function fetchCurrentUser(token) {
  return request('/api/users/me', { token });
}

export function pageUsers(token, query) {
  const params = new URLSearchParams({
    current: String(query.current),
    size: String(query.size)
  });
  if (query.username?.trim()) {
    params.set('username', query.username.trim());
  }
  if (query.status !== undefined && query.status !== null && query.status !== '') {
    params.set('status', String(query.status));
  }

  return request(`/api/users?${params.toString()}`, { token });
}

export function createUser(token, data) {
  return request('/api/users', {
    method: 'POST',
    token,
    body: JSON.stringify(data)
  });
}

export function updateUser(token, id, data) {
  return request(`/api/users/${id}`, {
    method: 'PUT',
    token,
    body: JSON.stringify(data)
  });
}

export function deleteUser(token, id) {
  return request(`/api/users/${id}`, {
    method: 'DELETE',
    token
  });
}

export function pageTasks(token, query) {
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
   * Vue 端和 React 端保持同样的业务语义：
   * scope=my 调用“我的任务”，scope=all 调用后台分页列表。
   * 路径差异集中在 API 封装里，页面只需要处理筛选条件和表格渲染。
   */
  const path = query.scope === 'my' ? '/api/tasks/my' : '/api/tasks';
  return request(`${path}?${params.toString()}`, { token });
}

export function getTask(token, id) {
  return request(`/api/tasks/${id}`, { token });
}

export function createTask(token, data) {
  return request('/api/tasks', {
    method: 'POST',
    token,
    body: JSON.stringify(data)
  });
}

export function updateTask(token, id, data) {
  return request(`/api/tasks/${id}`, {
    method: 'PUT',
    token,
    body: JSON.stringify(data)
  });
}

export function updateTaskStatus(token, id, status) {
  return request(`/api/tasks/${id}/status`, {
    method: 'PUT',
    token,
    body: JSON.stringify({ status })
  });
}

export function deleteTask(token, id) {
  return request(`/api/tasks/${id}`, {
    method: 'DELETE',
    token
  });
}

export function pageNotifications(token, query) {
  const params = new URLSearchParams({
    current: String(query.current),
    size: String(query.size)
  });
  if (query.readStatus !== undefined && query.readStatus !== null && query.readStatus !== '') {
    params.set('readStatus', String(query.readStatus));
  }

  return request(`/api/notifications/my?${params.toString()}`, { token });
}

export function fetchUnreadCount(token) {
  return request('/api/notifications/my/unread-count', { token });
}

export function markNotificationRead(token, id) {
  return request(`/api/notifications/${id}/read`, {
    method: 'PUT',
    token
  });
}

export function markAllNotificationsRead(token) {
  return request('/api/notifications/read-all', {
    method: 'PUT',
    token
  });
}
