import type { AuthSession, RecentNotificationsQuery, RecentTasksQuery, RecentUsersQuery } from '../types';

const DB_NAME = 'java_demo_react_admin';
const DB_VERSION = 2;
const AUTH_STORE = 'auth_session';
const QUERY_STORE = 'recent_users_query';
const TASK_QUERY_STORE = 'recent_tasks_query';
const NOTIFICATION_QUERY_STORE = 'recent_notifications_query';

/**
 * v0.3 只使用原生 IndexedDB API，避免为了两个简单缓存再引入第三方库。
 *
 * IndexedDB 的回调式 API 比 Promise 难读一点，所以这里先封装成最小 Promise 工具。
 * 后续如果离线缓存变复杂，可以再把这一层替换成 idb、Dexie 等专业库。
 */
function openDatabase(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open(DB_NAME, DB_VERSION);

    request.onupgradeneeded = () => {
      const db = request.result;
      if (!db.objectStoreNames.contains(AUTH_STORE)) {
        db.createObjectStore(AUTH_STORE, { keyPath: 'id' });
      }
      if (!db.objectStoreNames.contains(QUERY_STORE)) {
        db.createObjectStore(QUERY_STORE, { keyPath: 'id' });
      }
      if (!db.objectStoreNames.contains(TASK_QUERY_STORE)) {
        db.createObjectStore(TASK_QUERY_STORE, { keyPath: 'id' });
      }
      if (!db.objectStoreNames.contains(NOTIFICATION_QUERY_STORE)) {
        db.createObjectStore(NOTIFICATION_QUERY_STORE, { keyPath: 'id' });
      }
    };

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
  });
}

async function runStore<T>(
  storeName: string,
  mode: IDBTransactionMode,
  action: (store: IDBObjectStore) => IDBRequest<T>
): Promise<T> {
  const db = await openDatabase();
  return new Promise((resolve, reject) => {
    const transaction = db.transaction(storeName, mode);
    const store = transaction.objectStore(storeName);
    const request = action(store);

    request.onsuccess = () => resolve(request.result);
    request.onerror = () => reject(request.error);
    transaction.oncomplete = () => db.close();
    transaction.onerror = () => {
      db.close();
      reject(transaction.error);
    };
  });
}

export const authSessionStore = {
  get() {
    return runStore<AuthSession | undefined>(AUTH_STORE, 'readonly', (store) => store.get('current'));
  },
  put(session: AuthSession) {
    return runStore<IDBValidKey>(AUTH_STORE, 'readwrite', (store) => store.put(session));
  },
  clear() {
    return runStore<undefined>(AUTH_STORE, 'readwrite', (store) => store.delete('current'));
  }
};

export const recentUsersQueryStore = {
  get() {
    return runStore<RecentUsersQuery | undefined>(QUERY_STORE, 'readonly', (store) => store.get('recent'));
  },
  put(query: RecentUsersQuery) {
    return runStore<IDBValidKey>(QUERY_STORE, 'readwrite', (store) => store.put(query));
  }
};

export const recentTasksQueryStore = {
  get() {
    return runStore<RecentTasksQuery | undefined>(TASK_QUERY_STORE, 'readonly', (store) => store.get('recent'));
  },
  put(query: RecentTasksQuery) {
    return runStore<IDBValidKey>(TASK_QUERY_STORE, 'readwrite', (store) => store.put(query));
  }
};

export const recentNotificationsQueryStore = {
  get() {
    return runStore<RecentNotificationsQuery | undefined>(NOTIFICATION_QUERY_STORE, 'readonly', (store) => store.get('recent'));
  },
  put(query: RecentNotificationsQuery) {
    return runStore<IDBValidKey>(NOTIFICATION_QUERY_STORE, 'readwrite', (store) => store.put(query));
  }
};
