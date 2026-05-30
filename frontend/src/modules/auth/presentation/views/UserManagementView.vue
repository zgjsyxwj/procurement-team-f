<template>
  <div class="p-6 max-w-7xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-semibold text-gray-800">用户管理</h2>
      <a-button type="primary" @click="showCreateModal = true">
        创建用户
      </a-button>
    </div>

    <!-- 筛选与搜索 -->
    <div class="mb-4 p-4 bg-white rounded-xl shadow-sm">
      <a-space>
        <a-select
          v-model:value="filters.role"
          placeholder="按角色筛选"
          allow-clear
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option value="ADMIN">管理员</a-select-option>
          <a-select-option value="BUYER">采购员</a-select-option>
          <a-select-option value="BUSINESS_USER">业务人员</a-select-option>
        </a-select>

        <a-select
          v-model:value="filters.status"
          placeholder="按状态筛选"
          allow-clear
          style="width: 120px"
          @change="handleSearch"
        >
          <a-select-option value="ACTIVE">启用</a-select-option>
          <a-select-option value="DISABLED">停用</a-select-option>
        </a-select>

        <a-input-search
          v-model:value="filters.keyword"
          placeholder="搜索姓名/手机号"
          style="width: 200px"
          @search="handleSearch"
        />
      </a-space>
    </div>

    <!-- 用户列表表格 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="userList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'role'">
            <a-tag :color="getRoleColor(record.role)">
              {{ getRoleLabel(record.role) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'red'">
              {{ record.status === 'ACTIVE' ? '启用' : '停用' }}
            </a-tag>
          </template>

          <template v-if="column.key === 'actions'">
            <a-space>
              <a-button type="link" size="small" @click="handleChangeRole(record)">
                修改角色
              </a-button>
              <a-button
                type="link"
                size="small"
                :danger="record.status === 'ACTIVE'"
                @click="handleToggleStatus(record)"
              >
                {{ record.status === 'ACTIVE' ? '停用' : '启用' }}
              </a-button>
              <a-button type="link" size="small" @click="handleResetPassword(record)">
                重置密码
              </a-button>
              <a-button type="link" size="small" @click="handleUnlock(record)">
                解锁
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 创建用户弹窗 -->
    <a-modal
      v-model:open="showCreateModal"
      title="创建用户"
      :confirm-loading="createLoading"
      @ok="handleCreateUser"
      @cancel="resetCreateForm"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="姓名" required>
          <a-input v-model:value="createForm.name" placeholder="请输入姓名" />
        </a-form-item>
        <a-form-item label="手机号" required>
          <a-input v-model:value="createForm.phone" placeholder="请输入手机号" :maxlength="11" />
        </a-form-item>
        <a-form-item label="邮箱" required>
          <a-input v-model:value="createForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="角色" required>
          <a-select v-model:value="createForm.role" placeholder="请选择角色">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="BUYER">采购员</a-select-option>
            <a-select-option value="BUSINESS_USER">业务人员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 修改角色弹窗 -->
    <a-modal
      v-model:open="showRoleModal"
      title="修改角色"
      :confirm-loading="roleLoading"
      @ok="handleConfirmRoleChange"
    >
      <a-form layout="vertical">
        <a-form-item label="新角色">
          <a-select v-model:value="newRole" placeholder="请选择新角色">
            <a-select-option value="ADMIN">管理员</a-select-option>
            <a-select-option value="BUYER">采购员</a-select-option>
            <a-select-option value="BUSINESS_USER">业务人员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import {
  fetchUserList,
  executeCreateUser,
  executeUpdateUserRole,
  executeUpdateUserStatus,
  executeResetUserPassword,
  executeUnlockUser,
} from '../../application/manage-users.usecase';
import type { UserListItem, CreateUserRequest } from '../../types/dto/user.dto';

const loading = ref(false);
const userList = ref<UserListItem[]>([]);

const filters = reactive({
  role: undefined as string | undefined,
  status: undefined as string | undefined,
  keyword: '',
});

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '角色', dataIndex: 'role', key: 'role' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'actions', width: 280 },
];

