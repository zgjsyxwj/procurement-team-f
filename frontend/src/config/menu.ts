import { h } from 'vue';
import {
  DashboardOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  ShoppingOutlined,
  AuditOutlined,
  DollarOutlined,
  BarChartOutlined,
  SettingOutlined,
  MailOutlined,
  UserOutlined,
  PlusCircleOutlined,
} from '@ant-design/icons-vue';

export interface MenuItem {
  key: string;
  label: string;
  icon?: ReturnType<typeof h>;
  children?: MenuItem[];
}

/**
 * 根据用户角色返回对应的菜单配置。
 * 菜单 key 即为路由 path，点击后直接 router.push(key)。
 */
export function getMenuItems(role: string): MenuItem[] {
  switch (role) {
    case 'ADMIN':
      return adminMenu;
    case 'BUYER':
      return buyerMenu;
    case 'BUSINESS_USER':
      return businessMenu;
    case 'SUPPLIER':
      return supplierMenu;
    default:
      return [];
  }
}

// ==================== 采购经理菜单 ====================
const adminMenu: MenuItem[] = [
  {
    key: '/dashboard',
    label: '经理工作台',
    icon: h(DashboardOutlined),
  },
  {
    key: '/approval',
    label: '审批中心',
    icon: h(CheckCircleOutlined),
  },
  {
    key: '/purchase-requests',
    label: '采购申请单',
    icon: h(FileTextOutlined),
  },
  {
    key: '/rfqs',
    label: '询价单查询',
    icon: h(ShoppingOutlined),
  },
  {
    key: '/contracts',
    label: '合同管理',
    icon: h(AuditOutlined),
  },
  {
    key: '/pos',
    label: '履约与付款',
    icon: h(DollarOutlined),
  },
  {
    key: '/reports',
    label: '采购分析',
    icon: h(BarChartOutlined),
  },
  {
    key: '/settings',
    label: '系统管理',
    icon: h(SettingOutlined),
    children: [
      { key: '/admin/users', label: '账号管理' },
      { key: '/admin/audit-logs', label: '审计日志' },
      { key: '/settings/fields', label: '字段设置' },
      { key: '/settings/forms', label: '表单设置' },
    ],
  },
  {
    key: '/email-logs',
    label: '邮件日志',
    icon: h(MailOutlined),
  },
];

// ==================== 采购员菜单 ====================
const buyerMenu: MenuItem[] = [
  {
    key: '/dashboard',
    label: '工作台',
    icon: h(DashboardOutlined),
  },
  {
    key: '/suppliers',
    label: '供应商管理',
    icon: h(TeamOutlined),
  },
  {
    key: '/approval',
    label: '审批中心',
    icon: h(CheckCircleOutlined),
  },
  {
    key: '/purchase-requests',
    label: '采购申请单',
    icon: h(FileTextOutlined),
  },
  {
    key: '/rfqs',
    label: '询价单管理',
    icon: h(ShoppingOutlined),
  },
  {
    key: '/contracts',
    label: '合同管理',
    icon: h(AuditOutlined),
  },
  {
    key: '/pos',
    label: '履约与付款',
    icon: h(DollarOutlined),
  },
  {
    key: '/email-logs',
    label: '邮件日志',
    icon: h(MailOutlined),
  },
];

// ==================== 业务人员菜单 ====================
const businessMenu: MenuItem[] = [
  {
    key: '/my-purchase-requests',
    label: '我的采购申请',
    icon: h(FileTextOutlined),
  },
  {
    key: '/my-purchase-requests/create',
    label: '创建采购申请',
    icon: h(PlusCircleOutlined),
  },
  {
    key: '/my-contracts',
    label: '我的合同',
    icon: h(AuditOutlined),
  },
  {
    key: '/my-pos',
    label: '我的PO',
    icon: h(DollarOutlined),
  },
];

// ==================== 供应商菜单 ====================
const supplierMenu: MenuItem[] = [
  {
    key: '/company-info',
    label: '企业信息',
    icon: h(TeamOutlined),
  },
  {
    key: '/contacts',
    label: '联系人管理',
    icon: h(UserOutlined),
  },
  {
    key: '/my-rfqs',
    label: '我的询价单',
    icon: h(ShoppingOutlined),
  },
  {
    key: '/supplier-contracts',
    label: '合同管理',
    icon: h(AuditOutlined),
  },
];
