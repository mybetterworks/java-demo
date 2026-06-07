/**
 * Vue 端 WebSocket 地址构造工具。
 *
 * WebSocket 不能像普通 fetch 一样设置 Authorization header，因此 v0.8 使用 token 查询参数完成握手鉴权。
 * 如果配置了 VITE_API_BASE_URL，就把它从 http/https 转成 ws/wss；否则使用当前 Vite 页面地址并交给 /ws 代理转发到 Gateway。
 */
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export function buildNotificationWebSocketUrl(token) {
  const baseUrl = API_BASE_URL
    ? new URL(API_BASE_URL)
    : new URL(window.location.origin);
  baseUrl.protocol = baseUrl.protocol === 'https:' ? 'wss:' : 'ws:';
  baseUrl.pathname = '/ws/notifications';
  baseUrl.search = '';
  baseUrl.searchParams.set('token', token);
  return baseUrl.toString();
}
