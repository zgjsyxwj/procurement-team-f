<template>
  <a-modal
    :open="open"
    title="调整供应商状态"
    :confirm-loading="loading"
    @ok="handleOk"
    @cancel="emit('update:open', false)"
  >
    <a-form layout="vertical">
      <a-form-item label="目标状态" required>
        <a-radio-group v-model:value="targetStatus" @change="onTargetChange">
          <a-radio value="ACTIVE">合作中</a-radio>
          <a-radio value="DISABLED">已停用</a-radio>
        </a-radio-group>
      </a-form-item>

      <!-- 停用风险提示 -->
      <a-alert
        v-if="targetStatus === 'DISABLED'"
        class="mb-3"
        type="warning"
        show-icon
        :message="impact && impact.hasImpact ? '停用将影响以下未完成事项：' : '停用后该供应商将无法参与报价等业务。'"
      >
        <template v-if="impact && impact.hasImpact" #description>
          <ul class="list-disc pl-5">
            <li v-for="(item, idx) in impact.affectedItems" :key="idx">{{ item }}</li>
          </ul>
        </template>
      </a-alert>
      <a-spin v-if="impactLoading" size="small" class="mb-3" />

      <a-form-item label="操作备注">
        <a-textarea v-model:value="remark" :rows="3" placeholder="请输入操作备注（选填）" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { fetchDisableImpact } from '../../application/manage-suppliers.usecase';
import type { DisableImpactDto } from '../../types/dto/supplier.dto';

const props = defineProps<{
  open: boolean;
  supplierId: number;
  loading?: boolean;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
  confirm: [payload: { targetStatus: 'ACTIVE' | 'DISABLED'; remark?: string }];
}>();

const targetStatus = ref<'ACTIVE' | 'DISABLED'>('DISABLED');
const remark = ref('');
const impact = ref<DisableImpactDto | null>(null);
const impactLoading = ref(false);

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      targetStatus.value = 'DISABLED';
      remark.value = '';
      impact.value = null;
      loadImpact();
    }
  },
);

function onTargetChange() {
  if (targetStatus.value === 'DISABLED') {
    loadImpact();
  }
}

async function loadImpact() {
  if (targetStatus.value !== 'DISABLED') return;
  impactLoading.value = true;
  try {
    impact.value = await fetchDisableImpact(props.supplierId);
  } catch {
    impact.value = null;
  } finally {
    impactLoading.value = false;
  }
}

function handleOk() {
  emit('confirm', { targetStatus: targetStatus.value, remark: remark.value || undefined });
}
</script>
