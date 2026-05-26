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
