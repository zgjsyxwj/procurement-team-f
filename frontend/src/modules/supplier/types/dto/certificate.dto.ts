import type {
  CertificateAuditStatus,
  CertificateSource,
  CertificateTypeStatus,
  CertExpiryStatus,
  CertFieldType,
} from '../vo/supplier-info.vo';

/**
 * 证件类型差异化字段定义 DTO（Req 11.5）。
 */
export interface CertTypeFieldDto {
  id?: number;
  fieldKey: string;
  fieldLabel: string;
  fieldType: CertFieldType;
  required: boolean;
  sortOrder: number;
}

/**
 * 证件类型 DTO（Req 11.1-11.5）。
 */
export interface CertTypeDto {
  id: number;
  name: string;
  status: CertificateTypeStatus;
  remark?: string | null;
  fields: CertTypeFieldDto[];
}

/**
 * 新增/编辑证件类型请求 DTO。
 */
export interface SaveCertTypeRequest {
  name: string;
  remark?: string;
}

/**
 * 证件 DTO（对齐后端 `supplier_certificate` 表 + 派生到期状态）。
 */
export interface CertificateDto {
  id: number;
  supplierId?: number;
  certTypeId: number;
  certTypeName?: string | null;
  fileUrl: string;
  fileName: string;
  validFrom: string;
  validTo: string;
  auditStatus: CertificateAuditStatus;
  rejectReason?: string | null;
  source: CertificateSource;
  currentValid: boolean;
  extraFields?: Record<string, unknown> | null;
  maintainedBy?: string | null;
  /** 派生到期状态（查询时标注，Req 12.6） */
  expiryStatus?: CertExpiryStatus;
  createdAt?: string;
}

/**
 * 上传/更新证件请求 DTO（multipart 表单字段，文件单独以 file 提交）。
 */
export interface UploadCertificateRequest {
  certTypeId: number;
  validFrom: string;
  validTo: string;
  extraFields?: Record<string, unknown>;
}
