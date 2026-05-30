<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
    <div class="w-full max-w-md p-10 bg-white rounded-2xl shadow-xl">
      <h2 class="text-center text-2xl font-bold text-gray-800 mb-8">内部用户登录</h2>

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
        @finish="handleLogin"
      >
        <a-form-item label="手机号" name="phone">
          <a-input
            v-model:value="formState.phone"
            placeholder="请输入手机号"
            size="large"
            :maxlength="11"
          />
        </a-form-item>

        <a-form-item label="密码" name="password">
          <a-input-password
            v-model:value="formState.password"
            placeholder="请输入密码"
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
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="flex items-center justify-between mt-4">
        <a-button type="link" @click="handleSsoLogin">
          SSO 登录（Worklife）
        </a-button>
        <router-link to="/forgot-password" class="text-blue-500 hover:text-blue-600">忘记密码</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import { initiateSsoLogin } from '../../application/sso-login.usecase';
import type { Rule } from 'ant-design-vue/es/form';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const errorMessage = ref('');

const formState = reactive({
  phone: '',
  password: '',
});

const formRules: Record<string, Rule[]> = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入有效的手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
  ],
};

async function handleLogin() {
  loading.value = true;
  errorMessage.value = '';

  try {
    await authStore.login(formState.phone, formState.password);
    router.push('/');
  } catch (error: unknown) {
    errorMessage.value = extractErrorMessage(error);
  } finally {
    loading.value = false;
  }
}

function handleSsoLogin() {
  initiateSsoLogin();
}

function extractErrorMessage(error: unknown): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string }; status?: number } }).response;
    if (response?.status === 423) {
      return '账号已锁定，请30分钟后重试或联系管理员';
    }
    if (response?.status === 403) {
      return '账号已停用，请联系管理员';
    }
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return '手机号或密码错误';
}
</script>
