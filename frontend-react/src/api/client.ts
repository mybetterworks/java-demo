import type { ApiResponse } from '../types';

/**
 * API 基础地址：
 * - 开发环境默认留空，依赖 Vite proxy 把 /api 转发到 Spring Boot。
 * - 独立部署时可以通过 VITE_API_BASE_URL 指向后端完整地址。
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  readonly status: number;
  readonly code?: number;
  readonly data?: unknown;

  constructor(message: string, status: number, code?: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.data = data;
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

  if (!requestHeaders.has('Content-Type') && body !== undefined && !(body instanceof FormData)) {
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
    /**
     * v0.5.4 登录验证码会在失败响应 data 中返回 captchaRequired。
     * 请求层保留该 data，页面层才能区分“普通账号密码错误”和“需要展示滑块验证码”。
     */
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code, payload?.data);
  }
  if (payload.code !== 0) {
    throw new ApiError(payload.message || '业务处理失败', response.status, payload.code, payload.data);
  }

  return payload.data as T;
}

/**
 * 下载二进制文件的统一入口。
 *
 * 附件下载仍需要 Bearer token，但响应体不是 ApiResponse JSON，而是文件流。
 * 这里单独封装 blob 请求，避免把文件流交给普通 request<T> 的 JSON 解析逻辑。
 */
export async function requestBlob(path: string, options: RequestOptions = {}): Promise<Blob> {
  const { token, headers, body, ...rest } = options;
  const requestHeaders = new Headers(headers);

  if (token) {
    requestHeaders.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers: requestHeaders,
    body
  });

  if (!response.ok) {
    const payload = await tryReadJson<ApiResponse<unknown>>(response);
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code, payload?.data);
  }

  return response.blob();
}

async function readJson<T>(response: Response): Promise<T> {
  try {
    return (await response.json()) as T;
  } catch {
    throw new ApiError('后端没有返回合法 JSON，请检查服务是否启动。', response.status);
  }
}

async function tryReadJson<T>(response: Response): Promise<T | null> {
  try {
    return (await response.json()) as T;
  } catch {
    return null;
  }
}
