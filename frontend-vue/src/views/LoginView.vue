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

        <div v-if="captchaRequired" class="captcha-panel">
          <el-alert
            :type="captchaToken ? 'success' : 'warning'"
            :title="captchaToken ? '滑块验证已通过' : '需要滑块验证'"
            :description="captchaDescription"
            show-icon
            :closable="false"
          />
          <div class="captcha-track">
            <div class="captcha-track-copy">
              {{ captchaChallenge?.instruction || '点击获取验证码后，将滑块拖动到最右侧' }}
            </div>
            <el-slider
              v-model="sliderPosition"
              :min="0"
              :max="sliderMax"
              :disabled="!captchaChallenge || Boolean(captchaToken)"
            />
          </div>
          <div class="captcha-actions">
            <el-button :loading="captchaLoading" @click="loadCaptchaChallenge(form.username)">
              {{ captchaChallenge ? '重新获取验证码' : '获取验证码' }}
            </el-button>
            <el-button
              type="primary"
              plain
              :disabled="!captchaChallenge || Boolean(captchaToken)"
              :loading="captchaVerifying"
              @click="handleVerifyCaptcha"
            >
              验证滑块
            </el-button>
          </div>
        </div>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          :disabled="!captchaReady"
          class="full-button"
          @click="handleSubmit"
        >
          登录并进入系统
        </el-button>
      </el-form>
    </el-card>
  </main>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import { createSliderCaptchaApi, loginApi, verifySliderCaptchaApi } from '../api/backend';
import { ApiError } from '../api/client';

const emit = defineEmits(['login']);
const formRef = ref();
const loading = ref(false);
const captchaRequired = ref(false);
const captchaInfo = ref(null);
const captchaChallenge = ref(null);
const sliderPosition = ref(0);
const captchaToken = ref(null);
const captchaLoading = ref(false);
const captchaVerifying = ref(false);

const CAPTCHA_REQUIRED_CODE = 4601;

const form = reactive({
  username: 'alice',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const sliderMax = computed(() => {
  if (!captchaChallenge.value) {
    return 100;
  }
  return captchaChallenge.value.trackWidth - captchaChallenge.value.puzzleWidth;
});

const captchaReady = computed(() => !captchaRequired.value || Boolean(captchaToken.value));

const captchaDescription = computed(() => {
  if (!captchaInfo.value) {
    return '登录失败次数较多，请完成验证后继续登录。';
  }
  return `当前 5 分钟内失败 ${captchaInfo.value.failureCount}/${captchaInfo.value.failureThreshold} 次，请完成验证后继续登录。`;
});

watch(
  () => form.username,
  () => {
    /**
     * 验证码 challenge/token 与 username + clientIp 绑定。
     * 用户名变化后立刻丢弃旧验证码状态，避免旧 token 被误用于另一个账号。
     */
    resetCaptchaState();
  }
);

/**
 * Vue 页面组件负责视图、登录风险验证码和用户交互，登录成功后的全局会话保存交给组合式函数。
 * 这比把所有逻辑堆在 App.vue 中更符合 Composition API 项目的维护习惯。
 */
async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  if (captchaRequired.value && !captchaToken.value) {
    /**
     * 高风险状态下不直接重复提交账号密码，而是先让用户完成滑块验证。
     * 这样 Vue 与 React 的交互路径保持一致，也减少后端无效登录请求。
     */
    await loadCaptchaChallenge(form.username);
    ElMessage.warning('请先完成滑块验证，再提交登录');
    return;
  }

  loading.value = true;
  try {
    const response = await loginApi({
      username: form.username,
      password: form.password,
      captchaToken: captchaToken.value || undefined
    });
    emit('login', response);
    resetCaptchaState();
    ElMessage.success('登录成功，欢迎回来');
  } catch (error) {
    if (isCaptchaRequiredError(error)) {
      /**
       * 后端返回 code=4601 时，说明当前登录主体已经进入验证码流程。
       * 页面立即拉取 challenge，用户就能在当前表单内继续完成登录。
       */
      captchaRequired.value = true;
      captchaInfo.value = readCaptchaInfo(error);
      captchaToken.value = null;
      await loadCaptchaChallenge(form.username);
      ElMessage.warning(error.message);
    } else {
      ElMessage.error(error?.message || '登录失败，请稍后再试');
    }
  } finally {
    loading.value = false;
  }
}

async function loadCaptchaChallenge(username) {
  if (!username?.trim()) {
    return;
  }

  captchaLoading.value = true;
  try {
    captchaChallenge.value = await createSliderCaptchaApi({ username });
    sliderPosition.value = 0;
    captchaToken.value = null;
  } catch (error) {
    ElMessage.error(error?.message || '验证码生成失败，请稍后重试');
  } finally {
    captchaLoading.value = false;
  }
}

async function handleVerifyCaptcha() {
  if (!captchaChallenge.value) {
    await loadCaptchaChallenge(form.username);
    return;
  }

  captchaVerifying.value = true;
  try {
    /**
     * 当前 MVP 使用“滑动到终点”的轻量滑块。后端仍校验 challengeId、位置和 TTL，
     * 成功后返回短 TTL captchaToken；Vue 端只保存在内存中，不写入 localStorage。
     */
    const response = await verifySliderCaptchaApi({
      challengeId: captchaChallenge.value.challengeId,
      sliderPosition: sliderPosition.value
    });
    captchaToken.value = response.captchaToken;
    ElMessage.success('滑块验证通过，请继续登录');
  } catch (error) {
    captchaToken.value = null;
    captchaChallenge.value = null;
    sliderPosition.value = 0;
    ElMessage.error(error?.message || '滑块验证失败，请重新获取验证码');
  } finally {
    captchaVerifying.value = false;
  }
}

function resetCaptchaState() {
  captchaRequired.value = false;
  captchaInfo.value = null;
  captchaChallenge.value = null;
  sliderPosition.value = 0;
  captchaToken.value = null;
}

function isCaptchaRequiredError(error) {
  return error instanceof ApiError
    && (error.code === CAPTCHA_REQUIRED_CODE || readCaptchaInfo(error)?.captchaRequired === true);
}

function readCaptchaInfo(error) {
  if (!(error instanceof ApiError) || typeof error.data !== 'object' || error.data === null) {
    return null;
  }
  return error.data;
}
</script>
