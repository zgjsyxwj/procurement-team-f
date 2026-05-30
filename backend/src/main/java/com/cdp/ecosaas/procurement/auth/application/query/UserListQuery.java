package com.cdp.ecosaas.procurement.auth.application.query;

/**
 * 用户列表查询参数。
 * <p>
 * 支持按角色、状态筛选和按姓名/手机号关键字搜索。
 *
 * @param role    角色筛选（可选）
 * @param status  状态筛选（可选）
 * @param keyword 关键字搜索，匹配姓名或手机号（可选）
 * @param page    页码（从0开始）
 * @param size    每页大小
 */
public record UserListQuery(String role, String status, String keyword, int page, int size) {
}
