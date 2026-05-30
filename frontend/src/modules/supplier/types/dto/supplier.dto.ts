import type {
  SupplierStatus,
  SupplierCategory,
  CertExpiryStatus,
} from '../vo/supplier-info.vo';

/**
 * 银行账号 DTO（户名/开户银行名称/银行账号；一旦填写三项必填）。
 */
export interface BankAccountDto {
  id?: number;
  accountName: string;
  bankName: string;
  accountNumber: string;
}

/**
 * 供应商企业信息 DTO（含银行信息）。字段对齐后端 `supplier` 表。
 */
export interface SupplierDto {
  id: number;
  supplierCode: string;
  name: string;
  category: SupplierCategory;
  status: SupplierStatus;
  unifiedSocialCreditCode?: string | null;
  legalPerson?: string | null;
  registeredDate?: string | null;
  registeredCapital?: number | null;
  address?: string | null;
  generalTaxpayer?: boolean | null;
  businessScope?: string | null;
  enterpriseNature?: string | null;
  salesMode?: string | null;
  coverageArea?: string | null;
  annualRevenue?: number | null;
  employeeCount?: number | null;
  mainCustomers?: string | null;
  bankAccounts: BankAccountDto[];
  /** 是否存在待审核变更（供应商端用于标记/撤回入口） */
  hasPendingChange?: boolean;
  createdAt?: string;
  updatedAt?: string;
  version?: number;
}

/**
 * 供应商列表项 DTO（Req 8.1）。
 */
export interface SupplierListItemDto {
  id: number;
  supplierCode: string;
  name: string;
  unifiedSocialCreditCode?: string | null;
  status: SupplierStatus;
  primaryContactName?: string | null;
  primaryContactPhone?: string | null;
  certExpiryStatus: CertExpiryStatus;
}

/**
 * 供应商列表查询参数（Req 8.1-8.5）。
 */
export interface SupplierListQuery {
  nameKeyword?: string;
  status?: SupplierStatus;
  certExpiryStatus?: CertExpiryStatus;
  page: number;
  size: number;
}

/**
 * 分页响应（对齐后端 PageResult / Spring Page）。
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export type SupplierListResponse = PageResponse<SupplierListItemDto>;

/**
 * 停用前受影响事项清单 DTO（Req 7.12）。
 */
export interface DisableImpactDto {
  hasImpact: boolean;
  affectedItems: string[];
}

/**
 * 状态调整请求 DTO（Req 7.7-7.11）。
 */
export interface ChangeStatusRequest {
  targetStatus: Extract<SupplierStatus, 'ACTIVE' | 'DISABLED'>;
  remark?: string;
}
