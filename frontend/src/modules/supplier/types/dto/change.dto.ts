import type {
  ChangeType,
  ChangeSource,
  ChangeRequestStatus,
} from '../vo/supplier-info.vo';

/**
 * 变更字段明细 DTO（前后值对比，Req 5.2、50.2）。
 */
export interface ChangeFieldDto {
  fieldKey: string;
  fieldLabel: string;
  beforeValue?: string | null;
  afterValue?: string | null;
}

/**
 * 变更申请/记录 DTO（对齐后端 `supplier_change_request` 表）。
 */
export interface ChangeRequestDto {
  id: number;
  supplierId: number;
  supplierName?: string | null;
  changeType: ChangeType;
  source: ChangeSource;
  status: ChangeRequestStatus;
  submitterId: number;
  submitterName: string;
  submittedAt: string;
  reviewerId?: number | null;
  reviewerName?: string | null;
  reviewedAt?: string | null;
  reviewComment?: string | null;
  fields: ChangeFieldDto[];
}

/**
 * 变更历史查询参数（Req 50.3）。
 */
export interface ChangeHistoryQuery {
  startTime?: string;
  endTime?: string;
}
