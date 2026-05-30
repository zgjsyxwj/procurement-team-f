import * as userManagementService from '../infrastructure/services/user-management.service';
import type {
  CreateUserRequest,
  UserListQuery,
  UserListResponse,
  AuditLogQuery,
  AuditLogResponse,
} from '../types/dto/user.dto';

/**
 * 用户管理操作结果
 */
export interface ManageUsersResult {
  success: boolean;
  error?: string;
}

/**
 * 创建用户表单验证错误
 */
export interface CreateUserValidationError {
  name?: string;
  phone?: string;
  email?: string;
  role?: string;
}

/**
 * 验证创建用户表单
 */
export function validateCreateUserForm(data: CreateUserRequest): CreateUserValidationError | null {
  const errors: CreateUserValidationError = {};

  if (!data.name.trim()) {
    errors.name = '请输入姓名';
  }

  if (!data.phone.trim()) {
    errors.phone = '请输入手机号';
  } else if (!/^1\d{10}$/.test(data.phone.trim())) {
    errors.phone = '请输入有效的手机号';
  }

  if (!data.email.trim()) {
    errors.email = '请输入邮箱';
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email.trim())) {
    errors.email = '请输入有效的邮箱地址';
  }

  if (!data.role) {
    errors.role = '请选择角色';
  }

  return Object.keys(errors).length > 0 ? errors : null;
}

/**
 * 执行创建用户
 */
export async function executeCreateUser(data: CreateUserRequest): Promise<ManageUsersResult> {
  const validationErrors = validateCreateUserForm(data);
  if (validationErrors) {
    const firstError =
      validationErrors.name ||
      validationErrors.phone ||
      validationErrors.email ||
      validationErrors.role ||
      '表单验证失败';
    return { success: false, error: firstError };
  }

  try {
    await userManagementService.createUser({
      ...data,
      name: data.name.trim(),
      phone: data.phone.trim(),
      email: data.email.trim(),
    });
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '创建用户失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 查询用户列表
 */
export async function fetchUserList(query: UserListQuery): Promise<UserListResponse> {
  return userManagementService.getUserList(query);
}

/**
 * 执行修改用户角色
 */
export async function executeUpdateUserRole(userId: number, role: string): Promise<ManageUsersResult> {
  try {
    await userManagementService.updateUserRole(userId, role);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '修改角色失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 执行修改用户状态（停用/启用）
 */
export async function executeUpdateUserStatus(userId: number, status: string): Promise<ManageUsersResult> {
  try {
    await userManagementService.updateUserStatus(userId, status);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '修改状态失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 执行重置用户密码
 */
export async function executeResetUserPassword(userId: number): Promise<ManageUsersResult> {
  try {
    await userManagementService.resetUserPassword(userId);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '重置密码失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 执行解锁用户
 */
export async function executeUnlockUser(userId: number): Promise<ManageUsersResult> {
  try {
    await userManagementService.unlockUser(userId);
    return { success: true };
  } catch (error: unknown) {
    const message = extractErrorMessage(error, '解锁用户失败，请稍后重试');
    return { success: false, error: message };
  }
}

/**
 * 查询审计日志
 */
export async function fetchAuditLogs(query: AuditLogQuery): Promise<AuditLogResponse> {
  return userManagementService.getAuditLogs(query);
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
