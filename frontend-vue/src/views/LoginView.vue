<template>
  <main class="login-scene">
    <section class="login-hero">
      <p class="hero-kicker">v0.4 Vue Admin</p>
      <h1>把微服务学习做成可触摸的系统</h1>
      <p>
        这一版用 Vue 实现和 React 管理端一致的登录和用户管理闭环。后面接网关、缓存、消息和观测时，
        React 与 Vue 都可以作为稳定的验证入口。
      </p>
    </section>

    <el-card class="login-card" shadow="never">
      <h2>登录管理端</h2>
      <p class="login-copy">
        使用后端已有用户登录。登录成功后 token 会保存到 localStorage，用于刷新页面后恢复会话。
      </p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleSubmit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="例如 alice" autocomplete="username">
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            autocomplete="current-password"
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-button type="primary" size="large" :loading="loading" class="full-button" @click="handleSubmit">
          登录并进入系统
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import { loginApi } from '../api/backend';

const emit = defineEmits(['login']);
const formRef = ref();
const loading = ref(false);

const form = reactive({
  username: 'alice',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

/**
 * Vue 页面组件负责视图和用户交互，登录成功后的全局会话保存交给组合式函数。
 * 这比把所有逻辑堆在 App.vue 中更符合 Composition API 项目的维护习惯。
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    const response = await loginApi({
      username: form.username,
      password: form.password
    });
    emit('login', response);
    ElMessage.success('登录成功，欢迎回来');
  } catch (error) {
    ElMessage.error(error?.message || '登录失败，请稍后再试');
  } finally {
    loading.value = false;
  }
}
</script>
