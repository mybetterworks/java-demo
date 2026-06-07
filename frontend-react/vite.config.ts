import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    /**
     * v0.5 开始采用“前端本地进程 + Gateway 本地进程 + 后端本地进程 + MySQL Docker”的运行方式。
     * 开发时浏览器访问 Vite 端口 5320，所有 /api 请求由 Vite 代理到 Spring Cloud Gateway 8092。
     * 5320 和 8092 都避开了 Windows 当前保留端口段 5112-5311，
     * 以及本机占用范围 7991-8090、8146-8245，
     * 这样外部请求会统一经过网关 JWT 校验，再由网关按路径转发到用户、任务或通知服务。
     */
    proxy: {
      '/api': {
        target: 'http://localhost:8092',
        changeOrigin: true
      },
      '/v3/api-docs': {
        target: 'http://localhost:8092',
        changeOrigin: true
      },
      /**
       * v0.8 WebSocket 实时通知沿用 Gateway 作为统一入口。
       * Vite 开发服务器需要显式开启 ws 代理，否则浏览器连接 /ws/notifications 时会停留在前端端口。
       */
      '/ws': {
        target: 'ws://localhost:8092',
        ws: true,
        changeOrigin: true
      }
    }
  }
});
