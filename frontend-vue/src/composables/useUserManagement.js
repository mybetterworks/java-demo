import { onMounted, reactive, ref, unref } from 'vue';
import { ElMessage } from 'element-plus';
import { createUser, deleteUser, pageUsers, updateUser } from '../api/backend';
import { recentUsersQueryStore } from '../storage/localStorage';

const DEFAULT_QUERY = {
  current: 1,
  size: 10,
  username: '',
  status: null
};

const DEFAULT_USER_FORM = {
  username: '',
  password: '',
  nickname: '',
  status: 1,
  role: 'USER'
};

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少 3 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' }
  ],
  nickname: [{ max: 64, message: '昵称最多 64 个字符', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
};

/**
 * 用户管理组合式函数。
 *
 * 这里把“页面状态 + 后端调用 + 成功后刷新列表”的逻辑从 SFC 模板中抽离，
 * 让 UserManagementView 更像 Vue 项目中常见的页面组件：模板清晰，业务逻辑由 useXxx 承载。
 */
export function useUserManagement(tokenRef) {
  const userFormRef = ref();
  const loading = ref(true);
  const saving = ref(false);
  const dialogVisible = ref(false);
  const editingUser = ref(null);

  const queryForm = reactive({ ...DEFAULT_QUERY });
  const query = reactive({ ...DEFAULT_QUERY });
  const page = reactive({
    current: 1,
    size: 10,
    total: 0,
    pages: 0,
    records: []
  });

  const userForm = reactive({ ...DEFAULT_USER_FORM });

  onMounted(() => {
    void restoreQueryAndLoad();
  });

  async function restoreQueryAndLoad() {
    const recent = await recentUsersQueryStore.get();
    const nextQuery = recent ?? { ...DEFAULT_QUERY };
    applyQuery(nextQuery);
    await loadUsers(nextQuery);
  }

  async function loadUsers(nextQuery = query) {
    loading.value = true;
    try {
      const data = await pageUsers(unref(tokenRef), nextQuery);
      page.current = data.current;
      page.size = data.size;
      page.total = data.total;
      page.pages = data.pages;
      page.records = data.records;
      applyQuery(nextQuery);
      await recentUsersQueryStore.put({ ...nextQuery, id: 'recent' });
    } catch (error) {
      ElMessage.error(error?.message || '加载用户列表失败');
    } finally {
      loading.value = false;
    }
  }

  function handleQuery() {
    void loadUsers({
      ...query,
      username: queryForm.username,
      status: queryForm.status,
      current: 1
    });
  }

  function handleReset() {
    applyQuery({ ...DEFAULT_QUERY });
    void loadUsers({ ...DEFAULT_QUERY });
  }

  function handleSizeChange(size) {
    void loadUsers({
      ...query,
      current: 1,
      size
    });
  }

  function handleCurrentChange(current) {
    void loadUsers({
      ...query,
      current
    });
  }

  function openCreateDialog() {
    editingUser.value = null;
    Object.assign(userForm, DEFAULT_USER_FORM);
    dialogVisible.value = true;
  }

  function openEditDialog(user) {
    editingUser.value = user;
    Object.assign(userForm, {
      username: user.username,
      password: '',
      nickname: user.nickname || '',
      status: user.status,
      role: user.role || 'USER'
    });
    dialogVisible.value = true;
  }

  async function handleSave() {
    const valid = await userFormRef.value?.validate().catch(() => false);
    if (!valid) {
      return;
    }

    saving.value = true;
    try {
      if (editingUser.value) {
        await updateUser(unref(tokenRef), editingUser.value.id, {
          nickname: userForm.nickname,
          status: userForm.status,
          role: userForm.role
        });
        ElMessage.success('用户信息已更新');
      } else {
        await createUser(unref(tokenRef), {
          username: userForm.username,
          password: userForm.password,
          nickname: userForm.nickname,
          status: userForm.status,
          role: userForm.role
        });
        ElMessage.success('用户已创建');
      }

      dialogVisible.value = false;
      await loadUsers(query);
    } catch (error) {
      ElMessage.error(error?.message || '保存用户失败');
    } finally {
      saving.value = false;
    }
  }

  async function handleDelete(user) {
    try {
      await deleteUser(unref(tokenRef), user.id);
      ElMessage.success(`已逻辑删除用户 ${user.username}`);
      await loadUsers(query);
    } catch (error) {
      ElMessage.error(error?.message || '删除用户失败');
    }
  }

  function applyQuery(nextQuery) {
    Object.assign(query, {
      ...DEFAULT_QUERY,
      ...nextQuery
    });
    Object.assign(queryForm, {
      username: query.username,
      status: query.status
    });
  }

  return {
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
  };
}
