/**
 * 从 Axios 错误对象中提取后端返回的业务错误消息（{code, message, detail}）。
 */
export function extractErrorMessage(error: unknown, defaultMessage: string): string {
  if (error && typeof error === 'object' && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return defaultMessage;
}

/** 用例统一结果类型 */
export interface UseCaseResult<T = void> {
  success: boolean;
  data?: T;
  error?: string;
}
