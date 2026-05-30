import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { getCsrfToken } from './csrf-token';

/**
 * 全局 Axios 实例（跨模块共享）。
 * <p>
 * 配置：
 * - withCredentials: true（自动携带 httpOnly Cookie）
 * - CSRF Token 拦截器：写操作自动添加 X-CSRF-TOKEN 请求头
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: '',
  withCredentials: true,
  timeout: 30000,
});

// 请求拦截器：写操作自动添加 CSRF Token
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

export { apiClient };
