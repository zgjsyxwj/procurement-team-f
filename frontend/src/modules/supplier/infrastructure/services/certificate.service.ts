import { apiClient } from '@/shared/http/api-client';
import type { CertificateDto, UploadCertificateRequest } from '../../types/dto/certificate.dto';

/**
 * 证件接口（双端）。
 * <p>
 * 供应商端 `/api/supplier/certificates`（上传→待审核）；
 * 采购端 `/api/suppliers/{id}/certificates`（手动添加→直接已通过）。
 */

/** 本企业证件列表（供应商端） */
export async function getMyCertificates(): Promise<CertificateDto[]> {
  const res = await apiClient.get<CertificateDto[]>('/api/supplier/certificates');
  return res.data;
}

/** 供应商上传/更新证件（multipart：file + 表单字段） */
export async function uploadCertificate(
  file: File,
  request: UploadCertificateRequest,
): Promise<void> {
  const form = new FormData();
  form.append('file', file);
  form.append('certTypeId', String(request.certTypeId));
  form.append('validFrom', request.validFrom);
  form.append('validTo', request.validTo);
  if (request.extraFields) {
    form.append('extraFields', JSON.stringify(request.extraFields));
  }
  await apiClient.post('/api/supplier/certificates', form);
}

/** 证件列表（采购端） */
export async function getSupplierCertificates(supplierId: number): Promise<CertificateDto[]> {
  const res = await apiClient.get<CertificateDto[]>(`/api/suppliers/${supplierId}/certificates`);
  return res.data;
}

/** 采购员手动添加证件（直接已通过） */
export async function addCertificateByBuyer(
  supplierId: number,
  file: File,
  request: UploadCertificateRequest,
): Promise<void> {
  const form = new FormData();
  form.append('file', file);
  form.append('certTypeId', String(request.certTypeId));
  form.append('validFrom', request.validFrom);
  form.append('validTo', request.validTo);
  if (request.extraFields) {
    form.append('extraFields', JSON.stringify(request.extraFields));
  }
  await apiClient.post(`/api/suppliers/${supplierId}/certificates`, form);
}
