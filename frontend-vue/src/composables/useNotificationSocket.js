import { onBeforeUnmount, ref, unref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { buildNotificationWebSocketUrl } from '../api/realtime';

/**
 * Vue 端实时通知连接管理。
 *
 * 该组合式函数只处理 WebSocket 生命周期：登录态存在时连接，断线后延迟重连，登出时关闭。
 * 通知中心的数据刷新仍由 useNotificationCenter 负责，这样连接层和页面数据层不会缠在一起。
 */
export function useNotificationSocket(tokenRef) {
  const socketStatus = ref('idle');
  const lastSocketMessage = ref(null);
  let socket = null;
  let retryCount = 0;
  let reconnectTimer = null;
  let closedByOwner = false;

  watch(
    () => unref(tokenRef),
    (token) => {
      closeSocket('token_changed');
      lastSocketMessage.value = null;
      if (!token) {
        socketStatus.value = 'idle';
        return;
      }
      closedByOwner = false;
      retryCount = 0;
      connect(token);
    },
    { immediate: true }
  );

  onBeforeUnmount(() => {
    closeSocket('component_unmounted');
  });

  function connect(token) {
    clearReconnectTimer();
    socketStatus.value = retryCount === 0 ? 'connecting' : 'reconnecting';
    socket = new WebSocket(buildNotificationWebSocketUrl(token));

    socket.onopen = () => {
      retryCount = 0;
      socketStatus.value = 'connected';
    };

    socket.onmessage = (event) => {
      try {
        const payload = JSON.parse(event.data);
        lastSocketMessage.value = payload;
        if (payload.type !== 'CONNECTION_ACK' && payload.title) {
          ElMessage.info(payload.title);
        }
      } catch {
        ElMessage.warning('收到无法解析的实时通知消息');
      }
    };

    socket.onerror = () => {
      socketStatus.value = 'error';
    };

    socket.onclose = () => {
      socketStatus.value = 'closed';
      scheduleReconnect(token);
    };
  }

  function scheduleReconnect(token) {
    if (closedByOwner || !token) {
      return;
    }
    socketStatus.value = 'reconnecting';
    clearReconnectTimer();
    reconnectTimer = window.setTimeout(() => {
      retryCount += 1;
      connect(token);
    }, Math.min(3000 + retryCount * 1000, 8000));
  }

  function closeSocket(reason) {
    closedByOwner = true;
    clearReconnectTimer();
    if (socket) {
      socket.close(1000, reason);
      socket = null;
    }
  }

  function clearReconnectTimer() {
    if (reconnectTimer) {
      window.clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
  }

  return {
    socketStatus,
    lastSocketMessage
  };
}
