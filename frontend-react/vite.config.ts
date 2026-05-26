import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    /**
     * v0.3 采用“前端本地进程 + 后端本地进程 + MySQL Docker”的运行方式。
     * 开发时浏览器访问 Vite 端口 5173，所有 /api 请求由 Vite 代理到 Spring Boot 8091。
     * 8091 是本机占用范围 7991-8090、8146-8245 之外的后端固定端口，
     * 这样前端代码里可以保持相对路径，暂时不用处理浏览器跨域细节。
     */
    proxy: {
      '/api': {
        target: 'http://localhost:8091',
        changeOrigin: true
      },
      '/v3/api-docs': {
        target: 'http://localhost:8091',
        changeOrigin: true
      }
    }
  }
});
