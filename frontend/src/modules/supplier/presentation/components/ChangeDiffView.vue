<template>
  <div>
    <a-descriptions :column="1" size="small" bordered class="mb-4">
      <a-descriptions-item label="变更类型">
        {{ CHANGE_TYPE_LABEL[change.changeType] }}
      </a-descriptions-item>
      <a-descriptions-item label="来源">
        {{ CHANGE_SOURCE_LABEL[change.source] }}
      </a-descriptions-item>
      <a-descriptions-item label="提交人">
        {{ change.submitterName }}（{{ change.submittedAt }}）
      </a-descriptions-item>
      <a-descriptions-item v-if="change.reviewerName" label="审核人">
        {{ change.reviewerName }}（{{ change.reviewedAt }}）
      </a-descriptions-item>
      <a-descriptions-item v-if="change.reviewComment" label="审核意见">
        {{ change.reviewComment }}
      </a-descriptions-item>
    </a-descriptions>

    <a-table
      :columns="columns"
      :data-source="change.fields"
      :pagination="false"
      size="small"
      row-key="fieldKey"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'before'">
          <span class="text-gray-400">{{ displayValue(record.beforeValue) }}</span>
        </template>
        <template v-if="column.key === 'after'">
          <span class="text-blue-600 font-medium">{{ displayValue(record.afterValue) }}</span>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { CHANGE_TYPE_LABEL, CHANGE_SOURCE_LABEL } from '../../types/vo/supplier-info.vo';
import type { ChangeRequestDto } from '../../types/dto/change.dto';

defineProps<{ change: ChangeRequestDto }>();

const columns = [
  { title: '字段', dataIndex: 'fieldLabel', key: 'fieldLabel', width: 160 },
  { title: '变更前', key: 'before' },
  { title: '变更后', key: 'after' },
];

function displayValue(value?: string | null): string {
  return value === null || value === undefined || value === '' ? '（空）' : value;
}
</script>
