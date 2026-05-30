import * as authService from '../infrastructure/services/auth.service';
import { validatePassword } from '../domain/rules/password-validation.rule';
import type { PasswordValidationResult } from '../domain/rules/password-validation.rule';

/**
 * 修改密码表单验证错误
 */
export interface ChangePasswordValidationError {
  oldPassword?: string;
  newPassword?: string;
  confirmPassword?: string;
}

/**
 * 修改密码用例结果
 */
export interface ChangePasswordResult {
  success: boolean;
  error?: string;
}

/**
 * 验证修改密码表单
 * @returns 验证错误对象，无错误时返回 null
 */
export function validateChangePasswordForm(
  oldPassword: string,
  newPassword: string,
  confirmPassword: string,
): ChangePasswordValidationError | null {
  const errors: ChangePasswordValidationError = {};

  if (!oldPassword) {
    errors.oldPassword = '请输入旧密码';
  }

  if (!newPassword) {
    errors.newPassword = '请输入新密码';
  } else {
    const validation: PasswordValidationResult = validatePassword(newPassword);
    if (!validation.isValid) {
      errors.newPassword = validation.errors[0];
    }
  }

  if (!confirmPassword) {
    errors.confirmPassword = '请确认新密码';
  } else if (newPassword && confirmPassword !== newPassword) {
    errors.confirmPassword = '两次输入的密码不一致';
  }

  if (oldPassword && newPassword && oldPassword === newPassword) {
    errors.newPassword = '新密码不能与旧密码相同';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 执行修改密码
 * 流程：前端策略校验 → 调用 service
 */
export async function executeChangePassword(
  oldPassword: string,
  newPassword: string,
  confirmPassword: string,
): Promise<ChangePasswordResult> {
  const validationErrors = validateChangePasswordForm(oldPassword, newPassword, confirmPassword);
  if (validationErrors) {
    const firstError =
      validationErrors.oldPassword ||
      validationErrors.newPassword ||
      validationErrors.confirmPassword ||
      '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    await authService.changePassword(oldPassword, newPassword);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '修改密码失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 从错误对象中提取错误消息
 */
function extractErrorMessage(error: unknown, defaultMessage: string): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return defaultMessage;
}
