import { Spin } from 'antd'; // 从 Ant Design 引入 Spin 组件，用于加载状态的展示
import { useEffect, useState } from 'react'; // React 的两个 Hook，用于管理组件状态和副作用
import { fetchCurrentUser } from './api/backend'; // 引入后端 API，用于获取当前用户信息
import { AppShell } from './components/AppShell'; // 应用的布局组件，包含侧边菜单、顶部用户信息等
import { Dashboard } from './components/Dashboard'; // 首页概览组件，展示当前登录用户信息
import { LoginPage } from './components/LoginPage'; // 登录页面组件，处理用户登录
import { NotificationCenter } from './components/NotificationCenter'; // 通知中心页面组件，处理通知查询和已读操作
import { TaskManagement } from './components/TaskManagement'; // 任务管理页面组件，处理任务查询、创建、编辑和状态流转
import { UserManagement } from './components/UserManagement'; // 用户管理页面组件，处理用户的增删改查
import { authSessionStore } from './storage/indexedDb'; // IndexedDB 封装，用于存储登录会话
import type { AuthSession, LoginResponse, UserProfile } from './types'; // 引入类型定义，确保代码类型安全

// 定义页面视图的类型。v0.5.3 新增任务管理和通知中心，用同一套 view state 保持页面切换简单可读。
type ViewKey = 'dashboard' | 'users' | 'tasks' | 'notifications';

/**
 * React 管理端根组件。
 *
 * v0.3 的核心学习点是：浏览器登录 -> IndexedDB 保存 token -> 刷新后恢复登录态 ->
 * 携带 Bearer token 调用用户管理 API。这里把这条主流程收敛在根组件中，便于复盘。
 */
export function RootApp() {
  // 定义组件的状态
  const [booting, setBooting] = useState(true); // 是否正在初始化应用
  const [session, setSession] = useState<AuthSession | null>(null); // 当前登录会话
  const [currentUser, setCurrentUser] = useState<UserProfile | null>(null); // 当前登录用户信息
  const [activeView, setActiveView] = useState<ViewKey>('dashboard'); // 当前激活的页面视图

  // 组件挂载时调用，尝试恢复登录态
  useEffect(() => {
    void restoreSession(); // 调用恢复登录态的异步函数
  }, []);

  // 异步函数：从 IndexedDB 恢复登录态
  async function restoreSession() {
    try {
      const saved = await authSessionStore.get(); // 从 IndexedDB 获取保存的会话
      if (saved?.accessToken) {
        // 如果存在有效的 accessToken
        const user = await fetchCurrentUser(saved.accessToken); // 调用 API 获取当前用户信息
        const refreshed = { ...saved, user }; // 更新会话信息
        setSession(refreshed); // 设置会话状态
        setCurrentUser(user); // 设置当前用户信息
        await authSessionStore.put(refreshed); // 将更新后的会话保存到 IndexedDB
      }
    } catch {
      // 如果恢复失败（如 token 过期、后端重启等）
      await authSessionStore.clear(); // 清理本地会话
    } finally {
      setBooting(false); // 初始化完成
    }
  }

  // 处理登录逻辑
  async function handleLogin(response: LoginResponse) {
    const nextSession: AuthSession = {
      id: 'current', // 当前会话的 ID
      tokenType: response.tokenType, // token 类型
      accessToken: response.accessToken, // accessToken
      expiresInSeconds: response.expiresInSeconds, // token 过期时间
      username: response.user.username, // 用户名
      loginAt: new Date().toISOString(), // 登录时间
      user: response.user // 用户信息
    };
    await authSessionStore.put(nextSession); // 将会话保存到 IndexedDB
    setSession(nextSession); // 设置会话状态
    setCurrentUser(response.user); // 设置当前用户信息
    setActiveView('dashboard'); // 切换到首页视图
  }

  // 处理登出逻辑
  async function handleLogout() {
    await authSessionStore.clear(); // 清理本地会话
    setSession(null); // 清空会话状态
    setCurrentUser(null); // 清空当前用户信息
    setActiveView('dashboard'); // 切换到首页视图
  }

  // 如果应用正在初始化，显示加载界面
  if (booting) {
    return (
        <div className="boot-screen">
          <Spin size="large" /> {/* 显示加载动画 */}
          <span>正在从 IndexedDB 恢复登录态...</span> {/* 显示加载提示 */}
        </div>
    );
  }

  // 如果未登录或当前用户信息为空，显示登录页面
  if (!session || !currentUser) {
    return <LoginPage onLogin={handleLogin} />; // 登录成功后调用 handleLogin
  }

  // 如果已登录，显示应用主界面
  return (
      <AppShell
          currentUser={currentUser} // 当前用户信息
          activeView={activeView} // 当前激活的页面视图
          onViewChange={setActiveView} // 切换页面视图的回调
          onLogout={() => void handleLogout()} // 处理登出的回调
      >
        {/* 根据当前视图渲染不同的页面组件。暂不引入路由库，便于学习阶段观察最小页面切换机制。 */}
        {activeView === 'dashboard' && <Dashboard currentUser={currentUser} onNavigate={setActiveView} />}
        {activeView === 'users' && <UserManagement token={session.accessToken} />}
        {activeView === 'tasks' && <TaskManagement token={session.accessToken} />}
        {activeView === 'notifications' && <NotificationCenter token={session.accessToken} />}
      </AppShell>
  );
}
