<template>
  <div class="p-6 max-w-5xl mx-auto">
    <h2 class="text-xl font-semibold text-gray-800 mb-6">联系人管理</h2>
    <div class="bg-white rounded-xl shadow-sm p-5">
      <ContactList
        ref="contactListRef"
        :contacts="contacts"
        :loading="loading"
        :saving="saving"
        @save="(payload) => save(payload, () => contactListRef?.close())"
        @set-primary="setPrimary"
        @delete="remove"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import ContactList from '../components/ContactList.vue';
import { useContacts } from '../composables/useContacts';

const contactListRef = ref<InstanceType<typeof ContactList> | null>(null);
// 供应商端本企业联系人（supplierId=null）
const { contacts, loading, saving, load, save, setPrimary, remove } = useContacts(null);

onMounted(load);
</script>
