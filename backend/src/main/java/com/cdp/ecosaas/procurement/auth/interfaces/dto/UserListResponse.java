package com.cdp.ecosaas.procurement.auth.interfaces.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户列表分页响应 DTO
 *
 * @param content       用户列表
 * @param page          当前页码
 * @param size          每页大小
 * @param totalElements 总记录数
 * @param totalPages    总页数
 */
public record UserListResponse(
        List<UserItem> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * 用户列表项
     *
     * @param id           用户ID
     * @param name         用户姓名
     * @param phone        手机号
     * @param email        邮箱
     * @param role         角色
     * @param status       状态
     * @param isSuperAdmin 是否超级管理员
     * @param isFirstLogin 是否首次登录
     * @param createdAt    创建时间
     */
    public record UserItem(
            Long id,
            String name,
            String phone,
            String email,
            String role,
            String status,
            boolean isSuperAdmin,
            boolean isFirstLogin,
            LocalDateTime createdAt
    ) {
    }
}
