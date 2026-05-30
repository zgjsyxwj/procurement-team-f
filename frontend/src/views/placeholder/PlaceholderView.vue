<template>
  <div class="placeholder-view">
    <a-result status="info" :title="title" :sub-title="subtitle">
      <template #extra>
        <a-tag color="blue">{{ moduleName }}</a-tag>
        <a-tag>待开发</a-tag>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const title = computed(() => (route.meta.title as string) || '功能开发中');
const subtitle = computed(() => (route.meta.subtitle as string) || '该功能模块尚未实现，敬请期待');
const moduleName = computed(() => (route.meta.module as string) || '未知模块');
</script>

<style scoped>
.placeholder-view {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}
</style>
