<template>
  <a-modal
    :open="open"
    :title="contact ? '编辑联系人' : '新增联系人'"
    :confirm-loading="loading"
    @ok="handleOk"
    @cancel="emit('update:open', false)"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="姓名" required>
        <a-input v-model:value="form.name" placeholder="请输入姓名" />
      </a-form-item>
      <a-form-item label="手机号" required>
        <a-input v-model:value="form.phone" placeholder="请输入手机号" />
      </a-form-item>
      <a-form-item label="邮箱" required>
        <a-input v-model:value="form.email" placeholder="请输入邮箱" />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="职务">
            <a-input v-model:value="form.position" placeholder="请输入职务（选填）" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="部门">
            <a-input v-model:value="form.department" placeholder="请输入部门（选填）" />
          </a-form-item>
        </a-col>
      </a-row>
      <a-form-item>
        <a-checkbox v-model:checked="form.primary">设为主要联系人</a-checkbox>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';
import type { ContactDto, SaveContactRequest } from '../../types/dto/contact.dto';

const props = defineProps<{
  open: boolean;
  contact?: ContactDto | null;
  loading?: boolean;
}>();

const emit = defineEmits<{
  'update:open': [value: boolean];
  submit: [data: SaveContactRequest];
}>();

const form = reactive<SaveContactRequest>({
  name: '',
  phone: '',
  email: '',
  primary: false,
  position: '',
  department: '',
});

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      const c = props.contact;
      form.name = c?.name ?? '';
      form.phone = c?.phone ?? '';
      form.email = c?.email ?? '';
      form.primary = c?.primary ?? false;
      form.position = c?.position ?? '';
      form.department = c?.department ?? '';
    }
  },
);

function handleOk() {
  emit('submit', { ...form });
}
</script>
