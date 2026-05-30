import * as changeService from '../infrastructure/services/supplier-change.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import type { ChangeRequestDto } from '../types/dto/change.dto';

/**
 * 变更/证件审核用例（审核中心）。
 */

/** 待审核变更列表 */
export async function fetchPendingChanges(): Promise<ChangeRequestDto[]> {
  return changeService.getPendingChanges();
}

/** 变更详情（前后对比） */
export async function fetchChangeDetail(id: number): Promise<ChangeRequestDto> {
  return changeService.getChangeDetail(id);
}

/** 审核通过 */
export async function executeApproveChange(id: number): Promise<UseCaseResult> {
  try {
    await changeService.approveChange(id);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '审核通过失败，请稍后重试') };
  }
}

/** 审核驳回（需填原因） */
export async function executeRejectChange(id: number, reason: string): Promise<UseCaseResult> {
  if (!reason?.trim()) {
    return { success: false, error: '请填写驳回原因' };
  }
  try {
    await changeService.rejectChange(id, reason.trim());
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '审核驳回失败，请稍后重试') };
  }
}

/** 证件审核通过 */
export async function executeApproveCertificate(certId: number): Promise<UseCaseResult> {
  try {
    await changeService.approveCertificate(certId);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '证件审核通过失败，请稍后重试') };
  }
}

/** 证件审核驳回（需填原因） */
export async function executeRejectCertificate(
  certId: number,
  reason: string,
): Promise<UseCaseResult> {
  if (!reason?.trim()) {
    return { success: false, error: '请填写驳回原因' };
  }
  try {
    await changeService.rejectCertificate(certId, reason.trim());
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '证件审核驳回失败，请稍后重试') };
  }
}
