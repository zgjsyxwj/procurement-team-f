import * as authService from '../infrastructure/services/auth.service';
import { validatePassword } from '../domain/rules/password-validation.rule';
import type { PasswordValidationResult } from '../domain/rules/password-validation.rule';

/**
 * 忘记密码用例结果
 */
export interface ForgotPasswordResult {
  success: boolean;
  error?: string;
}

/**
 * 重置密码用例结果
 */
export interface ResetPasswordResult {
  success: boolean;
  error?: string;
}

/**
 * 验证邮箱格式
 */
export function validateEmail(email: string): string | null {
  if (!email.trim()) {
    return '请输入邮箱地址';
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
    return '请输入有效的邮箱地址';
  }
  return null;
}

/**
 * 执行忘记密码 - 发送重置邮件
 * 流程：验证邮箱格式 → 调用 service
 * 无论邮箱是否存在，前端统一显示成功提示（安全考虑）
 */
export async function executeForgotPassword(email: string): Promise<ForgotPasswordResult> {
  const emailError = validateEmail(email);
  if (emailError) {
    return { success: false, error: emailError };
  }

  try {
    await authService.forgotPassword(email.trim());
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '操作失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 验证重置密码表单
 */
export interface ResetPasswordValidationError {
  newPassword?: string;
  confirmPassword?: string;
}

/**
 * 验证重置密码表单
 */
export function validateResetPasswordForm(
  newPassword: string,
  confirmPassword: string,
): ResetPasswordValidationError | null {
  const errors: ResetPasswordValidationError = {};

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

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 执行重置密码
 * 流程：验证表单 → 校验密码策略 → 调用 service
 */
export async function executeResetPassword(
  token: string,
  newPassword: string,
  confirmPassword: string,
): Promise<ResetPasswordResult> {
  if (!token) {
    return { success: false, error: '重置链接无效' };
  }

  const validationErrors = validateResetPasswordForm(newPassword, confirmPassword);
  if (validationErrors) {
    const firstError = validationErrors.newPassword || validationErrors.confirmPassword || '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    await authService.resetPassword(token, newPassword);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '重置密码失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 从错误对象中提取错误消息
 */
function extractErrorMessage(error: unknown, defaultMessage: string): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string }; status?: number } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
    if (response?.status === 410) {
      return '重置链接已过期或已使用，请重新申请';
    }
  }
  return defaultMessage;
}
