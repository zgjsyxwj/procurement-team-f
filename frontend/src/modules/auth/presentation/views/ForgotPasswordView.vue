<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="w-full max-w-md p-10 bg-white rounded-2xl shadow-xl">
      <h2 class="text-center text-2xl font-bold text-gray-800 mb-4">忘记密码</h2>

      <template v-if="!submitted">
        <p class="text-center text-gray-500 mb-6">
          请输入您的注册邮箱，我们将发送密码重置链接到您的邮箱。
        </p>

        <a-alert
          v-if="errorMessage"
          :message="errorMessage"
          type="error"
          show-icon
          closable
          class="mb-6"
          @close="errorMessage = ''"
        />

        <a-form
          :model="formState"
          :rules="formRules"
          layout="vertical"
          @finish="handleSubmit"
        >
          <a-form-item label="邮箱" name="email">
            <a-input
              v-model:value="formState.email"
              placeholder="请输入注册邮箱"
              size="large"
            />
          </a-form-item>

          <a-form-item>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              block
              :loading="loading"
            >
              发送重置链接
            </a-button>
          </a-form-item>
        </a-form>
      </template>

      <template v-else>
        <a-alert
          message="邮件已发送"
          description="如果该邮箱已注册，您将收到重置邮件。请检查您的收件箱（包括垃圾邮件文件夹）。"
          type="success"
          show-icon
        />
      </template>

      <div class="text-center mt-6">
        <router-link to="/internal/login" class="text-blue-500 hover:text-blue-600">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { executeForgotPassword } from '../../application/forgot-password.usecase';
import type { Rule } from 'ant-design-vue/es/form';

const loading = ref(false);
const errorMessage = ref('');
const submitted = ref(false);

const formState = reactive({
  email: '',
});

const formRules: Record<string, Rule[]> = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
};

async function handleSubmit() {
  loading.value = true;
  errorMessage.value = '';

  const result = await executeForgotPassword(formState.email);

  if (result.success) {
    submitted.value = true;
  } else {
    errorMessage.value = result.error || '操作失败，请稍后重试';
  }

  loading.value = false;
}
</script>
