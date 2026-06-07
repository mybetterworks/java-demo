/**
 * 前端类型定义集中放在这里，方便和后端 DTO 一一对照学习。
 * 字段命名保持与 Spring Boot 返回 JSON 一致，避免在早期版本引入额外映射复杂度。
 */

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T | null;
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
  captchaToken?: string;
}

export interface LoginResponse {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  user: UserProfile;
}

export interface CaptchaRequiredData {
  captchaRequired: boolean;
  failureCount: number;
  failureThreshold: number;
  windowSeconds: number;
}

export interface SliderCaptchaChallengeRequest {
  username: string;
}

export interface SliderCaptchaChallengeResponse {
  challengeId: string;
  backgroundImage: string;
  puzzleImage: string;
  trackWidth: number;
  imageWidth: number;
  imageHeight: number;
  puzzleWidth: number;
  puzzleHeight: number;
  puzzleY: number;
  expiresInSeconds: number;
  instruction: string;
}

export interface CaptchaTrackPoint {
  x: number;
  y: number;
  t: number;
}

export interface SliderCaptchaVerifyRequest {
  challengeId: string;
  sliderX: number;
  durationMs: number;
  tracks: CaptchaTrackPoint[];
}

export interface SliderCaptchaVerifyResponse {
  captchaToken: string;
  expiresInSeconds: number;
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

export type TaskScope = 'my' | 'all';
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface TaskItem {
  id: number;
  title: string;
  description?: string | null;
  creatorUserId: number;
  assigneeUserId: number;
  status: TaskStatus;
  priority: TaskPriority;
  dueTime?: string | null;
  deleted: number;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface TasksQuery {
  current: number;
  size: number;
  scope: TaskScope;
  status?: TaskStatus | null;
  assigneeUserId?: number | null;
}

export interface CreateTaskRequest {
  title: string;
  description?: string;
  assigneeUserId?: number;
  priority?: TaskPriority;
  dueTime?: string;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  assigneeUserId?: number;
  priority?: TaskPriority;
  dueTime?: string;
}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}

export interface NotificationItem {
  id: number;
  receiverUserId: number;
  title: string;
  content: string;
  type: string;
  bizType: string;
  bizId?: number | null;
  readStatus: number;
  createdAt?: string | null;
  readAt?: string | null;
}

export interface NotificationsQuery {
  current: number;
  size: number;
  readStatus?: number | null;
}

export interface UnreadCountResponse {
  count: number;
}

export interface MarkAllReadResponse {
  count: number;
}

export type NotificationSocketStatus = 'idle' | 'connecting' | 'connected' | 'reconnecting' | 'closed' | 'error';

export interface NotificationSocketMessage {
  eventId: string;
  type: 'CONNECTION_ACK' | 'NOTIFICATION_CREATED' | 'UNREAD_COUNT_CHANGED' | 'SYSTEM_BROADCAST' | 'PONG' | string;
  receiverUserId?: number | null;
  title?: string | null;
  content?: string | null;
  notification?: NotificationItem | null;
  unreadCount?: number | null;
  onlineSessionCount?: number | null;
  createdAt?: string | null;
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

export interface RecentTasksQuery extends TasksQuery {
  id: 'recent';
}

export interface RecentNotificationsQuery extends NotificationsQuery {
  id: 'recent';
}
