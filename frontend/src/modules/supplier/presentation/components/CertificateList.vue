<template>
  <a-table
    :columns="columns"
    :data-source="certificates"
    :loading="loading"
    :pagination="false"
    row-key="id"
    size="small"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'certTypeName'">
        {{ typeName(record) }}
      </template>

      <template v-if="column.key === 'fileName'">
        <a v-if="record.fileUrl" :href="record.fileUrl" target="_blank" rel="noopener">
          {{ record.fileName }}
        </a>
        <span v-else>{{ record.fileName }}</span>
      </template>

      <template v-if="column.key === 'validity'">
        {{ record.validFrom }} ~ {{ record.validTo }}
      </template>

      <template v-if="column.key === 'auditStatus'">
        <a-tag :color="CERT_AUDIT_STATUS_COLOR[record.auditStatus as CertificateAuditStatus]">
          {{ CERT_AUDIT_STATUS_LABEL[record.auditStatus as CertificateAuditStatus] }}
        </a-tag>
        <a-tooltip v-if="record.rejectReason" :title="record.rejectReason">
          <span class="text-red-500 ml-1 cursor-help">（驳回原因）</span>
        </a-tooltip>
      </template>

      <template v-if="column.key === 'expiry'">
        <CertExpiryTag :status="record.expiryStatus ?? deriveExpiryStatus(record.validTo)" />
      </template>

      <template v-if="column.key === 'source'">
        {{ CERT_SOURCE_LABEL[record.source as CertificateSource] }}
      </template>

      <template v-if="column.key === 'actions'">
        <a-space v-if="showReview && record.auditStatus === 'PENDING_REVIEW'">
          <a-button type="link" size="small" @click="emit('approve', record)">通过</a-button>
          <a-button type="link" size="small" danger @click="emit('reject', record)">驳回</a-button>
        </a-space>
        <span v-else>-</span>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import CertExpiryTag from './CertExpiryTag.vue';
import {
  CERT_AUDIT_STATUS_LABEL,
  CERT_AUDIT_STATUS_COLOR,
  CERT_SOURCE_LABEL,
  type CertificateAuditStatus,
  type CertificateSource,
} from '../../types/vo/supplier-info.vo';
import { deriveExpiryStatus } from '../../domain/entities/certificate.entity';
import type { CertificateDto, CertTypeDto } from '../../types/dto/certificate.dto';

const props = withDefaults(
  defineProps<{
    certificates: CertificateDto[];
    loading?: boolean;
    showReview?: boolean;
    certTypes?: CertTypeDto[];
  }>(),
  { loading: false, showReview: false, certTypes: () => [] },
);

/** 解析证件类型名称：优先用响应自带 certTypeName，否则从 certTypes 字典按 certTypeId 映射 */
function typeName(record: CertificateDto): string {
  if (record.certTypeName) return record.certTypeName;
  return props.certTypes.find((t) => t.id === record.certTypeId)?.name ?? String(record.certTypeId);
}

const emit = defineEmits<{
  approve: [cert: CertificateDto];
  reject: [cert: CertificateDto];
}>();

const columns = [
  { title: '证件类型', dataIndex: 'certTypeName', key: 'certTypeName' },
  { title: '文件', key: 'fileName' },
  { title: '有效期', key: 'validity' },
  { title: '审核状态', key: 'auditStatus' },
  { title: '到期状态', key: 'expiry' },
  { title: '来源', key: 'source' },
  { title: '操作', key: 'actions', width: 140 },
];
</script>
