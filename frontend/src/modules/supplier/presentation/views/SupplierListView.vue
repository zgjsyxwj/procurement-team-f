<template>
  <div class="p-6 max-w-7xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-semibold text-gray-800">供应商管理</h2>
      <a-button type="primary" @click="goCreate">创建供应商</a-button>
    </div>

    <!-- 筛选 -->
    <div class="mb-4 p-4 bg-white rounded-xl shadow-sm">
      <a-space wrap>
        <a-input-search
          v-model:value="filters.nameKeyword"
          placeholder="搜索企业名称"
          style="width: 220px"
          allow-clear
          @search="handleSearch"
        />
        <a-select
          v-model:value="filters.status"
          placeholder="按状态筛选"
          allow-clear
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option v-for="(label, value) in SUPPLIER_STATUS_LABEL" :key="value" :value="value">
            {{ label }}
          </a-select-option>
        </a-select>
        <a-select
          v-model:value="filters.certExpiryStatus"
          placeholder="证件到期状态"
          allow-clear
          style="width: 150px"
          @change="handleSearch"
        >
          <a-select-option v-for="(label, value) in CERT_EXPIRY_STATUS_LABEL" :key="value" :value="value">
            {{ label }}
          </a-select-option>
        </a-select>
      </a-space>
    </div>

    <!-- 列表 -->
    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="list"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <a class="text-blue-600" @click="goDetail(record.id)">{{ record.name }}</a>
          </template>
          <template v-if="column.key === 'status'">
            <SupplierStatusTag :status="record.status" />
          </template>
          <template v-if="column.key === 'certExpiryStatus'">
            <CertExpiryTag :status="record.certExpiryStatus" />
          </template>
          <template v-if="column.key === 'actions'">
            <a-button type="link" size="small" @click="goDetail(record.id)">查看详情</a-button>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import type { TablePaginationConfig } from 'ant-design-vue';
import SupplierStatusTag from '../components/SupplierStatusTag.vue';
import CertExpiryTag from '../components/CertExpiryTag.vue';
import {
  SUPPLIER_STATUS_LABEL,
  CERT_EXPIRY_STATUS_LABEL,
  type SupplierStatus,
  type CertExpiryStatus,
} from '../../types/vo/supplier-info.vo';
import { fetchSupplierList } from '../../application/manage-suppliers.usecase';
import type { SupplierListItemDto } from '../../types/dto/supplier.dto';

const router = useRouter();
const loading = ref(false);
const list = ref<SupplierListItemDto[]>([]);

const filters = reactive({
  nameKeyword: '',
  status: undefined as SupplierStatus | undefined,
  certExpiryStatus: undefined as CertExpiryStatus | undefined,
});

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
});

const columns = [
  { title: '企业名称', dataIndex: 'name', key: 'name' },
  { title: '统一社会信用代码', dataIndex: 'unifiedSocialCreditCode', key: 'unifiedSocialCreditCode' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '主要联系人', dataIndex: 'primaryContactName', key: 'primaryContactName' },
  { title: '电话', dataIndex: 'primaryContactPhone', key: 'primaryContactPhone' },
  { title: '证件到期状态', dataIndex: 'certExpiryStatus', key: 'certExpiryStatus' },
  { title: '操作', key: 'actions', width: 120 },
];

onMounted(load);

async function load() {
  loading.value = true;
  try {
    const result = await fetchSupplierList({
      nameKeyword: filters.nameKeyword || undefined,
      status: filters.status,
      certExpiryStatus: filters.certExpiryStatus,
      page: (pagination.current as number) - 1,
      size: pagination.pageSize as number,
    });
    list.value = result.content;
    pagination.total = result.totalElements;
  } catch {
    message.error('加载供应商列表失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.current = 1;
  load();
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  load();
}

function goCreate() {
  router.push('/suppliers/create');
}

function goDetail(id: number) {
  router.push(`/suppliers/${id}`);
}
</script>
