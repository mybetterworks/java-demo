import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    /**
     * v0.5 开始采用“前端本地进程 + Gateway 本地进程 + 后端本地进程 + MySQL Docker”的运行方式。
     * 开发时浏览器访问 Vite 端口 5173，所有 /api 请求由 Vite 代理到 Spring Cloud Gateway 8092。
     * 8092 是本机占用范围 7991-8090、8146-8245 之外的网关端口，
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
      }
    }
  }
});
