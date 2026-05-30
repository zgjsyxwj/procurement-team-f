<template>
  <a-tag :color="SUPPLIER_STATUS_COLOR[status]">{{ SUPPLIER_STATUS_LABEL[status] }}</a-tag>
</template>

<script setup lang="ts">
import {
  SUPPLIER_STATUS_LABEL,
  SUPPLIER_STATUS_COLOR,
  type SupplierStatus,
} from '../../types/vo/supplier-info.vo';

defineProps<{ status: SupplierStatus }>();
</script>
