const SESSION_KEY = 'java-demo-vue-auth-session';
const RECENT_QUERY_KEY = 'java-demo-vue-recent-users-query';

function readJson(key) {
  const raw = localStorage.getItem(key);
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw);
  } catch {
    localStorage.removeItem(key);
    return null;
  }
}

/**
 * Vue 端本地存储封装。
 *
 * React 端使用 IndexedDB；Vue 端为了保持 JavaScript 示例轻量，底层使用 localStorage。
 * 但这里刻意暴露 `authSessionStore` 和 `recentUsersQueryStore` 两个 store 对象，
 * 方法名也保持 get/put/clear，与 React 端 storage/indexedDb.ts 的调用方式尽量一致。
 */
export const authSessionStore = {
  async get() {
    return readJson(SESSION_KEY);
  },
  async put(session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  },
  async clear() {
    localStorage.removeItem(SESSION_KEY);
  }
};

export const recentUsersQueryStore = {
  async get() {
    return readJson(RECENT_QUERY_KEY);
  },
  async put(query) {
    localStorage.setItem(RECENT_QUERY_KEY, JSON.stringify(query));
  }
};
