<template>
  <a-modal
    :open="open"
    title="发送邀请"
    :confirm-loading="loading"
    ok-text="发送"
    @ok="emit('confirm')"
    @cancel="emit('update:open', false)"
  >
    <p>
      将向供应商 <strong>{{ supplierName }}</strong> 的主要联系人邮箱发送门户邀请邮件。
    </p>
    <p class="text-gray-400 text-sm">若此前已发送，将重新发送一封邀请邮件。</p>
  </a-modal>
</template>

<script setup lang="ts">
defineProps<{
  open: boolean;
  supplierName: string;
  loading?: boolean;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
  confirm: [];
}>();
</script>
