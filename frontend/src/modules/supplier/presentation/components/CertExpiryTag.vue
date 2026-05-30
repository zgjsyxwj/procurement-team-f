<template>
  <a-tag :color="CERT_EXPIRY_STATUS_COLOR[status]">{{ CERT_EXPIRY_STATUS_LABEL[status] }}</a-tag>
</template>

<script setup lang="ts">
import {
  CERT_EXPIRY_STATUS_LABEL,
  CERT_EXPIRY_STATUS_COLOR,
  type CertExpiryStatus,
} from '../../types/vo/supplier-info.vo';

defineProps<{ status: CertExpiryStatus }>();
</script>
