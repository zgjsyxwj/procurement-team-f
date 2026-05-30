/**
 * 创建用户请求 DTO
 */
export interface CreateUserRequest {
  name: string;
  phone: string;
  email: string;
  role: 'ADMIN' | 'BUYER' | 'BUSINESS_USER';
}

/**
 * 用户列表查询参数
 */
export interface UserListQuery {
  role?: string;
  status?: string;
  keyword?: string;
  page: number;
  size: number;
}

/**
 * 用户列表项
 */
export interface UserListItem {
  id: number;
  name: string;
  phone: string;
  email: string;
  role: string;
  status: string;
  isFirstLogin: boolean;
  createdAt: string;
}

/**
 * 用户列表响应 DTO
 */
export interface UserListResponse {
  content: UserListItem[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

/**
 * 审计日志查询参数
 */
export interface AuditLogQuery {
  eventType?: string;
  startTime?: string;
  endTime?: string;
  targetUserId?: number;
  page: number;
  size: number;
}

/**
 * 审计日志项
 */
export interface AuditLogItem {
  id: number;
  eventType: string;
  operatorId: number | null;
  operatorName: string | null;
  targetUserId: number | null;
  targetUserName: string | null;
  ipAddress: string;
  result: string;
  detail: string | null;
  createdAt: string;
}

/**
 * 审计日志响应 DTO
 */
export interface AuditLogResponse {
  content: AuditLogItem[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}
