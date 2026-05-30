import { apiClient } from '@/shared/http/api-client';
import type {
  SupplierDto,
  SupplierListQuery,
  SupplierListResponse,
  DisableImpactDto,
  ChangeStatusRequest,
} from '../../types/dto/supplier.dto';
import type { CreateSupplierCommand } from '../../types/command/create-supplier.command';
import type { ChangeRequestDto, ChangeHistoryQuery } from '../../types/dto/change.dto';

/** 创建供应商响应（后端 CreateSupplierResponse） */
export interface CreateSupplierResponse {
  id: number;
  supplierCode: string;
}

// ==================== 供应商端（/api/supplier/**） ====================

/** 查看本企业信息（含银行信息、待审核变更标记） */
export async function getMyProfile(): Promise<SupplierDto> {
  const res = await apiClient.get<SupplierDto>('/api/supplier/profile');
  return res.data;
}

/**
 * 编辑企业信息（合作中→提交待审核；待完善信息→保存草稿）。
 * 后端 UpdateSupplierInfoRequest 仅含 changedFields（基本信息 key→值），后端按字段级 diff 记录变更。
 */
export async function updateMyProfile(changedFields: Record<string, string>): Promise<void> {
  await apiClient.put('/api/supplier/profile', { changedFields });
}

/** 提交准入审核（待完善信息→待审核信息） */
export async function submitForReview(): Promise<void> {
  await apiClient.post('/api/supplier/profile/submit-review');
}

/** 查询当前待审核变更（后端返回列表；同类至多一条待审核，取首条） */
export async function getMyPendingChange(): Promise<ChangeRequestDto | null> {
  const res = await apiClient.get<ChangeRequestDto[]>('/api/supplier/profile/pending-change');
  return res.data?.[0] ?? null;
}

/** 撤回待审核变更 */
export async function withdrawMyPendingChange(): Promise<void> {
  await apiClient.post('/api/supplier/profile/pending-change/withdraw');
}

// ==================== 采购端（/api/suppliers/**） ====================

/** 供应商列表（分页/名称模糊/状态/证件到期状态筛选） */
export async function getSupplierList(query: SupplierListQuery): Promise<SupplierListResponse> {
  const res = await apiClient.get<SupplierListResponse>('/api/suppliers', { params: query });
  return res.data;
}

/** 创建供应商（仅保存/保存并发送邀请） */
export async function createSupplier(command: CreateSupplierCommand): Promise<CreateSupplierResponse> {
  const res = await apiClient.post<CreateSupplierResponse>('/api/suppliers', command);
  return res.data;
}

/** 供应商详情 */
export async function getSupplierDetail(id: number): Promise<SupplierDto> {
  const res = await apiClient.get<SupplierDto>(`/api/suppliers/${id}`);
  return res.data;
}

/** 采购员直接编辑供应商信息（即时生效 + 记录变更） */
export async function updateSupplier(id: number, changedFields: Record<string, string>): Promise<void> {
  await apiClient.put(`/api/suppliers/${id}`, { changedFields });
}

/** 发送/重发邀请邮件 */
export async function inviteSupplier(id: number): Promise<void> {
  await apiClient.post(`/api/suppliers/${id}/invite`);
}

/** 停用前受影响事项清单 */
export async function getDisableImpact(id: number): Promise<DisableImpactDto> {
  const res = await apiClient.get<DisableImpactDto>(`/api/suppliers/${id}/disable-impact`);
  return res.data;
}

/** 调整状态（合作中/已停用，含操作备注） */
export async function changeSupplierStatus(id: number, request: ChangeStatusRequest): Promise<void> {
  await apiClient.patch(`/api/suppliers/${id}/status`, request);
}

/** 变更记录（时间倒序，按时间范围筛选） */
export async function getChangeHistory(
  id: number,
  query?: ChangeHistoryQuery,
): Promise<ChangeRequestDto[]> {
  const res = await apiClient.get<ChangeRequestDto[]>(`/api/suppliers/${id}/change-history`, {
    params: query,
  });
  return res.data;
}
