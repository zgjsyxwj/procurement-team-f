import axios from 'axios';
import type { LoginRequest, LoginResponse } from '../../types/dto/login.dto';
import type { ChangePasswordCommand, ForgotPasswordCommand, ResetPasswordCommand } from '../../types/command/change-password.command';
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
 * 内部用户手机号密码登录
 */
export async function login(phone: string, password: string): Promise<LoginResponse> {
  const request: LoginRequest = { phone, password };
  const response = await apiClient.post<LoginResponse>('/api/internal/auth/login', request);
  return response.data;
}

/**
 * 供应商手机号密码登录
 */
export async function supplierLogin(phone: string, password: string): Promise<LoginResponse> {
  const request: LoginRequest = { phone, password };
  const response = await apiClient.post<LoginResponse>('/api/supplier/auth/login', request);
  return response.data;
}

/**
 * 登出
 */
export async function logout(): Promise<void> {
  await apiClient.post('/api/auth/logout');
}

/**
 * 修改密码
 */
export async function changePassword(oldPassword: string, newPassword: string): Promise<void> {
  const command: ChangePasswordCommand = { oldPassword, newPassword };
  await apiClient.post('/api/auth/change-password', command);
}

/**
 * 忘记密码 - 发送重置邮件
 */
export async function forgotPassword(email: string): Promise<void> {
  const command: ForgotPasswordCommand = { email };
  await apiClient.post('/api/auth/forgot-password', command);
}

/**
 * 重置密码 - 通过 Token 重置
 */
export async function resetPassword(token: string, newPassword: string): Promise<void> {
  const command: ResetPasswordCommand = { token, newPassword };
  await apiClient.post('/api/auth/reset-password', command);
}
