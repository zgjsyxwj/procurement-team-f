<template>
  <div>
    <div class="flex items-center justify-between mb-3">
      <span class="font-medium text-gray-700">联系人</span>
      <a-button type="primary" size="small" @click="openCreate">新增联系人</a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="contacts"
      :loading="loading"
      :pagination="false"
      row-key="id"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'primary'">
          <a-tag v-if="record.primary" color="blue">主要联系人</a-tag>
          <span v-else>-</span>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space>
            <a-button type="link" size="small" @click="openEdit(record)">编辑</a-button>
            <a-button
              v-if="!record.primary"
              type="link"
              size="small"
              @click="emit('set-primary', record)"
            >
              设为主要
            </a-button>
            <a-button
              v-if="showInvite"
              type="link"
              size="small"
              @click="emit('invite', record)"
            >
              发送邀请
            </a-button>
            <a-button
              type="link"
              size="small"
              danger
              :disabled="record.primary"
              @click="emit('delete', record)"
            >
              删除
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <ContactFormDialog
      v-model:open="dialogOpen"
      :contact="editing"
      :loading="saving"
      @submit="onSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import ContactFormDialog from './ContactFormDialog.vue';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';

withDefaults(
  defineProps<{
    contacts: ContactDto[];
    loading?: boolean;
    saving?: boolean;
    showInvite?: boolean;
  }>(),
  { loading: false, saving: false, showInvite: false },
);

const emit = defineEmits<{
  'set-primary': [contact: ContactDto];
  delete: [contact: ContactDto];
  invite: [contact: ContactDto];
  save: [payload: { contactId: number | null; data: SaveContactRequest }];
}>();

const columns = [
  { title: '姓名', dataIndex: 'name', key: 'name' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '邮箱', dataIndex: 'email', key: 'email' },
  { title: '职务', dataIndex: 'position', key: 'position' },
  { title: '部门', dataIndex: 'department', key: 'department' },
  { title: '主要联系人', key: 'primary', width: 120 },
  { title: '操作', key: 'actions', width: 240 },
];

const dialogOpen = ref(false);
const editing = ref<ContactDto | null>(null);

function openCreate() {
  editing.value = null;
  dialogOpen.value = true;
}

function openEdit(record: ContactDto) {
  editing.value = record;
  dialogOpen.value = true;
}

function onSubmit(data: SaveContactRequest) {
  emit('save', { contactId: editing.value?.id ?? null, data });
}

/** 由父组件在保存成功后调用以关闭弹窗 */
function close() {
  dialogOpen.value = false;
}

defineExpose({ close });
</script>
