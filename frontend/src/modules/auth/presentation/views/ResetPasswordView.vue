<template>
  <div class="reset-password-container">
    <div class="reset-password-card">
      <h2 class="page-title">重置密码</h2>

      <a-alert
        v-if="errorMessage"
        :message="errorMessage"
        type="error"
        show-icon
        closable
        class="form-alert"
        @close="errorMessage = ''"
      />

      <a-alert
        v-if="successMessage"
        :message="successMessage"
        type="success"
        show-icon
        class="form-alert"
      />

      <a-form
        v-if="!resetSuccess"
        :model="formState"
        layout="vertical"
        @finish="handleSubmit"
      >
        <a-form-item
          label="新密码"
          name="newPassword"
          :validate-status="formState.newPassword && !passwordValidation.isValid.value ? 'warning' : ''"
        >
          <a-input-password
            v-model:value="formState.newPassword"
            placeholder="请输入新密码"
            size="large"
          />
        </a-form-item>

        <!-- 密码复杂度实时校验提示 -->
        <div v-if="formState.newPassword" class="password-rules">
          <div
            v-for="rule in passwordValidation.rules.value"
            :key="rule.label"
            :class="['rule-item', { passed: rule.passed }]"
          >
            <span class="rule-icon">{{ rule.passed ? '✓' : '✗' }}</span>
            <span>{{ rule.label }}</span>
          </div>
        </div>

        <a-form-item label="确认密码" name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            placeholder="请再次输入新密码"
            size="large"
          />
          <template v-if="formState.confirmPassword && formState.confirmPassword !== formState.newPassword">
            <span class="confirm-error">两次输入的密码不一致</span>
          </template>
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            block
            :loading="loading"
            :disabled="!canSubmit"
          >
            重置密码
          </a-button>
        </a-form-item>
      </a-form>

      <div class="page-footer">
        <router-link to="/internal/login">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { executeResetPassword } from '../../application/forgot-password.usecase';
import { usePasswordValidation } from '../composables/usePasswordValidation';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const resetSuccess = ref(false);

const formState = reactive({
  newPassword: '',
  confirmPassword: '',
});

const passwordValidation = usePasswordValidation();

// 同步表单密码到 composable
watch(() => formState.newPassword, (val) => {
  passwordValidation.password.value = val;
});

// 从 URL 参数获取重置 Token
const token = computed(() => (route.query.token as string) || '');

const canSubmit = computed(() => {
  return (
    passwordValidation.isValid.value &&
    formState.confirmPassword === formState.newPassword &&
    formState.confirmPassword.length > 0
  );
});

async function handleSubmit() {
  if (!token.value) {
    errorMessage.value = '重置链接无效';
    return;
  }

  loading.value = true;
  errorMessage.value = '';

  const result = await executeResetPassword(
    token.value,
    formState.newPassword,
    formState.confirmPassword,
  );

  if (result.success) {
    resetSuccess.value = true;
    successMessage.value = '密码重置成功，3秒后跳转到登录页...';
    setTimeout(() => {
      router.push('/internal/login');
    }, 3000);
  } else {
    errorMessage.value = result.error || '重置密码失败';
  }

  loading.value = false;
}
</script>

<style scoped>
.reset-password-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.reset-password-card {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.page-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 24px;
  color: #333;
}

.form-alert {
  margin-bottom: 24px;
}

.password-rules {
  margin-bottom: 16px;
  padding: 12px;
  background: #fafafa;
  border-radius: 4px;
}

.rule-item {
  font-size: 13px;
  color: #999;
  margin-bottom: 4px;
}

.rule-item.passed {
  color: #52c41a;
}

.rule-icon {
  margin-right: 8px;
}

.confirm-error {
  color: #ff4d4f;
  font-size: 12px;
}

.page-footer {
  text-align: center;
  margin-top: 24px;
}
</style>
