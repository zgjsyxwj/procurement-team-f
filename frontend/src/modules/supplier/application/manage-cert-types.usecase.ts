import * as certTypeService from '../infrastructure/services/cert-type.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import type {
  CertTypeDto,
  CertTypeFieldDto,
  SaveCertTypeRequest,
} from '../types/dto/certificate.dto';
import type { CertificateTypeStatus } from '../types/vo/supplier-info.vo';

/**
 * 证件类型字典管理用例（管理端）。
 */

/** 证件类型列表 */
export async function fetchCertTypes(): Promise<CertTypeDto[]> {
  return certTypeService.getCertTypes();
}

/** 保存证件类型（id 为空=新增，否则=编辑） */
export async function executeSaveCertType(
  id: number | null,
  data: SaveCertTypeRequest,
): Promise<UseCaseResult> {
  if (!data.name?.trim()) {
    return { success: false, error: '请输入证件类型名称' };
  }
  const payload: SaveCertTypeRequest = { ...data, name: data.name.trim() };
  try {
    if (id == null) {
      await certTypeService.createCertType(payload);
    } else {
      await certTypeService.updateCertType(id, payload);
    }
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '保存证件类型失败，请稍后重试') };
  }
}

/** 停用/启用证件类型 */
export async function executeChangeCertTypeStatus(
  id: number,
  status: CertificateTypeStatus,
): Promise<UseCaseResult> {
  try {
    await certTypeService.changeCertTypeStatus(id, status);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '状态调整失败，请稍后重试') };
  }
}

/** 维护差异化字段（整体替换） */
export async function executeUpdateCertTypeFields(
  id: number,
  fields: CertTypeFieldDto[],
): Promise<UseCaseResult> {
  // 校验：字段标识与显示名必填
  for (const field of fields) {
    if (!field.fieldKey?.trim() || !field.fieldLabel?.trim()) {
      return { success: false, error: '差异化字段的字段标识与显示名不能为空' };
    }
  }
  try {
    await certTypeService.updateCertTypeFields(id, fields);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '字段维护失败，请稍后重试') };
  }
}
