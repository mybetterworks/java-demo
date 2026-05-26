/**
 * 前端类型定义集中放在这里，方便和后端 DTO 一一对照学习。
 * 字段命名保持与 Spring Boot 返回 JSON 一致，避免在早期版本引入额外映射复杂度。
 */

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  status: number;
  role: string;
  deleted: number;
  lastLoginAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PageResponse<T> {
  current: number;
  size: number;
  total: number;
  pages: number;
  records: T[];
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  user: UserProfile;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  nickname?: string;
  status?: number;
  role?: string;
}

export interface UpdateUserRequest {
  nickname?: string;
  status?: number;
  role?: string;
}

export interface UsersQuery {
  current: number;
  size: number;
  username?: string;
  status?: number | null;
}

export interface AuthSession {
  id: 'current';
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  username: string;
  loginAt: string;
  user: UserProfile;
}

export interface RecentUsersQuery extends UsersQuery {
  id: 'recent';
}