const showCreateModal = ref(false);
const createLoading = ref(false);
const createForm = reactive<CreateUserRequest>({
  name: '',
  phone: '',
  email: '',
  role: 'BUYER' as CreateUserRequest['role'],
});

const showRoleModal = ref(false);
const roleLoading = ref(false);
const selectedUser = ref<UserListItem | null>(null);
const newRole = ref('');

onMounted(() => {
  loadUsers();
});

async function loadUsers() {
  loading.value = true;
  try {
    const result = await fetchUserList({
      role: filters.role,
      status: filters.status,
      keyword: filters.keyword || undefined,
      page: (pagination.current as number) - 1,
      size: pagination.pageSize as number,
    });
    userList.value = result.content;
    pagination.total = result.totalElements;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadUsers();
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadUsers();
}

async function handleCreateUser() {
  createLoading.value = true;
  const result = await executeCreateUser(createForm);
  if (result.success) {
    message.success('用户创建成功，初始密码已发送至邮箱');
    showCreateModal.value = false;
    resetCreateForm();
    loadUsers();
  } else {
    message.error(result.error || '创建失败');
  }
  createLoading.value = false;
}

function resetCreateForm() {
  createForm.name = '';
  createForm.phone = '';
  createForm.email = '';
  createForm.role = 'BUYER';
}

function handleChangeRole(record: UserListItem) {
  selectedUser.value = record;
  newRole.value = record.role;
  showRoleModal.value = true;
}

async function handleConfirmRoleChange() {
  if (!selectedUser.value) return;
  roleLoading.value = true;
  const result = await executeUpdateUserRole(selectedUser.value.id, newRole.value);
  if (result.success) {
    message.success('角色修改成功');
    showRoleModal.value = false;
    loadUsers();
  } else {
    message.error(result.error || '修改失败');
  }
  roleLoading.value = false;
}

function handleToggleStatus(record: UserListItem) {
  const newStatus = record.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE';
  const actionText = newStatus === 'DISABLED' ? '停用' : '启用';

  Modal.confirm({
    title: `确认${actionText}`,
    content: `确定要${actionText}用户 "${record.name}" 吗？`,
    async onOk() {
      const result = await executeUpdateUserStatus(record.id, newStatus);
      if (result.success) {
        message.success(`${actionText}成功`);
        loadUsers();
      } else {
        message.error(result.error || `${actionText}失败`);
      }
    },
  });
}

function handleResetPassword(record: UserListItem) {
  Modal.confirm({
    title: '确认重置密码',
    content: `确定要重置用户 "${record.name}" 的密码吗？新密码将发送至其邮箱。`,
    async onOk() {
      const result = await executeResetUserPassword(record.id);
      if (result.success) {
        message.success('密码已重置，新密码已发送至用户邮箱');
      } else {
        message.error(result.error || '重置失败');
      }
    },
  });
}

function handleUnlock(record: UserListItem) {
  Modal.confirm({
    title: '确认解锁',
    content: `确定要解锁用户 "${record.name}" 吗？`,
    async onOk() {
      const result = await executeUnlockUser(record.id);
      if (result.success) {
        message.success('用户已解锁');
        loadUsers();
      } else {
        message.error(result.error || '解锁失败');
      }
    },
  });
}

function getRoleLabel(role: string): string {
  const map: Record<string, string> = {
    ADMIN: '管理员',
    BUYER: '采购员',
    BUSINESS_USER: '业务人员',
  };
  return map[role] || role;
}

function getRoleColor(role: string): string {
  const map: Record<string, string> = {
    ADMIN: 'red',
    BUYER: 'blue',
    BUSINESS_USER: 'green',
  };
  return map[role] || 'default';
}
</script>
