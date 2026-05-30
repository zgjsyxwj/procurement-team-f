import { apiClient } from '@/shared/http/api-client';
import type {
  CertTypeDto,
  CertTypeFieldDto,
  SaveCertTypeRequest,
} from '../../types/dto/certificate.dto';
import type { CertificateTypeStatus } from '../../types/vo/supplier-info.vo';

/**
 * 证件类型字典接口。
 * <p>
 * 管理端走 `/api/admin/cert-types`（增删改/停用/字段维护）；
 * 供应商端走 `/api/supplier/cert-types`（仅启用类型，用于上传时选择）。
 */

/** 可选证件类型（供应商端，仅启用 + 差异化字段） */
export async function getSelectableCertTypes(): Promise<CertTypeDto[]> {
  const res = await apiClient.get<CertTypeDto[]>('/api/supplier/cert-types');
  return res.data;
}

/** 证件类型列表（管理端） */
export async function getCertTypes(): Promise<CertTypeDto[]> {
  const res = await apiClient.get<CertTypeDto[]>('/api/admin/cert-types');
  return res.data;
}

/** 新增证件类型（名称唯一） */
export async function createCertType(data: SaveCertTypeRequest): Promise<void> {
  await apiClient.post('/api/admin/cert-types', data);
}

/** 编辑证件类型 */
export async function updateCertType(id: number, data: SaveCertTypeRequest): Promise<void> {
  await apiClient.put(`/api/admin/cert-types/${id}`, data);
}

/** 停用/启用证件类型（后端 CertTypeStatusRequest 仅含 active 布尔） */
export async function changeCertTypeStatus(
  id: number,
  status: CertificateTypeStatus,
): Promise<void> {
  await apiClient.patch(`/api/admin/cert-types/${id}/status`, { active: status === 'ACTIVE' });
}

/** 维护差异化字段（整体替换） */
export async function updateCertTypeFields(
  id: number,
  fields: CertTypeFieldDto[],
): Promise<void> {
  await apiClient.put(`/api/admin/cert-types/${id}/fields`, { fields });
}
