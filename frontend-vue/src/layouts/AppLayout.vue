<template>
  <el-container class="app-shell">
    <el-aside class="app-sider" width="240px">
      <div class="brand">
        <div class="brand-mark">JD</div>
        <div>
          <div class="brand-title">Java Demo</div>
          <div class="brand-subtitle">Vue Admin</div>
        </div>
      </div>

      <el-menu class="app-menu" :default-active="activeView" @select="handleSelect">
        <el-menu-item index="dashboard">首页概览</el-menu-item>
        <el-menu-item index="users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="app-main">
      <el-header class="app-header">
        <div>
          <p class="header-eyebrow">Microservice Learning Console</p>
          <h2>用户与认证练习台</h2>
        </div>

        <div class="header-actions">
          <el-avatar class="user-avatar">{{ avatarText }}</el-avatar>
          <div class="header-user">
            <strong>{{ currentUser.nickname || currentUser.username }}</strong>
            <el-tag :type="currentUser.status === 1 ? 'success' : 'warning'">
              {{ currentUser.role || 'USER' }}
            </el-tag>
          </div>
          <el-button @click="$emit('logout')">退出</el-button>
        </div>
      </el-header>

      <el-main class="app-content">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  currentUser: {
    type: Object,
    required: true
  },
  activeView: {
    type: String,
    required: true
  }
});

const emit = defineEmits(['view-change', 'logout']);

/**
 * Vue 项目习惯把页面骨架放在 layouts 目录。
 * 该组件只负责管理端外壳、菜单和头部用户信息，不处理 API、登录态或页面业务。
 */
const avatarText = computed(() => {
  return (props.currentUser.nickname || props.currentUser.username || 'U').slice(0, 1);
});

function handleSelect(key) {
  emit('view-change', key);
}
</script>
