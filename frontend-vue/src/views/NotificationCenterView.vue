<template>
  <div class="page-stack">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-title">
        <div>
          <p class="hero-kicker">Notification Inbox</p>
          <h2>通知中心</h2>
          <p class="toolbar-copy">
            当前未读 <strong>{{ unreadCount }}</strong> 条。任务创建、负责人变更和状态流转都会生成通知。
          </p>
        </div>
        <div class="form-actions">
          <el-button @click="loadNotifications()">刷新</el-button>
          <el-popconfirm title="确认将当前用户全部通知标记为已读？" @confirm="handleMarkAllRead">
            <template #reference>
              <el-button type="primary" :loading="readingAll">全部已读</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <el-form :model="queryForm" inline class="query-form" @submit.prevent>
        <el-form-item label="已读状态">
          <el-select v-model="queryForm.readStatus" clearable placeholder="全部通知" class="query-select">
            <el-option
              v-for="option in readStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <div class="form-actions">
            <el-button type="primary" @click="handleQuery">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="content-card">
      <el-table :data="page.records" v-loading="loading" row-key="id" border class="notifications-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="{ row }">
            <div class="notification-cell">
              <strong :class="{ unread: row.readStatus === 0 }">{{ row.title }}</strong>
              <span>{{ row.content }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="notificationTypeTagType(row.type)">
              {{ notificationTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bizType" label="业务" width="150">
          <template #default="{ row }">
            {{ row.bizType || 'GENERAL' }}{{ row.bizId ? ` #${row.bizId}` : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="readStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.readStatus === 0 ? 'warning' : 'info'">
              {{ row.readStatus === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="readAt" label="已读时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.readAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              :disabled="row.readStatus === 1"
              :loading="readingId === row.id"
              @click="handleMarkRead(row)"
            >
              标记已读
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          :current-page="page.current"
          :page-size="page.size"
          :page-sizes="[5, 10, 20]"
          :total="page.total"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { toRef } from 'vue';
import { useNotificationCenter } from '../composables/useNotificationCenter';

const props = defineProps({
  token: {
    type: String,
    required: true
  }
});

/**
 * 通知中心的页面结构与 React 端保持一致：
 * 顶部展示未读数和全量操作，下面用筛选表单、表格和分页承接 notification-service。
 * Vue 端只把交互实现换成 Element Plus，业务字段和操作按钮不做语义漂移。
 */
const {
  queryForm,
  page,
  unreadCount,
  loading,
  readingId,
  readingAll,
  readStatusOptions,
  handleQuery,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  handleMarkRead,
  handleMarkAllRead,
  loadNotifications,
  notificationTypeLabel,
  notificationTypeTagType,
  formatDateTime
} = useNotificationCenter(toRef(props, 'token'));
</script>
