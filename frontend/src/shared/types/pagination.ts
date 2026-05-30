/**
 * 通用分页查询参数（跨模块共享）。
 */
export interface PageQuery {
  page: number;
  size: number;
}

/**
 * 通用分页响应（跨模块共享）。
 */
export interface PageResult<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
