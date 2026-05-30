/**
 * 当前用户信息值对象
 */
export interface UserInfo {
  id: number;
  name: string;
  role: 'ADMIN' | 'BUYER' | 'BUSINESS_USER' | 'SUPPLIER';
  isFirstLogin: boolean;
}
