import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    /**
     * v0.4 Vue 管理端继续沿用“前端本地进程 + 后端本地进程”的学习模式。
     * 浏览器访问 5174，所有 /api 与 /v3/api-docs 请求由 Vite 转发到后端 8091。
     * 5174 和 4174 都避开了本机已占用端口范围 7991-8090、8146-8245。
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
