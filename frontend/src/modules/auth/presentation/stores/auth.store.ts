import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import type { UserInfo } from '../../types/vo/user-info.vo';
import * as authService from '../../infrastructure/services/auth.service';

export const useAuthStore = defineStore('auth', () => {
  // State
  const user = ref<UserInfo | null>(null);

  // Getters
  const isAuthenticated = computed(() => user.value !== null);
  const isAdmin = computed(() => user.value?.role === 'ADMIN');
  const isBuyer = computed(() => user.value?.role === 'BUYER');
  const isBusinessUser = computed(() => user.value?.role === 'BUSINESS_USER');
  const isSupplier = computed(() => user.value?.role === 'SUPPLIER');
  const isFirstLogin = computed(() => user.value?.isFirstLogin ?? false);

  // Actions

  /**
   * 内部用户登录
   */
  async function login(phone: string, password: string): Promise<void> {
    const response = await authService.login(phone, password);
    user.value = {
      id: response.id,
      name: response.name,
      role: response.role as UserInfo['role'],
      isFirstLogin: response.isFirstLogin,
    };
  }

  /**
   * 供应商登录
   */
  async function supplierLogin(phone: string, password: string): Promise<void> {
    const response = await authService.supplierLogin(phone, password);
    user.value = {
      id: response.id,
      name: response.name,
      role: response.role as UserInfo['role'],
      isFirstLogin: response.isFirstLogin,
    };
  }

  /**
   * 登出
   */
  async function logout(): Promise<void> {
    try {
      await authService.logout();
    } finally {
      user.value = null;
    }
  }

  /**
   * 页面刷新时从后端获取当前用户信息
   */
  async function checkAuth(): Promise<void> {
    try {
      const { default: axios } = await import('axios');
      const res = await axios.get('/api/auth/me', { withCredentials: true });
      user.value = res.data as UserInfo;
    } catch {
      user.value = null;
    }
  }

  function setUser(userInfo: UserInfo): void {
    user.value = userInfo;
  }

  function clearUser(): void {
    user.value = null;
  }

  return {
    user,
    isAuthenticated,
    isAdmin,
    isBuyer,
    isBusinessUser,
    isSupplier,
    isFirstLogin,
    login,
    supplierLogin,
    logout,
    checkAuth,
    setUser,
    clearUser,
  };
});
