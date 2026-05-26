/**
 * API 基础地址：
 * - 开发环境默认留空，依赖 Vite proxy 把 /api 转发到 Spring Boot 8091。
 * - 独立部署时可以通过 VITE_API_BASE_URL 指向完整后端地址。
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

/**
 * Vue 端统一 API 错误类型。
 *
 * 后端已经统一返回 { code, message, data }，但 HTTP 状态码和业务 code 都可能表示失败。
 * 用一个错误类把状态码、业务码和提示信息保留下来，页面层只需要统一弹出 message。
 */
export class ApiError extends Error {
  constructor(message, status, code) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

/**
 * 统一后端请求入口。
 *
 * 这一层负责补充 JSON 请求头、Bearer Token、解析统一响应和转换错误。
 * 后续接入 Gateway、刷新 token 或审计日志时，Vue 端优先改这一处即可。
 */
export async function request(path, options = {}) {
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

  const payload = await readJson(response);
  if (!response.ok) {
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code);
  }
  if (payload.code !== 0) {
    throw new ApiError(payload.message || '业务处理失败', response.status, payload.code);
  }

  return payload.data;
}

async function readJson(response) {
  try {
    return await response.json();
  } catch {
    throw new ApiError('后端没有返回合法 JSON，请检查服务是否启动。', response.status);
  }
}
