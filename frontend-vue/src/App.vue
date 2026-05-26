<template>
  <div v-if="booting" class="boot-screen">
    <el-icon class="boot-icon"><Loading /></el-icon>
    <span>正在从 localStorage 恢复 Vue 登录态...</span>
  </div>

  <LoginView v-else-if="!session || !currentUser" @login="handleLogin" />

  <AppLayout
    v-else
    :current-user="currentUser"
    :active-view="activeView"
    @view-change="activeView = $event"
    @logout="handleLogout"
  >
    <DashboardView v-if="activeView === 'dashboard'" :current-user="currentUser" />
    <UserManagementView v-else :token="session.accessToken" />
  </AppLayout>
</template>

<script setup>
import { onMounted } from 'vue';
import { Loading } from '@element-plus/icons-vue';
import { useAuthSession } from './composables/useAuthSession';
import AppLayout from './layouts/AppLayout.vue';
import DashboardView from './views/DashboardView.vue';
import LoginView from './views/LoginView.vue';
import UserManagementView from './views/UserManagementView.vue';

/**
 * Vue 管理端根组件。
 *
 * App.vue 在 Vue 项目里通常只做应用级接线：
 * 1. 调用组合式函数恢复登录态。
 * 2. 根据登录状态渲染登录页或管理端布局。
 * 3. 用轻量 view state 在两个页面级 view 之间切换。
 *
 * 这样既保持当前功能不变，也避免把业务逻辑堆在根组件里。
 */
const {
  booting,
  session,
  currentUser,
  activeView,
  restoreSession,
  handleLogin,
  handleLogout
} = useAuthSession();

onMounted(() => {
  void restoreSession();
});
</script>
