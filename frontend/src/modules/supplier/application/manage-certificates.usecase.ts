import * as certificateService from '../infrastructure/services/certificate.service';
import * as certTypeService from '../infrastructure/services/cert-type.service';
import { validateCertificateFile } from '../infrastructure/adapters/oss-upload.adapter';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import type {
  CertificateDto,
  CertTypeDto,
  UploadCertificateRequest,
} from '../types/dto/certificate.dto';

/**
 * 证件管理用例（上传 / 列表 / 采购员手动添加）。
 */

/** 可选证件类型（供应商端，仅启用） */
export async function fetchSelectableCertTypes(): Promise<CertTypeDto[]> {
  return certTypeService.getSelectableCertTypes();
}

/** 本企业证件列表（供应商端） */
export async function fetchMyCertificates(): Promise<CertificateDto[]> {
  return certificateService.getMyCertificates();
}

/** 供应商证件列表（采购端） */
export async function fetchSupplierCertificates(supplierId: number): Promise<CertificateDto[]> {
  return certificateService.getSupplierCertificates(supplierId);
}

/** 校验有效期（截止须晚于起始） */
function validateValidity(request: UploadCertificateRequest): string | null {
  if (!request.certTypeId) {
    return '请选择证件类型';
  }
  if (!request.validFrom || !request.validTo) {
    return '请选择有效期';
  }
  if (new Date(request.validTo) <= new Date(request.validFrom)) {
    return '有效期截止须晚于起始';
  }
  return null;
}

/** 供应商上传证件（→待审核） */
export async function executeUploadCertificate(
  file: File | null,
  request: UploadCertificateRequest,
): Promise<UseCaseResult> {
  if (!file) {
    return { success: false, error: '请选择要上传的文件' };
  }
  const fileError = validateCertificateFile(file);
  if (!fileError.valid) {
    return { success: false, error: fileError.error };
  }
  const validityError = validateValidity(request);
  if (validityError) {
    return { success: false, error: validityError };
  }

  try {
    await certificateService.uploadCertificate(file, request);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '证件上传失败，请稍后重试') };
  }
}

/** 采购员手动添加证件（→直接已通过） */
export async function executeAddCertificateByBuyer(
  supplierId: number,
  file: File | null,
  request: UploadCertificateRequest,
): Promise<UseCaseResult> {
  if (!file) {
    return { success: false, error: '请选择要上传的文件' };
  }
  const fileError = validateCertificateFile(file);
  if (!fileError.valid) {
    return { success: false, error: fileError.error };
  }
  const validityError = validateValidity(request);
  if (validityError) {
    return { success: false, error: validityError };
  }

  try {
    await certificateService.addCertificateByBuyer(supplierId, file, request);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '添加证件失败，请稍后重试') };
  }
}
