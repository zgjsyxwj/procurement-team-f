<template>
  <div class="p-6 max-w-6xl mx-auto">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">审核中心 - 信息变更</h2>

    <div class="bg-white rounded-xl shadow-sm overflow-hidden">
      <a-table
        :columns="columns"
        :data-source="pendingList"
        :loading="loading"
        :pagination="false"
        row-key="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'changeType'">
            {{ CHANGE_TYPE_LABEL[record.changeType as ChangeType] }}
          </template>
          <template v-if="column.key === 'actions'">
            <a-button type="link" size="small" @click="openReview(record.id)">审核</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 审核弹窗 -->
    <a-modal
      v-model:open="reviewOpen"
      title="变更审核"
      width="720px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <ChangeDiffView v-if="detail" :change="detail" />
        <div class="mt-4">
          <a-textarea
            v-model:value="rejectReason"
            :rows="2"
            placeholder="驳回时请填写原因"
          />
        </div>
        <div class="flex justify-end gap-2 mt-4">
          <a-button danger :loading="rejecting" @click="handleReject">驳回</a-button>
          <a-button type="primary" :loading="approving" @click="handleApprove">通过</a-button>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import ChangeDiffView from '../components/ChangeDiffView.vue';
import {
  fetchPendingChanges,
  fetchChangeDetail,
  executeApproveChange,
  executeRejectChange,
} from '../../application/review-change.usecase';
import { CHANGE_TYPE_LABEL, type ChangeType } from '../../types/vo/supplier-info.vo';
import type { ChangeRequestDto } from '../../types/dto/change.dto';

const loading = ref(false);
const pendingList = ref<ChangeRequestDto[]>([]);

const reviewOpen = ref(false);
const detail = ref<ChangeRequestDto | null>(null);
const detailLoading = ref(false);
const rejectReason = ref('');
const approving = ref(false);
const rejecting = ref(false);

const columns = [
  { title: '供应商ID', dataIndex: 'supplierId', key: 'supplierId', width: 110 },
  { title: '变更类型', key: 'changeType' },
  { title: '提交人', dataIndex: 'submitterName', key: 'submitterName' },
  { title: '提交时间', dataIndex: 'submittedAt', key: 'submittedAt' },
  { title: '操作', key: 'actions', width: 100 },
];

onMounted(load);

async function load() {
  loading.value = true;
  try {
    pendingList.value = await fetchPendingChanges();
  } catch {
    message.error('加载待审核变更失败');
  } finally {
    loading.value = false;
  }
}

async function openReview(id: number) {
  reviewOpen.value = true;
  rejectReason.value = '';
  detail.value = null;
  detailLoading.value = true;
  try {
    detail.value = await fetchChangeDetail(id);
  } catch {
    message.error('加载变更详情失败');
  } finally {
    detailLoading.value = false;
  }
}

async function handleApprove() {
  if (!detail.value) return;
  approving.value = true;
  const result = await executeApproveChange(detail.value.id);
  approving.value = false;
  if (result.success) {
    message.success('已通过');
    reviewOpen.value = false;
    await load();
  } else {
    message.error(result.error || '操作失败');
  }
}

async function handleReject() {
  if (!detail.value) return;
  rejecting.value = true;
  const result = await executeRejectChange(detail.value.id, rejectReason.value);
  rejecting.value = false;
  if (result.success) {
    message.success('已驳回');
    reviewOpen.value = false;
    await load();
  } else {
    message.error(result.error || '操作失败');
  }
}
</script>
