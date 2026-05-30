/**
 * 供应商管理模块共享枚举与标签映射（值对象）。
 *
 * <p>枚举值与后端 `com.cdp.ecosaas.procurement.supplier.domain.model.*` 保持一致；
 * 标签为中文 UI 文案。
 */

// ==================== 供应商状态 ====================
export type SupplierStatus =
  | 'CREATED'
  | 'PENDING_ENTRY'
  | 'PENDING_INFO'
  | 'PENDING_REVIEW'
  | 'ACTIVE'
  | 'DISABLED';

export const SUPPLIER_STATUS_LABEL: Record<SupplierStatus, string> = {
  CREATED: '创建成功',
  PENDING_ENTRY: '待进入',
  PENDING_INFO: '待完善信息',
  PENDING_REVIEW: '待审核信息',
  ACTIVE: '合作中',
  DISABLED: '已停用',
};

export const SUPPLIER_STATUS_COLOR: Record<SupplierStatus, string> = {
  CREATED: 'default',
  PENDING_ENTRY: 'orange',
  PENDING_INFO: 'gold',
  PENDING_REVIEW: 'blue',
  ACTIVE: 'green',
  DISABLED: 'red',
};

// ==================== 供应商分类 ====================
export type SupplierCategory = 'DOMESTIC' | 'OVERSEAS';

export const SUPPLIER_CATEGORY_LABEL: Record<SupplierCategory, string> = {
  DOMESTIC: '国内',
  OVERSEAS: '国外',
};

// ==================== 变更类型 / 来源 / 状态 ====================
export type ChangeType = 'BASIC_INFO' | 'BANK';

export const CHANGE_TYPE_LABEL: Record<ChangeType, string> = {
  BASIC_INFO: '基本信息',
  BANK: '银行信息',
};

export type ChangeSource = 'SUPPLIER' | 'BUYER';

export const CHANGE_SOURCE_LABEL: Record<ChangeSource, string> = {
  SUPPLIER: '供应商提交',
  BUYER: '采购员编辑',
};

export type ChangeRequestStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN';

export const CHANGE_REQUEST_STATUS_LABEL: Record<ChangeRequestStatus, string> = {
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '驳回',
  WITHDRAWN: '已撤回',
};

export const CHANGE_REQUEST_STATUS_COLOR: Record<ChangeRequestStatus, string> = {
  PENDING_REVIEW: 'blue',
  APPROVED: 'green',
  REJECTED: 'red',
  WITHDRAWN: 'default',
};

// ==================== 证件审核状态 / 来源 ====================
export type CertificateAuditStatus = 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED';

export const CERT_AUDIT_STATUS_LABEL: Record<CertificateAuditStatus, string> = {
  PENDING_REVIEW: '待审核',
  APPROVED: '已通过',
  REJECTED: '驳回',
};

export const CERT_AUDIT_STATUS_COLOR: Record<CertificateAuditStatus, string> = {
  PENDING_REVIEW: 'blue',
  APPROVED: 'green',
  REJECTED: 'red',
};

export type CertificateSource = 'SUPPLIER_UPLOAD' | 'BUYER_MAINTAIN';

export const CERT_SOURCE_LABEL: Record<CertificateSource, string> = {
  SUPPLIER_UPLOAD: '供应商上传',
  BUYER_MAINTAIN: '采购员维护',
};

// ==================== 证件类型状态 ====================
export type CertificateTypeStatus = 'ACTIVE' | 'DISABLED';

export const CERT_TYPE_STATUS_LABEL: Record<CertificateTypeStatus, string> = {
  ACTIVE: '启用',
  DISABLED: '停用',
};

// ==================== 证件到期状态（派生，不落库） ====================
export type CertExpiryStatus = 'NORMAL' | 'EXPIRING_SOON' | 'EXPIRED';

export const CERT_EXPIRY_STATUS_LABEL: Record<CertExpiryStatus, string> = {
  NORMAL: '正常',
  EXPIRING_SOON: '即将到期',
  EXPIRED: '已过期',
};

export const CERT_EXPIRY_STATUS_COLOR: Record<CertExpiryStatus, string> = {
  NORMAL: 'green',
  EXPIRING_SOON: 'orange',
  EXPIRED: 'red',
};

// ==================== 证件差异化字段类型 ====================
export type CertFieldType = 'TEXT' | 'NUMBER' | 'DATE' | 'SELECT';
