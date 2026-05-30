package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 修改用户角色命令
 *
 * @param userId  目标用户ID
 * @param newRole 新角色（ADMIN/BUYER/BUSINESS_USER）
 */
public record UpdateUserRoleCommand(Long userId, String newRole) {
}
