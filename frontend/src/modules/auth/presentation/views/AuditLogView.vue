<template>
  <div class="p-6 max-w-7xl mx-auto">
    <div class="mb-6">
      <h2 class="text-xl font-semibold text-gray-800">审计日志</h2>
    </div>

    <!-- 筛选条件 -->
    <div class="mb-4 p-4 bg-white rounded-xl shadow-sm">
      <a-space>
        <a-select
          v-model:value="filters.eventType"
          placeholder="事件类型"
          allow-clear
          style="width: 180px"
          @change="handleSearch"
        >
          <a-select-option value="LOGIN_SUCCESS">登录成功</a-select-option>
          <a-select-option value="LOGIN_FAILURE">登录失败</a-select-option>
          <a-select-option value="LOGOUT">登出</a-select-option>
          <a-select-option value="PASSWORD_CHANGE">密码修改</a-select-option>
          <a-select-option value="PASSWORD_RESET">密码重置</a-select-option>
          <a-select-option value="ACCOUNT_LOCKED">账号锁定</a-select-option>
          <a-select-option value="ACCOUNT_UNLOCKED">账号解锁</a-select-option>
          <a-select-option value="ACCOUNT_CREATED">账号创建</a-select-option>
          <a-select-option value="ACCOUNT_DISABLED">账号停用</a-select-option>
          <a-select-option value="ACCOUNT_ENABLED">账号启用</a-select-option>
          <a-select-option value="ROLE_CHANGED">角色变更</a-select-option>
        </a-select>

        <a-range-picker
          v-model:value="filters.dateRange"
          :placeholder="['开始时间', '结束时间']"
          @change="handleSearch"
        />

        <a-input-search
          v-model:value="filters.targetAccount"
          placeholder="目标账号"
          style="width: 160px"
          @search="handleSearch"
        />
      </a-space>
    </div>

    <!-- 审计日志表格 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="logList"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'eventType'">
            <a-tag :color="getEventColor(record.eventType)">
              {{ getEventLabel(record.eventType) }}
            </a-tag>
          </template>

          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'green' : 'red'">
              {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import type { Dayjs } from 'dayjs';
import { fetchAuditLogs } from '../../application/manage-users.usecase';
import type { AuditLogItem } from '../../types/dto/user.dto';

const loading = ref(false);
const logList = ref<AuditLogItem[]>([]);

const filters = reactive({
  eventType: undefined as string | undefined,
  dateRange: null as [Dayjs, Dayjs] | null,
  targetAccount: '',
});

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: '事件类型', dataIndex: 'eventType', key: 'eventType', width: 120 },
  { title: '操作人', dataIndex: 'operatorName', key: 'operatorName' },
  { title: '目标账号', dataIndex: 'targetUserName', key: 'targetUserName' },
  { title: 'IP 地址', dataIndex: 'ipAddress', key: 'ipAddress' },
  { title: '结果', dataIndex: 'result', key: 'result', width: 80 },
  { title: '详情', dataIndex: 'detail', key: 'detail', ellipsis: true },
  { title: '时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
];

onMounted(() => {
  loadLogs();
});

async function loadLogs() {
  loading.value = true;
  try {
    const result = await fetchAuditLogs({
      eventType: filters.eventType,
      startTime: filters.dateRange?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
      endTime: filters.dateRange?.[1]?.format('YYYY-MM-DD HH:mm:ss'),
      targetUserId: undefined,
      page: (pagination.current as number) - 1,
      size: pagination.pageSize as number,
    });
    logList.value = result.content;
    pagination.total = result.totalElements;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  loadLogs();
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadLogs();
}

function getEventLabel(eventType: string): string {
  const map: Record<string, string> = {
    LOGIN_SUCCESS: '登录成功',
    LOGIN_FAILURE: '登录失败',
    LOGOUT: '登出',
    SESSION_TIMEOUT: '会话超时',
    PASSWORD_CHANGE: '密码修改',
    PASSWORD_RESET: '密码重置',
    ACCOUNT_LOCKED: '账号锁定',
    ACCOUNT_UNLOCKED: '账号解锁',
    ACCOUNT_CREATED: '账号创建',
    ACCOUNT_DISABLED: '账号停用',
    ACCOUNT_ENABLED: '账号启用',
    ROLE_CHANGED: '角色变更',
  };
  return map[eventType] || eventType;
}

function getEventColor(eventType: string): string {
  const map: Record<string, string> = {
    LOGIN_SUCCESS: 'green',
    LOGIN_FAILURE: 'red',
    LOGOUT: 'default',
    SESSION_TIMEOUT: 'orange',
    PASSWORD_CHANGE: 'blue',
    PASSWORD_RESET: 'blue',
    ACCOUNT_LOCKED: 'red',
    ACCOUNT_UNLOCKED: 'green',
    ACCOUNT_CREATED: 'cyan',
    ACCOUNT_DISABLED: 'red',
    ACCOUNT_ENABLED: 'green',
    ROLE_CHANGED: 'purple',
  };
  return map[eventType] || 'default';
}
</script>
