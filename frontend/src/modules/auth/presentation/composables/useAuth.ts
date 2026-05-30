import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';
import type { UserInfo } from '../../types/vo/user-info.vo';

/**
 * 认证状态 composable
 *
 * 封装认证状态判断逻辑，提供响应式属性和操作方法。
 * 用于组件中快速获取当前用户信息、判断角色权限、执行登出等操作。
 */
export function useAuth() {
  const authStore = useAuthStore();
  const router = useRouter();

  // 响应式属性
  const currentUser = computed<UserInfo | null>(() => authStore.user);
  const isAuthenticated = computed<boolean>(() => authStore.isAuthenticated);
  const isAdmin = computed<boolean>(() => authStore.isAdmin);
  const isBuyer = computed<boolean>(() => authStore.isBuyer);
  const isBusinessUser = computed<boolean>(() => authStore.isBusinessUser);
  const isSupplier = computed<boolean>(() => authStore.isSupplier);
  const isFirstLogin = computed<boolean>(() => authStore.isFirstLogin);

  /**
   * 判断当前用户是否拥有指定角色
   */
  function hasRole(role: UserInfo['role']): boolean {
    return authStore.user?.role === role;
  }

  /**
   * 判断当前用户是否拥有指定角色列表中的任一角色
   */
  function hasAnyRole(roles: UserInfo['role'][]): boolean {
    const userRole = authStore.user?.role;
    if (!userRole) return false;
    return roles.includes(userRole);
  }

  /**
   * 执行登出操作
   * 调用后端登出接口，清除本地状态，跳转到登录页
   */
  async function logout(): Promise<void> {
    await authStore.logout();
  }

  /**
   * 跳转到登录页
   * 可选保存当前路径用于登录后回跳
   */
  function redirectToLogin(redirectPath?: string): void {
    const query = redirectPath ? { redirect: redirectPath } : undefined;
    router.push({ name: 'InternalLogin', query });
  }

  return {
    // 响应式属性
    currentUser,
    isAuthenticated,
    isAdmin,
    isBuyer,
    isBusinessUser,
    isSupplier,
    isFirstLogin,
    // 方法
    hasRole,
    hasAnyRole,
    logout,
    redirectToLogin,
  };
}
