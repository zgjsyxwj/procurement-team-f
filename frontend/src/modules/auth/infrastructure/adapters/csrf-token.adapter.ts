import type { InternalAxiosRequestConfig } from 'axios';

const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-CSRF-TOKEN';

/**
 * 从 Cookie 中读取 CSRF Token
 */
export function getCsrfToken(): string | null {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split('=');
    if (name === CSRF_COOKIE_NAME) {
      return decodeURIComponent(value);
    }
  }
  return null;
}

/**
 * Axios 请求拦截器：在 POST/PATCH/PUT/DELETE 请求头中自动添加 X-CSRF-TOKEN
 */
export function csrfRequestInterceptor(config: InternalAxiosRequestConfig): InternalAxiosRequestConfig {
  const method = config.method?.toUpperCase();
  if (method === 'POST' || method === 'PATCH' || method === 'PUT' || method === 'DELETE') {
    const csrfToken = getCsrfToken();
    if (csrfToken) {
      config.headers[CSRF_HEADER_NAME] = csrfToken;
    }
  }
  return config;
}
