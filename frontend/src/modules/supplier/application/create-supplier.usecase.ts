import * as supplierService from '../infrastructure/services/supplier.service';
import type { CreateSupplierResponse } from '../infrastructure/services/supplier.service';
import { extractErrorMessage, type UseCaseResult } from './extract-error';
import type { CreateSupplierCommand } from '../types/command/create-supplier.command';

const PHONE_PATTERN = /^[+]?[\d\s-]{6,20}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export interface CreateSupplierValidationError {
  name?: string;
  category?: string;
  contactName?: string;
  contactPhone?: string;
  contactEmail?: string;
}

/**
 * 校验创建供应商表单（企业名称、分类、主要联系人）。
 */
export function validateCreateSupplierForm(
  command: CreateSupplierCommand,
): CreateSupplierValidationError | null {
  const errors: CreateSupplierValidationError = {};

  if (!command.name?.trim()) {
    errors.name = '请输入企业名称';
  }
  if (!command.category) {
    errors.category = '请选择供应商分类';
  }
  if (!command.contactName?.trim()) {
    errors.contactName = '请输入主要联系人姓名';
  }
  if (!command.contactPhone?.trim()) {
    errors.contactPhone = '请输入主要联系人手机号';
  } else if (!PHONE_PATTERN.test(command.contactPhone.trim())) {
    errors.contactPhone = '请输入有效的手机号';
  }
  if (!command.contactEmail?.trim()) {
    errors.contactEmail = '请输入主要联系人邮箱';
  } else if (!EMAIL_PATTERN.test(command.contactEmail.trim())) {
    errors.contactEmail = '请输入有效的邮箱地址';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 执行创建供应商（含「保存」/「保存并发送邀请」）。
 */
export async function executeCreateSupplier(
  command: CreateSupplierCommand,
): Promise<UseCaseResult<CreateSupplierResponse>> {
  const validationErrors = validateCreateSupplierForm(command);
  if (validationErrors) {
    const firstError =
      validationErrors.name ||
      validationErrors.category ||
      validationErrors.contactName ||
      validationErrors.contactPhone ||
      validationErrors.contactEmail ||
      '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    const supplier = await supplierService.createSupplier({
      ...command,
      name: command.name.trim(),
      contactName: command.contactName.trim(),
      contactPhone: command.contactPhone.trim(),
      contactEmail: command.contactEmail.trim(),
    });
    return { success: true, data: supplier };
  } catch (error) {
    return { success: false, error: extractErrorMessage(error, '创建供应商失败，请稍后重试') };
  }
}
