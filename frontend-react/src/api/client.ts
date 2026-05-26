import type { ApiResponse } from '../types';

/**
 * API 基础地址：
 * - 开发环境默认留空，依赖 Vite proxy 把 /api 转发到 Spring Boot。
 * - 独立部署时可以通过 VITE_API_BASE_URL 指向后端完整地址。
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  readonly status: number;
  readonly code?: number;

  constructor(message: string, status: number, code?: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

interface RequestOptions extends RequestInit {
  token?: string;
}

/**
 * 统一后端请求入口。
 *
 * 这里集中处理 JSON 序列化、Authorization 头、HTTP 状态码和后端统一响应结构。
 * 后续接入网关、刷新 token、全局审计日志时，只需要优先改这一层。
 */
export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { token, headers, body, ...rest } = options;
  const requestHeaders = new Headers(headers);

  if (!requestHeaders.has('Content-Type') && body !== undefined) {
    requestHeaders.set('Content-Type', 'application/json');
  }
  if (token) {
    requestHeaders.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers: requestHeaders,
    body
  });

  const payload = await readJson<ApiResponse<T>>(response);
  if (!response.ok) {
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code);
  }
  if (payload.code !== 0) {
    throw new ApiError(payload.message || '业务处理失败', response.status, payload.code);
  }

  return payload.data;
}

async function readJson<T>(response: Response): Promise<T> {
  try {
    return (await response.json()) as T;
  } catch {
    throw new ApiError('后端没有返回合法 JSON，请检查服务是否启动。', response.status);
  }
}
