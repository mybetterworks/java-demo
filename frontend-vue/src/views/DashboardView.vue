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
        <el-descriptions-item label="头像">
          <div class="avatar-upload-row">
            <span>{{ currentUser.avatarUrl ? '已上传' : '未上传' }}</span>
            <el-upload
              accept="image/png,image/jpeg,image/webp"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
            >
              <el-button size="small" :loading="avatarUploading">上传头像</el-button>
            </el-upload>
          </div>
        </el-descriptions-item>
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
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { uploadMyAvatar } from '../api/backend';

const emit = defineEmits(['navigate', 'current-user-change']);

const props = defineProps({
  token: {
    type: String,
    required: true
  },
  currentUser: {
    type: Object,
    required: true
  }
});

const avatarUploading = ref(false);

async function handleAvatarChange(uploadFile) {
  if (!uploadFile.raw) {
    return;
  }
  avatarUploading.value = true;
  try {
    /**
     * Element Plus Upload 在这里只负责选择文件；真正上传走统一 API 封装。
     * 上传成功后通知 App.vue 更新 currentUser 和 localStorage 会话，保证刷新前后头像一致。
     */
    const updatedUser = await uploadMyAvatar(props.token, uploadFile.raw);
    emit('current-user-change', updatedUser);
    ElMessage.success('头像已上传到 MinIO');
  } catch (error) {
    ElMessage.error(error?.message || '头像上传失败');
  } finally {
    avatarUploading.value = false;
  }
}
</script>
