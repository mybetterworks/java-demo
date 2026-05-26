import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import './styles.css';

/**
 * Vue 管理端入口。
 *
 * v0.4 的目标是用 JavaScript 版本的 Vue 3 复刻 React 管理端的核心业务闭环：
 * 登录、查看当前用户、用户分页、新增用户和编辑用户。
 * 这里统一挂载 Element Plus，避免每个页面重复引入组件。
 */
createApp(App).use(ElementPlus).mount('#app');
