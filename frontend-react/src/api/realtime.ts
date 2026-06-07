/**
 * WebSocket 地址构造工具。
 *
 * HTTP API 可以使用 fetch 的相对路径交给 Vite proxy 或 Gateway；WebSocket 需要把 http/https
 * 转换成 ws/wss，并把 JWT 放到 query 参数中，因为浏览器原生 WebSocket 不能设置 Authorization header。
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export function buildNotificationWebSocketUrl(token: string) {
  const baseUrl = API_BASE_URL
    ? new URL(API_BASE_URL)
    : new URL(window.location.origin);
  baseUrl.protocol = baseUrl.protocol === 'https:' ? 'wss:' : 'ws:';
  baseUrl.pathname = '/ws/notifications';
  baseUrl.search = '';
  baseUrl.searchParams.set('token', token);
  return baseUrl.toString();
}
