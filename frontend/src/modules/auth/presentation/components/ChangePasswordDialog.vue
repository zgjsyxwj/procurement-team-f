<template>
  <a-modal
    :open="open"
    title="修改密码"
    :confirm-loading="loading"
    @cancel="handleCancel"
    @ok="handleSubmit"
  >
    <a-alert
      v-if="errorMessage"
      :message="errorMessage"
      type="error"
      show-icon
      closable
      class="mb-4"
      @close="errorMessage = ''"
    />

    <a-form :model="formState" layout="vertical">
      <a-form-item label="旧密码" required>
        <a-input-password
          v-model:value="formState.oldPassword"
          placeholder="请输入旧密码"
        />
      </a-form-item>

      <a-form-item label="新密码" required>
        <a-input-password
          v-model:value="formState.newPassword"
          placeholder="请输入新密码"
        />
      </a-form-item>

      <!-- 密码复杂度实时校验提示 -->
      <div v-if="formState.newPassword" class="mb-4 p-3 bg-gray-50 rounded-lg">
        <div
          v-for="rule in passwordValidation.rules.value"
          :key="rule.label"
          class="text-sm mb-1 flex items-center gap-2"
          :class="rule.passed ? 'text-green-500' : 'text-gray-400'"
        >
          <span>{{ rule.passed ? '✓' : '✗' }}</span>
          <span>{{ rule.label }}</span>
        </div>
      </div>

      <a-form-item label="确认新密码" required>
        <a-input-password
          v-model:value="formState.confirmPassword"
          placeholder="请再次输入新密码"
        />
        <template v-if="formState.confirmPassword && formState.confirmPassword !== formState.newPassword">
          <span class="text-red-500 text-xs">两次输入的密码不一致</span>
        </template>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { executeChangePassword } from '../../application/change-password.usecase';
import { usePasswordValidation } from '../composables/usePasswordValidation';

const props = defineProps<{
  open: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void;
  (e: 'success'): void;
}>();

const loading = ref(false);
const errorMessage = ref('');

const formState = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
});

const passwordValidation = usePasswordValidation();

watch(() => formState.newPassword, (val) => {
  passwordValidation.password.value = val;
});

watch(() => props.open, (val) => {
  if (!val) {
    formState.oldPassword = '';
    formState.newPassword = '';
    formState.confirmPassword = '';
    errorMessage.value = '';
    passwordValidation.password.value = '';
  }
});

function handleCancel() {
  emit('update:open', false);
}

async function handleSubmit() {
  loading.value = true;
  errorMessage.value = '';

  const result = await executeChangePassword(
    formState.oldPassword,
    formState.newPassword,
    formState.confirmPassword,
  );

  if (result.success) {
    message.success('密码修改成功');
    emit('update:open', false);
    emit('success');
  } else {
    errorMessage.value = result.error || '修改密码失败';
  }

  loading.value = false;
}
</script>
