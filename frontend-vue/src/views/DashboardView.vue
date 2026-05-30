<template>
  <div class="page-stack">
    <section class="page-hero-card">
      <p class="hero-kicker">Session Restored</p>
      <h1>当前用户已通过 JWT 接入后端</h1>
      <p>
        刷新页面后，Vue 端会从 localStorage 读取 token，再调用 `/api/users/me` 校验并恢复当前登录用户。
        页面行为继续保持和 React 管理端一致，但文件位置采用 Vue 常见的 views 目录组织。
        v0.5.3 开始，Vue 管理端也可以通过 Gateway 操作任务服务和通知服务。
      </p>
      <div class="hero-actions">
        <el-button type="primary" @click="$emit('navigate', 'tasks')">进入任务管理</el-button>
        <el-button @click="$emit('navigate', 'notifications')">查看通知中心</el-button>
      </div>
    </section>

    <el-row :gutter="16">
      <el-col :xs="24" :md="8">
        <el-card class="stat-card" shadow="never">
          <el-statistic title="用户 ID" :value="currentUser.id" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="stat-card" shadow="never">
          <el-statistic title="状态" :value="currentUser.status === 1 ? '启用' : '禁用'" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card class="stat-card" shadow="never">
          <el-statistic title="角色" :value="currentUser.role || 'USER'" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="content-card">
      <template #header>当前登录用户</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ currentUser.username }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ currentUser.nickname || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近登录">{{ currentUser.lastLoginAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentUser.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card class="content-card" shadow="never">
          <template #header>任务管理入口</template>
          <p class="toolbar-copy">
            创建任务、分配负责人、切换任务状态，并验证 task-service 到 notification-service 的同步通知链路。
          </p>
          <el-button @click="$emit('navigate', 'tasks')">打开任务管理</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="content-card" shadow="never">
          <template #header>通知中心入口</template>
          <p class="toolbar-copy">
            查询当前用户通知、查看未读数、单条已读和全部已读，为后续 WebSocket 实时推送做页面承接。
          </p>
          <el-button @click="$emit('navigate', 'notifications')">打开通知中心</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineEmits(['navigate']);

defineProps({
  currentUser: {
    type: Object,
    required: true
  }
});
</script>
