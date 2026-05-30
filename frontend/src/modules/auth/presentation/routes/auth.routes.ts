import type { RouteRecordRaw, Router } from 'vue-router';
import { useAuthStore } from '../stores/auth.store';

/**
 * vue-router meta 字段类型扩展
 */
declare module 'vue-router' {
  interface RouteMeta {
    /** 公开路由，无需认证即可访问 */
    public?: boolean;
    /** 需要认证才能访问 */
    requiresAuth?: boolean;
    /** 允许访问的角色列表 */
    roles?: string[];
  }
}

/**
 * 认证与权限管理模块路由配置
 *
 * 路由 meta 字段说明：
 * - public: 公开路由，无需认证即可访问
 * - requiresAuth: 需要认证才能访问
 * - roles: 允许访问的角色列表（需配合 requiresAuth 使用）
 */
export const authRoutes: RouteRecordRaw[] = [
  // 公开路由 - 登录与密码重置
  {
    path: '/internal/login',
    name: 'InternalLogin',
    component: () => import('../views/InternalLoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/supplier/login',
    name: 'SupplierLogin',
    component: () => import('../views/SupplierLoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPasswordView.vue'),
    meta: { public: true },
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/ResetPasswordView.vue'),
    meta: { public: true },
  },

  // 管理员路由已移至主布局路由中（src/router/index.ts）
];


/**
 * 创建全局路由守卫
 *
 * 功能：
 * 1. 全局前置守卫：未认证用户重定向到登录页
 * 2. 角色权限守卫：非 ADMIN 用户访问管理页面时重定向到首页
 * 3. 页面入口隔离：业务人员/采购员/供应商各自路由互不可见
 */
export function createAuthGuard(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore();

    // 公开路由 - 允许直接访问
    if (to.meta.public) {
      next();
      return;
    }

    // 检查认证状态
    if (!authStore.isAuthenticated) {
      // 尝试从后端恢复会话（利用 httpOnly Cookie）
      await authStore.checkAuth();

      if (!authStore.isAuthenticated) {
        // 未认证，重定向到内部用户登录页
        next({ name: 'InternalLogin', query: { redirect: to.fullPath } });
        return;
      }
    }

    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      const userRole = authStore.user?.role;
      if (!userRole || !to.meta.roles.includes(userRole)) {
        // 无权限，重定向到首页
        next('/');
        return;
      }
    }

    next();
  });
}

/**
 * 配置 Axios 响应拦截器，处理会话超时
 *
 * 当后端返回 401 时，清除用户状态并跳转到登录页
 */
export function setupSessionTimeoutInterceptor(router: Router): void {
  import('axios').then(({ default: axios }) => {
    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          const authStore = useAuthStore();
          authStore.clearUser();
          router.push({ name: 'InternalLogin' });
        }
        return Promise.reject(error);
      },
    );
  });
}
