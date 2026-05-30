import { createRouter, createWebHistory } from 'vue-router';
import { authRoutes, createAuthGuard, setupSessionTimeoutInterceptor } from '../modules/auth/presentation/routes/auth.routes';
import { supplierRoutes } from '../modules/supplier/presentation/routes/supplier.routes';

const PlaceholderView = () => import('../views/placeholder/PlaceholderView.vue');

const routes = [
  // 公开路由（登录、密码重置等）
  ...authRoutes,

  // 主布局路由（需要认证）
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      // ==================== 工作台 ====================
      {
        path: '',
        redirect: '/dashboard',
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: PlaceholderView,
        meta: { title: '工作台', subtitle: '待办事项、快捷入口和数据概览', module: '工作台' },
      },

      // ==================== 供应商管理（模块02） ====================
      // 采购端列表/创建/详情、变更审核中心、证件类型字典、供应商端企业信息/联系人
      ...supplierRoutes,

      // ==================== 采购申请单（模块03） ====================
      {
        path: 'purchase-requests',
        name: 'PurchaseRequestList',
        component: PlaceholderView,
        meta: { title: '采购申请单', subtitle: '查看已审批通过并分配的采购申请单', module: '采购申请单', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'purchase-requests/:id',
        name: 'PurchaseRequestDetail',
        component: PlaceholderView,
        meta: { title: 'PR详情', subtitle: '查看采购申请单详情及下游进度', module: '采购申请单', roles: ['ADMIN', 'BUYER'] },
      },
      // 业务人员的采购申请
      {
        path: 'my-purchase-requests',
        name: 'MyPurchaseRequests',
        component: PlaceholderView,
        meta: { title: '我的采购申请', subtitle: '查看本人发起的采购申请单', module: '采购申请单', roles: ['BUSINESS_USER'] },
      },
      {
        path: 'my-purchase-requests/create',
        name: 'CreatePurchaseRequest',
        component: PlaceholderView,
        meta: { title: '创建采购申请', subtitle: '填写采购需求并提交审批', module: '采购申请单', roles: ['BUSINESS_USER'] },
      },

      // ==================== 询报价管理（模块04） ====================
      {
        path: 'rfqs',
        name: 'RfqList',
        component: PlaceholderView,
        meta: { title: '询价单管理', subtitle: '查看和管理询价单', module: '询报价管理', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'rfqs/create',
        name: 'RfqCreate',
        component: PlaceholderView,
        meta: { title: '创建询价单', subtitle: '从采购申请单创建询价单', module: '询报价管理', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'rfqs/:id',
        name: 'RfqDetail',
        component: PlaceholderView,
        meta: { title: '询价单详情', subtitle: '查看询价单详情、报价和核价', module: '询报价管理', roles: ['ADMIN', 'BUYER'] },
      },
      // 供应商的询价单
      {
        path: 'my-rfqs',
        name: 'SupplierRfqList',
        component: PlaceholderView,
        meta: { title: '我的询价单', subtitle: '查看收到的询价单并提交报价', module: '询报价管理', roles: ['SUPPLIER'] },
      },

      // ==================== 合同管理（模块05） ====================
      {
        path: 'contracts',
        name: 'ContractList',
        component: PlaceholderView,
        meta: { title: '合同管理', subtitle: '查看和管理采购合同', module: '合同管理', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'contracts/create',
        name: 'ContractCreate',
        component: PlaceholderView,
        meta: { title: '创建合同', subtitle: '从已完成询价单创建合同', module: '合同管理', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'contracts/:id',
        name: 'ContractDetail',
        component: PlaceholderView,
        meta: { title: '合同详情', subtitle: '查看合同详情、审批和签署状态', module: '合同管理', roles: ['ADMIN', 'BUYER'] },
      },
      // 业务人员的合同
      {
        path: 'my-contracts',
        name: 'MyContracts',
        component: PlaceholderView,
        meta: { title: '我的合同', subtitle: '查看本人PR关联的合同', module: '合同管理', roles: ['BUSINESS_USER'] },
      },
      // 供应商的合同
      {
        path: 'supplier-contracts',
        name: 'SupplierContracts',
        component: PlaceholderView,
        meta: { title: '合同管理', subtitle: '查看待签署和已签署合同', module: '合同管理', roles: ['SUPPLIER'] },
      },

      // ==================== 履约与付款（模块06） ====================
      {
        path: 'pos',
        name: 'PoList',
        component: PlaceholderView,
        meta: { title: '履约与付款', subtitle: '查看PO和付款状态', module: '履约与付款', roles: ['ADMIN', 'BUYER'] },
      },
      {
        path: 'pos/:id',
        name: 'PoDetail',
        component: PlaceholderView,
        meta: { title: 'PO详情', subtitle: '查看PO详情和付款进度', module: '履约与付款', roles: ['ADMIN', 'BUYER'] },
      },
      // 业务人员的PO
      {
        path: 'my-pos',
        name: 'MyPos',
        component: PlaceholderView,
        meta: { title: '我的PO', subtitle: '查看本人发起的PO', module: '履约与付款', roles: ['BUSINESS_USER'] },
      },

      // ==================== 审批中心（模块07） ====================
      {
        path: 'approval',
        name: 'ApprovalCenter',
        component: PlaceholderView,
        meta: { title: '审批中心', subtitle: '处理待审批事项', module: '审批中心', roles: ['ADMIN', 'BUYER'] },
      },

      // ==================== 报表（模块07） ====================
      {
        path: 'reports',
        name: 'Reports',
        component: PlaceholderView,
        meta: { title: '采购分析', subtitle: '支出分析、流程效率、供应商评估', module: '报表分析', roles: ['ADMIN'] },
      },

      // ==================== 系统管理（模块01 + 07） ====================
      {
        path: 'admin/users',
        name: 'UserManagement',
        component: () => import('../modules/auth/presentation/views/UserManagementView.vue'),
        meta: { title: '账号管理', module: '系统管理', roles: ['ADMIN'] },
      },
      {
        path: 'admin/audit-logs',
        name: 'AuditLogs',
        component: () => import('../modules/auth/presentation/views/AuditLogView.vue'),
        meta: { title: '审计日志', module: '系统管理', roles: ['ADMIN'] },
      },
      {
        path: 'settings/fields',
        name: 'FieldSettings',
        component: PlaceholderView,
        meta: { title: '字段设置', subtitle: '管理表单字段库配置', module: '系统管理', roles: ['ADMIN'] },
      },
      {
        path: 'settings/forms',
        name: 'FormSettings',
        component: PlaceholderView,
        meta: { title: '表单设置', subtitle: '管理表单模板配置', module: '系统管理', roles: ['ADMIN'] },
      },

      // ==================== 邮件日志 ====================
      {
        path: 'email-logs',
        name: 'EmailLogs',
        component: PlaceholderView,
        meta: { title: '邮件日志', subtitle: '查看系统邮件发送记录', module: '系统管理', roles: ['ADMIN', 'BUYER'] },
      },

      // 供应商端路由（企业信息/联系人）已并入上方 supplierRoutes
    ],
  },

  // 兜底重定向
  {
    path: '/:pathMatch(.*)*',
    redirect: '/internal/login',
  },
];

export const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 安装认证守卫
createAuthGuard(router);

// 安装会话超时拦截器
setupSessionTimeoutInterceptor(router);
