import * as supplierService from '../infrastructure/services/supplier.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import { validateBankAccounts } from '../domain/rules/bank-account-validation.rule';
import { validateBasicInfo } from './manage-supplier-info.usecase';
import type { SubmitChangeInput } from '../types/command/submit-change.command';
import type { ChangeRequestDto } from '../types/dto/change.dto';

/**
 * 供应商端提交信息变更 / 撤回用例。
 */

/**
 * 提交企业信息变更（合作中→待审核；待完善信息→保存草稿）。
 * 提交前做基本信息 + 银行三项联动校验；仅基本信息 changedFields 随后端接口持久化。
 */
export async function executeSubmitChange(input: SubmitChangeInput): Promise<UseCaseResult> {
  const basicError = validateBasicInfo(input.basicInfo);
  if (basicError) {
    const firstError =
      basicError.name ||
      basicError.registeredCapital ||
      basicError.annualRevenue ||
      basicError.employeeCount ||
      '表单验证失败';
    return { success: false, error: firstError };
  }

  const bankErrors = validateBankAccounts(input.bankAccounts);
  if (bankErrors.length > 0) {
    return { success: false, error: '银行信息填写不完整：户名、开户银行、银行账号须同时填写' };
  }

  try {
    await supplierService.updateMyProfile(input.basicInfo);
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '提交失败，请稍后重试') };
  }
}

/** 提交准入审核（待完善信息→待审核信息） */
export async function executeSubmitForReview(): Promise<UseCaseResult> {
  try {
    await supplierService.submitForReview();
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '提交审核失败，请稍后重试') };
  }
}

/** 查询当前待审核变更 */
export async function fetchPendingChange(): Promise<ChangeRequestDto | null> {
  return supplierService.getMyPendingChange();
}

/** 撤回待审核变更 */
export async function executeWithdraw(): Promise<UseCaseResult> {
  try {
    await supplierService.withdrawMyPendingChange();
    return { success: true };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '撤回失败，请稍后重试') };
  }
}
