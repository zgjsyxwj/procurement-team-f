const CSRF_COOKIE_NAME = 'XSRF-TOKEN';

/**
 * 从 Cookie 中读取 CSRF Token（跨模块共享）。
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
