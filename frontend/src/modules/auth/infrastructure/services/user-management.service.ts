import axios from 'axios';
import type { CreateUserRequest, UserListQuery, UserListResponse, AuditLogQuery, AuditLogResponse } from '../../types/dto/user.dto';
import { getCsrfToken } from '../adapters/csrf-token.adapter';

const apiClient = axios.create({
  baseURL: '',
  withCredentials: true,
});

// 在写操作请求头中自动添加 CSRF Token
apiClient.interceptors.request.use((config) => {
  const method = config.method?.toUpperCase();
  if (method === 'POST' || method === 'PATCH' || method === 'PUT' || method === 'DELETE') {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      config.headers['X-CSRF-TOKEN'] = csrfToken;
    }
  }
  return config;
});

/**
 * 查询内部用户列表
 */
export async function getUserList(query: UserListQuery): Promise<UserListResponse> {
  const response = await apiClient.get<UserListResponse>('/api/admin/users', {
    params: query,
  });
  return response.data;
}

/**
 * 创建内部用户
 */
export async function createUser(data: CreateUserRequest): Promise<void> {
  await apiClient.post('/api/admin/users', data);
}

/**
 * 修改用户角色
 */
export async function updateUserRole(id: number, role: string): Promise<void> {
  await apiClient.patch(`/api/admin/users/${id}/role`, { role });
}

/**
 * 修改用户状态（停用/启用）
 */
export async function updateUserStatus(id: number, status: string): Promise<void> {
  await apiClient.patch(`/api/admin/users/${id}/status`, { status });
}

/**
 * 重置用户密码
 */
export async function resetUserPassword(id: number): Promise<void> {
  await apiClient.post(`/api/admin/users/${id}/reset-password`);
}

/**
 * 手动解锁用户
 */
export async function unlockUser(id: number): Promise<void> {
  await apiClient.post(`/api/admin/users/${id}/unlock`);
}

/**
 * 查询审计日志
 */
export async function getAuditLogs(query: AuditLogQuery): Promise<AuditLogResponse> {
  const response = await apiClient.get<AuditLogResponse>('/api/admin/audit-logs', {
    params: query,
  });
  return response.data;
}
