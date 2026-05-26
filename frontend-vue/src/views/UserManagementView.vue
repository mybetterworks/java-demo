<template>
  <div class="page-stack">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-title">
        <div>
          <p class="hero-kicker">User CRUD</p>
          <h2>用户列表</h2>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
      </div>

      <el-form :model="queryForm" inline class="query-form" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" clearable placeholder="按用户名模糊搜索" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" class="status-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
      <el-table :data="page.records" v-loading="loading" row-key="id" border class="users-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="nickname" label="昵称" min-width="150">
          <template #default="{ row }">{{ row.nickname || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.role || 'USER' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="180">
          <template #default="{ row }">{{ row.lastLoginAt || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确认逻辑删除该用户？" @confirm="handleDelete(row)">
                <template #reference>
                  <el-button size="small" type="danger">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
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

    <el-dialog
      v-model="dialogVisible"
      :title="editingUser ? `编辑用户：${editingUser.username}` : '新增用户'"
      width="520px"
      destroy-on-close
    >
      <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-position="top">
        <template v-if="!editingUser">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="userForm.username" placeholder="例如 bob" />
          </el-form-item>
          <el-form-item label="初始密码" prop="password">
            <el-input v-model="userForm.password" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
        </template>

        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="userForm.nickname" placeholder="用户展示名" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" class="dialog-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" class="dialog-select">
            <el-option label="USER" value="USER" />
            <el-option label="ADMIN" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { toRef } from 'vue';
import { useUserManagement } from '../composables/useUserManagement';

const props = defineProps({
  token: {
    type: String,
    required: true
  }
});

/**
 * 页面级组件只保留模板和少量组合式函数接线。
 * 查询、分页、新增、编辑、删除等状态和方法集中放入 composables，
 * 这是 Vue 3 项目中常见的“页面 + useXxx 组合式逻辑”组织方式。
 */
const {
  queryForm,
  page,
  userForm,
  userRules,
  userFormRef,
  loading,
  saving,
  dialogVisible,
  editingUser,
  handleQuery,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  openCreateDialog,
  openEditDialog,
  handleSave,
  handleDelete
} = useUserManagement(toRef(props, 'token'));
</script>
