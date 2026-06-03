import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    /**
     * v0.5 Vue 管理端改为通过 Spring Cloud Gateway 访问后端。
     * 浏览器访问 5321，所有 /api 与 /v3/api-docs 请求由 Vite 转发到网关 8092，
     * 再由网关完成基础 JWT 校验并按路径转发到用户、任务或通知服务。
     * 5321、4174 和 8092 都避开了 Windows 当前保留端口段 5112-5311，
     * 以及本机已占用端口范围 7991-8090、8146-8245。
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
