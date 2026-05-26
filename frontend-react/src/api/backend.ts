import { request } from './client';
import type {
  CreateUserRequest,
  LoginRequest,
  LoginResponse,
  PageResponse,
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
