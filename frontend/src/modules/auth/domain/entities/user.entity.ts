/**
 * 用户实体 - 前端领域模型
 * 表示系统中的用户核心信息
 */
export interface User {
  id: number;
  name: string;
  phone: string;
  email: string;
  role: UserRole;
  status: UserStatus;
  isFirstLogin: boolean;
  createdAt: string;
}

/**
 * 用户角色枚举
 */
export type UserRole = 'ADMIN' | 'BUYER' | 'BUSINESS_USER' | 'SUPPLIER';

/**
 * 用户状态枚举
 */
export type UserStatus = 'ACTIVE' | 'DISABLED';

/**
 * 判断用户是否为管理员
 */
export function isAdmin(user: User): boolean {
  return user.role === 'ADMIN';
}

/**
 * 判断用户是否为采购员
 */
export function isBuyer(user: User): boolean {
  return user.role === 'BUYER';
}

/**
 * 判断用户是否为业务人员
 */
export function isBusinessUser(user: User): boolean {
  return user.role === 'BUSINESS_USER';
}

/**
 * 判断用户是否为供应商
 */
export function isSupplier(user: User): boolean {
  return user.role === 'SUPPLIER';
}

/**
 * 判断用户账号是否处于活跃状态
 */
export function isActive(user: User): boolean {
  return user.status === 'ACTIVE';
}
