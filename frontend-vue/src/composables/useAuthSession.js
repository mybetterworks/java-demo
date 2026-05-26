import { ref } from 'vue';
import { fetchCurrentUser } from '../api/backend';
import { authSessionStore } from '../storage/localStorage';

/**
 * 登录会话组合式函数。
 *
 * Vue 3 项目通常会把可复用状态逻辑抽到 composables 中。
 * 当前项目还没有引入 Pinia，因此先用一个轻量 useAuthSession 管理启动恢复、
 * 登录成功保存、退出登录和页面切换，既保留现有功能，也能体现 Vue 的组合式开发特点。
 */
export function useAuthSession() {
  const booting = ref(true);
  const session = ref(null);
  const currentUser = ref(null);
  const activeView = ref('dashboard');

  async function restoreSession() {
    try {
      const saved = await authSessionStore.get();
      if (saved?.accessToken) {
        const user = await fetchCurrentUser(saved.accessToken);
        const refreshed = { ...saved, user };
        session.value = refreshed;
        currentUser.value = user;
        await authSessionStore.put(refreshed);
      }
    } catch {
      await authSessionStore.clear();
    } finally {
      booting.value = false;
    }
  }

  async function handleLogin(response) {
    const nextSession = {
      id: 'current',
      tokenType: response.tokenType,
      accessToken: response.accessToken,
      expiresInSeconds: response.expiresInSeconds,
      username: response.user.username,
      loginAt: new Date().toISOString(),
      user: response.user
    };

    await authSessionStore.put(nextSession);
    session.value = nextSession;
    currentUser.value = response.user;
    activeView.value = 'dashboard';
  }

  async function handleLogout() {
    await authSessionStore.clear();
    session.value = null;
    currentUser.value = null;
    activeView.value = 'dashboard';
  }

  return {
    booting,
    session,
    currentUser,
    activeView,
    restoreSession,
    handleLogin,
    handleLogout
  };
}
