/**
 * API 基础地址：
 * - 开发环境默认留空，依赖 Vite proxy 把 /api 转发到 Gateway 8092。
 * - 独立部署时可以通过 VITE_API_BASE_URL 指向完整网关或后端地址。
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

/**
 * Vue 端统一 API 错误类型。
 *
 * 后端已经统一返回 { code, message, data }，但 HTTP 状态码和业务 code 都可能表示失败。
 * 用一个错误类把状态码、业务码和提示信息保留下来，页面层只需要统一弹出 message。
 */
export class ApiError extends Error {
  constructor(message, status, code, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.data = data;
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

  const payload = await readJson(response);
  if (!response.ok) {
    /**
     * v0.5.4 登录滑块验证码会在失败响应 data 中返回 captchaRequired。
     * Vue 页面层需要这个 data 来判断是否展示验证码区域，因此请求层不能丢弃它。
     */
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code, payload?.data);
  }
  if (payload.code !== 0) {
    throw new ApiError(payload.message || '业务处理失败', response.status, payload.code, payload.data);
  }

  return payload.data;
}

/**
 * 下载二进制文件的请求入口。
 *
 * 附件下载需要 Bearer token，但响应不是统一 JSON，而是 Blob 文件流。
 * 因此单独封装，避免普通 request 把文件流当 JSON 解析。
 */
export async function requestBlob(path, options = {}) {
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
    const payload = await tryReadJson(response);
    throw new ApiError(payload?.message || `HTTP ${response.status}`, response.status, payload?.code, payload?.data);
  }

  return response.blob();
}

async function readJson(response) {
  try {
    return await response.json();
  } catch {
    throw new ApiError('后端没有返回合法 JSON，请检查服务是否启动。', response.status);
  }
}

async function tryReadJson(response) {
  try {
    return await response.json();
  } catch {
    return null;
  }
}
