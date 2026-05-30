/**
 * 登录请求 DTO
 */
export interface LoginRequest {
  phone: string;
  password: string;
}

/**
 * 登录响应 DTO
 * 与后端 LoginResponse record 对应: { id, name, role, isFirstLogin }
 */
export interface LoginResponse {
  id: number;
  name: string;
  role: string;
  isFirstLogin: boolean;
}
