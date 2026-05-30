/**
 * 标准化后端错误响应格式。
 */
export interface ApiErrorResponse {
  error: string;
  message: string;
  timestamp?: string;
  violations?: string[];
}

/**
 * 从 Axios 错误中提取用户友好的错误消息（跨模块共享）。
 *
 * @param error Axios 错误对象
 * @param defaultMessage 默认错误消息
 * @returns 用户友好的错误消息
 */
export function extractErrorMessage(error: unknown, defaultMessage: string = '操作失败，请稍后重试'): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: ApiErrorResponse; status?: number } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
    // 根据 HTTP 状态码返回通用提示
    switch (response?.status) {
      case 401:
        return '认证已过期，请重新登录';
      case 403:
        return '权限不足';
      case 404:
        return '请求的资源不存在';
      case 423:
        return '账号已锁定';
      case 500:
        return '服务器内部错误，请稍后重试';
    }
  }
  return defaultMessage;
}
