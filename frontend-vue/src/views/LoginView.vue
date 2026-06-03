<template>
  <main class="login-scene">
    <section class="login-hero">
      <p class="hero-kicker">v0.5.5 Vue Admin</p>
      <h1>把微服务学习做成可触摸的系统</h1>
      <p>
        当前版本继续强化登录安全：失败次数过多后，需要先完成后端生成的图片拼图验证，
        再携带一次性 token 登录。Vue 与 React 保持同样业务路径，方便对比学习。
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
            :title="captchaToken ? '拼图验证已通过' : '需要拼图验证'"
            :description="captchaDescription"
            show-icon
            :closable="false"
          />
          <div class="captcha-track">
            <div class="captcha-track-copy">
              {{ captchaChallenge?.instruction || '点击获取验证码后，将拼图块拖动到背景缺口位置' }}
            </div>
            <template v-if="captchaChallenge">
              <div
                class="captcha-puzzle-stage"
                :style="{ width: `${captchaChallenge.imageWidth}px`, height: `${captchaChallenge.imageHeight}px` }"
              >
                <img
                  class="captcha-background"
                  :src="captchaChallenge.backgroundImage"
                  alt="验证码背景图"
                  draggable="false"
                >
                <img
                  :class="['captcha-puzzle-piece', { dragging }]"
                  :src="captchaChallenge.puzzleImage"
                  alt="可拖动拼图块"
                  draggable="false"
                  :style="{
                    width: `${captchaChallenge.puzzleWidth}px`,
                    height: `${captchaChallenge.puzzleHeight}px`,
                    transform: `translate3d(${sliderPosition}px, ${captchaChallenge.puzzleY}px, 0)`
                  }"
                  @pointerdown="handlePuzzlePointerDown"
                  @pointermove="handlePuzzlePointerMove"
                  @pointerup="handlePuzzlePointerEnd"
                  @pointercancel="handlePuzzlePointerEnd"
                >
              </div>
<!--              <div class="captcha-progress">-->
<!--                <span>拖动距离：{{ Math.round(sliderPosition) }}px</span>-->
<!--                <span>有效期：{{ captchaChallenge.expiresInSeconds }}s</span>-->
<!--              </div>-->
            </template>
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
              验证拼图
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
const dragging = ref(false);
const dragStartClientX = ref(0);
const dragStartSliderX = ref(0);
const dragStartedAt = ref(0);
const trackPoints = ref([]);

const CAPTCHA_REQUIRED_CODE = 4601;

const form = reactive({
  username: 'alice',
  password: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

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
    resetPuzzleDragState();
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

  const tracks = ensureFinalTrackPoint();
  if (tracks.length < 3) {
    /**
     * 前端只做体验层提示，真正的验证码安全判断仍在后端。
     * 没有拖动轨迹时先提示用户操作，避免直接收到后端 4602 看起来像系统异常。
     */
    ElMessage.warning('请先拖动拼图块到缺口位置');
    return;
  }

  captchaVerifying.value = true;
  try {
    /**
     * v0.5.5 提交的是拼图块最终横坐标、拖动耗时和基础轨迹。
     * Vue 端不保存答案，也不把 captchaToken 写入 localStorage，降低二次验证结果被复用的风险。
     */
    const response = await verifySliderCaptchaApi({
      challengeId: captchaChallenge.value.challengeId,
      sliderX: Math.round(sliderPosition.value),
      durationMs: resolveDragDurationMs(),
      tracks
    });
    captchaToken.value = response.captchaToken;
    ElMessage.success('拼图验证通过，请继续登录');
  } catch (error) {
    captchaToken.value = null;
    captchaChallenge.value = null;
    resetPuzzleDragState();
    ElMessage.error(error?.message || '拼图验证失败，请重新获取验证码');
  } finally {
    captchaVerifying.value = false;
  }
}

function handlePuzzlePointerDown(event) {
  if (!captchaChallenge.value || captchaToken.value) {
    return;
  }

  /**
   * Pointer Events 同时覆盖鼠标和触摸屏。这里记录拖动起点、当前拼图块位置和起始时间，
   * 后续移动时只根据水平位移更新 sliderX，真实答案仍由后端保存和校验。
   */
  event.preventDefault();
  event.currentTarget?.setPointerCapture?.(event.pointerId);
  dragStartClientX.value = event.clientX;
  dragStartSliderX.value = sliderPosition.value;
  dragStartedAt.value = performance.now();
  trackPoints.value = [{ x: Math.round(sliderPosition.value), y: captchaChallenge.value.puzzleY, t: 0 }];
  dragging.value = true;
}

function handlePuzzlePointerMove(event) {
  if (!dragging.value || !captchaChallenge.value || captchaToken.value) {
    return;
  }

  const maxSliderX = captchaChallenge.value.trackWidth - captchaChallenge.value.puzzleWidth;
  const nextX = clamp(dragStartSliderX.value + event.clientX - dragStartClientX.value, 0, maxSliderX);
  sliderPosition.value = nextX;
  appendTrackPoint(
    Math.round(nextX),
    captchaChallenge.value.puzzleY,
    Math.round(performance.now() - dragStartedAt.value)
  );
}

function handlePuzzlePointerEnd(event) {
  if (!dragging.value || !captchaChallenge.value) {
    return;
  }

  appendTrackPoint(
    Math.round(sliderPosition.value),
    captchaChallenge.value.puzzleY,
    Math.round(performance.now() - dragStartedAt.value)
  );
  event.currentTarget?.releasePointerCapture?.(event.pointerId);
  dragging.value = false;
}

function appendTrackPoint(x, y, t) {
  const tracks = trackPoints.value;
  const last = tracks.length > 0 ? tracks[tracks.length - 1] : null;
  // 只记录有意义的轨迹点，避免过于密集的点导致请求体积过大。经验值：位置变化超过 2px 或时间间隔超过 45ms 时记录一个点。
  if (!last || Math.abs(last.x - x) >= 2 || t - last.t >= 45) {
    tracks.push({ x, y, t });
  }
  if (tracks.length > 120) {
    trackPoints.value = tracks.slice(tracks.length - 120);
  }
}

function ensureFinalTrackPoint() {
  if (!captchaChallenge.value) {
    return [];
  }
  appendTrackPoint(Math.round(sliderPosition.value), captchaChallenge.value.puzzleY, resolveDragDurationMs());
  return trackPoints.value;
}

function resolveDragDurationMs() {
  const tracks = trackPoints.value;
  const last = tracks.length > 0 ? tracks[tracks.length - 1] : null;
  if (dragStartedAt.value <= 0) {
    return last?.t ?? 0;
  }
  return Math.max(last?.t ?? 0, Math.round(performance.now() - dragStartedAt.value));
}

function resetCaptchaState() {
  captchaRequired.value = false;
  captchaInfo.value = null;
  captchaChallenge.value = null;
  resetPuzzleDragState();
  captchaToken.value = null;
}

function resetPuzzleDragState() {
  sliderPosition.value = 0;
  dragging.value = false;
  dragStartClientX.value = 0;
  dragStartSliderX.value = 0;
  dragStartedAt.value = 0;
  trackPoints.value = [];
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

function clamp(value, min, max) {
  return Math.min(Math.max(value, min), max);
}
</script>
