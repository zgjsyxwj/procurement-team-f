import * as supplierService from '../infrastructure/services/supplier.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import type {
  SupplierListQuery,
  SupplierListResponse,
  SupplierDto,
  DisableImpactDto,
  ChangeStatusRequest,
} from '../types/dto/supplier.dto';
import type { ChangeRequestDto, ChangeHistoryQuery } from '../types/dto/change.dto';

/** 查询供应商列表 */
export async function fetchSupplierList(query: SupplierListQuery): Promise<SupplierListResponse> {
  return supplierService.getSupplierList(query);
}

/** 查询供应商详情 */
export async function fetchSupplierDetail(id: number): Promise<SupplierDto> {
  return supplierService.getSupplierDetail(id);
}

/** 发送/重发邀请邮件 */
export async function executeInvite(id: number): Promise<UseCaseResult> {
  try {
    await supplierService.inviteSupplier(id);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '邀请发送失败，请稍后重试') };
  }
}

/** 查询停用前受影响事项 */
export async function fetchDisableImpact(id: number): Promise<DisableImpactDto> {
  return supplierService.getDisableImpact(id);
}

/** 调整供应商状态（合作中/已停用） */
export async function executeChangeStatus(
  id: number,
  request: ChangeStatusRequest,
): Promise<UseCaseResult> {
  try {
    await supplierService.changeSupplierStatus(id, request);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '状态调整失败，请稍后重试') };
  }
}

/** 查询变更记录（时间倒序） */
export async function fetchChangeHistory(
  id: number,
  query?: ChangeHistoryQuery,
): Promise<ChangeRequestDto[]> {
  return supplierService.getChangeHistory(id, query);
}
