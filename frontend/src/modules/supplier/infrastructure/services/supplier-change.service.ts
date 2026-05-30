import { apiClient } from '@/shared/http/api-client';
import type { ChangeRequestDto } from '../../types/dto/change.dto';

/**
 * 变更审核接口（采购/管理端，/api/supplier-changes/**）。
 */

/** 待审核变更列表（后端返回 ChangeRecordResponse 列表） */
export async function getPendingChanges(): Promise<ChangeRequestDto[]> {
  const res = await apiClient.get<ChangeRequestDto[]>('/api/supplier-changes');
  return res.data;
}

/** 变更详情（前后对比） */
export async function getChangeDetail(id: number): Promise<ChangeRequestDto> {
  const res = await apiClient.get<ChangeRequestDto>(`/api/supplier-changes/${id}`);
  return res.data;
}

/** 审核通过（变更生效） */
export async function approveChange(id: number): Promise<void> {
  await apiClient.post(`/api/supplier-changes/${id}/approve`);
}

/** 审核驳回（原因 + 通知供应商） */
export async function rejectChange(id: number, reason: string): Promise<void> {
  await apiClient.post(`/api/supplier-changes/${id}/reject`, { reason });
}

/** 证件审核通过 */
export async function approveCertificate(certId: number): Promise<void> {
  await apiClient.post(`/api/supplier-certificates/${certId}/approve`);
}

/** 证件审核驳回（原因 + 通知） */
export async function rejectCertificate(certId: number, reason: string): Promise<void> {
  await apiClient.post(`/api/supplier-certificates/${certId}/reject`, { reason });
}
