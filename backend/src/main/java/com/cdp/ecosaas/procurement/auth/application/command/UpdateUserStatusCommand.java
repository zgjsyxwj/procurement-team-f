package com.cdp.ecosaas.procurement.auth.application.command;

/**
 * 修改用户状态命令（停用/启用）
 *
 * @param userId    目标用户ID
 * @param newStatus 新状态（ACTIVE/DISABLED）
 */
public record UpdateUserStatusCommand(Long userId, String newStatus) {
}
