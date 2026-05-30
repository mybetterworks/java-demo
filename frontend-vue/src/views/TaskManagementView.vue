<template>
  <div class="page-stack">
    <el-card shadow="never" class="toolbar-card">
      <div class="toolbar-title">
        <div>
          <p class="hero-kicker">Task Workflow</p>
          <h2>任务管理</h2>
          <p class="toolbar-copy">
            通过 Gateway 调用 task-service，创建任务时会同步触发 notification-service 生成通知。
          </p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新增任务</el-button>
      </div>

      <el-form :model="queryForm" inline class="query-form" @submit.prevent>
        <el-form-item label="范围">
          <el-select v-model="queryForm.scope" class="query-select">
            <el-option label="我的任务" value="my" />
            <el-option label="全部任务" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" clearable placeholder="全部状态" class="query-select">
            <el-option
              v-for="option in taskStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人ID">
          <el-input-number
            v-model="queryForm.assigneeUserId"
            :min="1"
            controls-position="right"
            placeholder="全部任务可用"
            class="query-number"
          />
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
      <el-table :data="page.records" v-loading="loading" row-key="id" border class="tasks-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220">
          <template #default="{ row }">
            <strong>{{ row.title }}</strong>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-select
              :model-value="row.status"
              size="small"
              :loading="statusChangingId === row.id"
              :disabled="statusChangingId === row.id"
              @change="(value) => handleStatusChange(row, value)"
            >
              <el-option
                v-for="option in taskStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="110">
          <template #default="{ row }">
            <el-tag :type="taskPriorityTagType(row.priority)">
              {{ taskPriorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeUserId" label="负责人ID" width="110" />
        <el-table-column prop="dueTime" label="截止时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.dueTime) }}</template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" @click="openDetailDialog(row)">详情</el-button>
              <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
              <el-popconfirm title="确认逻辑删除该任务？" @confirm="handleDelete(row)">
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
      :title="editingTask ? `编辑任务：${editingTask.title}` : '新增任务'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-position="top">
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="例如 学习 v0.5.3 前端联调" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input v-model="taskForm.description" type="textarea" :rows="4" placeholder="记录任务背景、验收方式或学习目标" />
        </el-form-item>
        <el-form-item label="负责人用户 ID">
          <el-input-number
            v-model="taskForm.assigneeUserId"
            :min="1"
            controls-position="right"
            placeholder="不填默认当前用户"
            class="dialog-number"
          />
          <p class="field-help">不填时后端会默认分配给当前登录用户。</p>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="taskForm.priority" class="dialog-select">
            <el-option
              v-for="option in taskPriorityOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-input v-model="taskForm.dueTime" placeholder="例如 2026-05-28T18:00:00，可不填" />
          <p class="field-help">后端 LocalDateTime 使用 2026-05-28T18:00:00 这种格式。</p>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="任务详情" width="560px" destroy-on-close>
      <div v-loading="detailLoading">
        <el-descriptions v-if="detailTask" :column="1" border>
          <el-descriptions-item label="任务ID">{{ detailTask.id }}</el-descriptions-item>
          <el-descriptions-item label="标题">{{ detailTask.title }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ detailTask.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="taskStatusTagType(detailTask.status)">
              {{ taskStatusLabel(detailTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="taskPriorityTagType(detailTask.priority)">
              {{ taskPriorityLabel(detailTask.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建人ID">{{ detailTask.creatorUserId }}</el-descriptions-item>
          <el-descriptions-item label="负责人ID">{{ detailTask.assigneeUserId }}</el-descriptions-item>
          <el-descriptions-item label="截止时间">{{ formatDateTime(detailTask.dueTime) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detailTask.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(detailTask.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { toRef } from 'vue';
import { useTaskManagement } from '../composables/useTaskManagement';

const props = defineProps({
  token: {
    type: String,
    required: true
  }
});

/**
 * 页面模板刻意和 React 任务管理页保持同样的信息架构：
 * 顶部筛选区、表格区、分页区、新增/编辑弹窗、详情弹窗。
 * 差异只体现在 Vue SFC + Element Plus + composable 的实现方式上。
 */
const {
  queryForm,
  page,
  taskForm,
  taskRules,
  taskFormRef,
  loading,
  saving,
  dialogVisible,
  editingTask,
  detailVisible,
  detailLoading,
  detailTask,
  statusChangingId,
  taskStatusOptions,
  taskPriorityOptions,
  handleQuery,
  handleReset,
  handleSizeChange,
  handleCurrentChange,
  openCreateDialog,
  openEditDialog,
  openDetailDialog,
  handleSave,
  handleStatusChange,
  handleDelete,
  taskStatusLabel,
  taskStatusTagType,
  taskPriorityLabel,
  taskPriorityTagType,
  formatDateTime
} = useTaskManagement(toRef(props, 'token'));
</script>
