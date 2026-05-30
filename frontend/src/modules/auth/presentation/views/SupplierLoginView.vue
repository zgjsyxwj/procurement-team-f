<template>
  <div class="flex items-center justify-center min-h-screen bg-gradient-to-br from-orange-50 to-amber-100">
    <div class="w-full max-w-md p-10 bg-white rounded-2xl shadow-xl">
      <h2 class="text-center text-2xl font-bold text-gray-800 mb-8">供应商登录</h2>

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

      <div class="flex justify-end mt-4">
        <router-link to="/forgot-password" class="text-blue-500 hover:text-blue-600">忘记密码</router-link>
      </div>
    </div>

    <!-- 首次登录修改密码弹窗 -->
    <a-modal
      v-model:open="showChangePasswordModal"
      title="建议修改密码"
      :closable="false"
      :mask-closable="false"
    >
      <p class="text-gray-600">您是首次登录，建议修改初始密码以确保账号安全。</p>
      <template #footer>
        <a-space>
          <a-button @click="skipChangePassword">稍后再说</a-button>
          <a-button type="primary" @click="goChangePassword">立即修改</a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import type { Rule } from 'ant-design-vue/es/form';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const errorMessage = ref('');
const showChangePasswordModal = ref(false);

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
    await authStore.supplierLogin(formState.phone, formState.password);
    if (authStore.isFirstLogin) {
      showChangePasswordModal.value = true;
    } else {
      router.push('/');
    }
  } catch (error: unknown) {
    errorMessage.value = extractErrorMessage(error);
  } finally {
    loading.value = false;
  }
}

function skipChangePassword() {
  showChangePasswordModal.value = false;
  router.push('/');
}

function goChangePassword() {
  showChangePasswordModal.value = false;
  router.push('/change-password');
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
