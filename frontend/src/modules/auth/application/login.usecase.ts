import * as authService from '../infrastructure/services/auth.service';
import type { LoginResponse } from '../types/dto/login.dto';

/**
 * 登录表单验证错误
 */
export interface LoginValidationError {
  phone?: string;
  password?: string;
}

/**
 * 登录用例结果
 */
export interface LoginResult {
  success: boolean;
  data?: LoginResponse;
  error?: string;
}

/**
 * 验证登录表单
 * @returns 验证错误对象，无错误时返回 null
 */
export function validateLoginForm(phone: string, password: string): LoginValidationError | null {
  const errors: LoginValidationError = {};

  if (!phone.trim()) {
    errors.phone = '请输入手机号';
  } else if (!/^1\d{10}$/.test(phone.trim())) {
    errors.phone = '请输入有效的手机号';
  }

  if (!password) {
    errors.password = '请输入密码';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 执行内部用户登录
 * 流程：表单验证 → 调用 service → 处理响应
 */
export async function executeLogin(phone: string, password: string): Promise<LoginResult> {
  const validationErrors = validateLoginForm(phone, password);
  if (validationErrors) {
    const firstError = validationErrors.phone || validationErrors.password || '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    const response = await authService.login(phone.trim(), password);
    return { success: true, data: response };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '登录失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 执行供应商登录
 * 流程：表单验证 → 调用 service → 处理响应
 */
export async function executeSupplierLogin(phone: string, password: string): Promise<LoginResult> {
  const validationErrors = validateLoginForm(phone, password);
  if (validationErrors) {
    const firstError = validationErrors.phone || validationErrors.password || '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    const response = await authService.supplierLogin(phone.trim(), password);
    return { success: true, data: response };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '登录失败，请稍后重试');
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
    if (response?.status === 423) {
      return '账号已锁定，请30分钟后重试或联系管理员';
    }
    if (response?.status === 403) {
      return '账号已停用，请联系管理员';
    }
  }
  return defaultMessage;
}
