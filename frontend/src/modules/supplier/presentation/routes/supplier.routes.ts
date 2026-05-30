import type { RouteRecordRaw } from 'vue-router';

/**
 * 供应商管理模块（模块02）路由 —— 主布局（需认证）下的子路由。
 *
 * <p>角色守卫由 meta.roles 配合 `createAuthGuard`（模块01）统一执行：
 * - 采购端（ADMIN/BUYER）：供应商列表/创建/详情、变更审核中心
 * - 管理端（ADMIN）：证件类型字典
 * - 供应商端（SUPPLIER）：企业信息（默认入口，无工作台首页）、联系人管理
 */
export const supplierRoutes: RouteRecordRaw[] = [
  // ==================== 采购端 ====================
  {
    path: 'suppliers',
    name: 'SupplierList',
    component: () => import('../views/SupplierListView.vue'),
    meta: { title: '供应商管理', subtitle: '查看和管理供应商信息', module: '供应商管理', roles: ['ADMIN', 'BUYER'] },
  },
  {
    path: 'suppliers/create',
    name: 'SupplierCreate',
    component: () => import('../views/SupplierCreateView.vue'),
    meta: { title: '创建供应商', subtitle: '填写供应商基础信息并发送邀请', module: '供应商管理', roles: ['ADMIN', 'BUYER'] },
  },
  {
    path: 'suppliers/:id',
    name: 'SupplierDetail',
    component: () => import('../views/SupplierDetailView.vue'),
    meta: { title: '供应商详情', subtitle: '查看供应商企业信息、联系人和证件', module: '供应商管理', roles: ['ADMIN', 'BUYER'] },
  },
  {
    path: 'supplier-changes',
    name: 'SupplierChangeReview',
    component: () => import('../views/ChangeReviewView.vue'),
    meta: { title: '供应商审核', subtitle: '审核供应商信息变更', module: '供应商管理', roles: ['ADMIN', 'BUYER'] },
  },

  // ==================== 管理端 ====================
  {
    path: 'admin/cert-types',
    name: 'CertTypeManagement',
    component: () => import('../views/CertTypeManagementView.vue'),
    meta: { title: '证件类型字典', subtitle: '维护证件类型与差异化字段', module: '系统管理', roles: ['ADMIN'] },
  },

  // ==================== 供应商端 ====================
  {
    path: 'company-info',
    name: 'CompanyInfo',
    component: () => import('../views/SupplierProfileView.vue'),
    meta: { title: '企业信息', subtitle: '维护企业基础信息、银行信息、联系人和证件', module: '供应商管理', roles: ['SUPPLIER'] },
  },
  {
    path: 'contacts',
    name: 'ContactManagement',
    component: () => import('../views/SupplierContactsView.vue'),
    meta: { title: '联系人管理', subtitle: '管理企业联系人信息', module: '供应商管理', roles: ['SUPPLIER'] },
  },
];
